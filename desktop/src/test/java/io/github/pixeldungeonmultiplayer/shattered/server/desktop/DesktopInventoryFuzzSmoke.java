package io.github.pixeldungeonmultiplayer.shattered.server.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3FileHandle;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Preferences;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.desktop.DesktopPlatformSupport;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.watabou.noosa.Game;
import com.watabou.plugins.PluginManifest;
import com.watabou.utils.FileUtils;
import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import io.github.pixeldungeonmultiplayer.shattered.server.network.SendData;
import io.github.pixeldungeonmultiplayer.shattered.server.network.Server;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import io.github.pixeldungeonmultiplayer.shattered.testclient.ClientInventory;
import io.github.pixeldungeonmultiplayer.shattered.testclient.ClientItem;
import io.github.pixeldungeonmultiplayer.shattered.testclient.SimulatedClient;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

public final class DesktopInventoryFuzzSmoke {

    private static final long TIMEOUT_MILLIS = 5_000;
    private static final int STEPS = 1_000;
    private static final int INITIAL_ITEMS = 32;
    private static final long SEED = 0x5EED_1A7EL;
    private static final AtomicReference<Throwable> failure = new AtomicReference<>();

    private DesktopInventoryFuzzSmoke() {
    }

    public static void main(String[] args) throws Exception {
        Game.version = System.getProperty("Specification-Version", "InventoryFuzzSmoke");
        Game.versionCode = Integer.parseInt(System.getProperty("Implementation-Version", "1"));

        int port = reservePort();
        String basePath = "build" + File.separator + "inventoryFuzzSmoke" + File.separator
                + System.currentTimeMillis() + File.separator;
        File testDir = new File(basePath);
        require(testDir.mkdirs() || testDir.isDirectory(), "could not create test directory");

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SPDMP Inventory Fuzz Smoke");
        config.setWindowedMode(720, 400);
        config.setPreferencesConfig(basePath, Files.FileType.Local);
        SPDSettings.set(new Lwjgl3Preferences(
                new Lwjgl3FileHandle(basePath + SPDSettings.DEFAULT_PREFS_FILE, Files.FileType.Local)
        ));
        SPDSettings.put("server_uuid", "desktop-inventory-fuzz-smoke-server");
        SPDSettings.put("server_port", port);
        SPDSettings.put("online_mode", false);
        SPDSettings.put("max_players", 1);
        SPDSettings.put("server_name", "SPDMP Inventory Fuzz Test Server");
        SPDSettings.put("motd", "");
        FileUtils.setDefaultFileProperties(Files.FileType.Local, basePath);

        new Lwjgl3Application(new FuzzGame(new TestDesktopPlatformSupport(), port), config);
        if (failure.get() != null) {
            failure.get().printStackTrace();
            System.exit(1);
        }
    }

    private static int reservePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class FuzzGame extends ShatteredPixelDungeon {
        private final int port;
        private final Random random = new Random(SEED);
        private final Deque<String> trace = new ArrayDeque<>();
        private int nextId;
        private String lastMismatch;

        private FuzzGame(TestDesktopPlatformSupport platform, int port) {
            super(platform);
            this.port = port;
        }

        @Override
        public void create() {
            super.create();
            Thread testThread = new Thread(this::runTest, "SPDMP inventory-fuzz client");
            testThread.setDaemon(true);
            testThread.start();
        }

        private void runTest() {
            try {
                waitFor(() -> Server.started, "server did not start");
                try (SimulatedClient client = new SimulatedClient().connect(connectClientSocket())) {
                    client.parseNext();
                    AtomicBoolean sceneReady = new AtomicBoolean(false);
                    client.afterAction("interlevel_scene", (c, action) -> {
                        if ("fade_out".equals(action.optString("state", ""))) sceneReady.set(true);
                    });
                    client.join("warrior", "");
                    waitForClient(client, sceneReady::get, "game scene was not initialized");

                    AtomicInteger heroReady = new AtomicInteger();
                    AtomicInteger itemChanges = new AtomicInteger();
                    AtomicInteger spriteActions = new AtomicInteger();
                    client.afterAction("hero_ready", (c, action) -> heroReady.incrementAndGet());
                    client.afterAction("item_add", (c, action) -> itemChanges.incrementAndGet());
                    client.afterAction("item_remove", (c, action) -> itemChanges.incrementAndGet());
                    client.afterAction("item_update", (c, action) -> itemChanges.incrementAndGet());
                    client.afterAction("sprite_action", (c, action) -> spriteActions.incrementAndGet());
                    fillHeroHeap();

                    for (int step = 1; step <= STEPS; step++) {
                        List<List<Integer>> paths = fuzzPaths(client.inventory());
                        if (paths.isEmpty() || random.nextInt(100) < 62) {
                            record(step + " PICK");
                            int expectedReady = heroReady.get() + 1;
                            client.selectCell(firstHero().pos);
                            waitForClient(client, () -> heroReady.get() >= expectedReady,
                                    "pickup did not complete at step " + step);
                        } else {
                            List<Integer> path = paths.get(random.nextInt(paths.size()));
                            record(step + " DROP " + path);
                            int expectedChanges = itemChanges.get() + 1;
                            client.itemAction(path, "DROP");
                            waitForClient(client, () -> itemChanges.get() >= expectedChanges,
                                    "drop did not complete at step " + step);
                        }
                        if (step % 25 == 0 || step == STEPS) {
                            System.out.println("Inventory fuzz progress: step " + step + "/" + STEPS);
                        }
                    }
                    int expectedSpriteActions = spriteActions.get() + 1;
                    client.selectCell(heroPosition() + 1);
                    waitForClient(client, () -> spriteActions.get() >= expectedSpriteActions,
                            "movement bound was not received after fuzz run");
                    waitForInventory(client, "inventory mismatch after fuzz run (seed=" + SEED + ")");
                }
            } catch (Throwable t) {
                failure.set(t);
            } finally {
                Server.stopServer();
                Gdx.app.exit();
            }
        }

        private void fillHeroHeap() throws InterruptedException {
            onGameThread(() -> {
                Hero hero = firstHero();
                for (int i = 0; i < INITIAL_ITEMS; i++) {
                    Dungeon.level.drop(random.nextBoolean() ? new StackItem() : new LooseItem(nextId++), hero.pos);
                }
                SendData.forceFlush(hero);
            });
        }

        private int heroPosition() throws InterruptedException {
            AtomicReference<Integer> result = new AtomicReference<>();
            onGameThread(() -> result.set(firstHero().pos));
            return result.get();
        }

        private List<List<Integer>> fuzzPaths(ClientInventory inventory) {
            ArrayList<List<Integer>> paths = new ArrayList<>();
            for (Map.Entry<List<Integer>, ClientItem> entry : inventory.itemsByPath().entrySet()) {
                if (entry.getKey().size() == 1 && isFuzzItem(entry.getValue())) paths.add(entry.getKey());
            }
            return paths;
        }

        private boolean isFuzzItem(ClientItem item) {
            Object name = item.raw.opt("name");
            return name instanceof JSONObject && ((JSONObject) name).optString("raw", "").startsWith("fuzz-");
        }

        private void waitForInventory(SimulatedClient client, String message) throws IOException, InterruptedException {
            long deadline = System.currentTimeMillis() + TIMEOUT_MILLIS;
            while (System.currentTimeMillis() < deadline) {
                if (inventoryMatches(serverInventory(), client.inventory())) return;
                try {
                    client.parseNext();
                } catch (SocketTimeoutException ignored) {
                }
            }
            require(inventoryMatches(serverInventory(), client.inventory()),
                    message + "\n" + lastMismatch + "\nrecent actions=" + trace);
        }

        private ClientInventory serverInventory() throws InterruptedException {
            AtomicReference<ClientInventory> result = new AtomicReference<>();
            onGameThread(() -> {
                Hero hero = firstHero();
                Object serialized = new SerializationContext(Server.SERIALIZERS, hero)
                        .serialize(hero.belongings, "rebuild");
                result.set(ClientInventory.fromJson((JSONObject) serialized));
            });
            return result.get();
        }

        private boolean inventoryMatches(ClientInventory expected, ClientInventory actual) {
            if (actual == null) {
                lastMismatch = "client inventory is absent";
                return false;
            }
            if (!expected.itemsByPath().keySet().equals(actual.itemsByPath().keySet())) {
                lastMismatch = "paths: expected=" + describe(expected) + ", actual=" + describe(actual);
                return false;
            }
            for (List<Integer> path : expected.itemsByPath().keySet()) {
                ClientItem expectedItem = expected.itemAt(path);
                ClientItem actualItem = actual.itemAt(path);
                if (expectedItem.quantity != actualItem.quantity || expectedItem.image != actualItem.image) {
                    lastMismatch = "item at " + path + ": expected=" + expectedItem.image + "/" + expectedItem.quantity
                            + ", actual=" + actualItem.image + "/" + actualItem.quantity;
                    return false;
                }
            }
            return true;
        }

        private void record(String action) {
            if (trace.size() == 50) trace.removeFirst();
            trace.addLast(action);
        }

        private String describe(Item item) {
            return item.getClass().getSimpleName() + "(q=" + item.quantity() + ")";
        }

        private String describe(ClientInventory inventory) {
            StringBuilder result = new StringBuilder("[");
            for (Map.Entry<List<Integer>, ClientItem> entry : inventory.itemsByPath().entrySet()) {
                if (result.length() > 1) result.append(", ");
                result.append(entry.getKey()).append('=').append(entry.getValue().image)
                        .append('/').append(entry.getValue().quantity);
            }
            return result.append(']').toString();
        }

        private void onGameThread(ThrowingRunnable operation) throws InterruptedException {
            AtomicReference<Throwable> error = new AtomicReference<>();
            CountDownLatch done = new CountDownLatch(1);
            Game.runOnRenderThread(() -> {
                try {
                    operation.run();
                } catch (Throwable t) {
                    error.set(t);
                } finally {
                    done.countDown();
                }
            });
            require(done.await(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS), "game thread operation timed out");
            if (error.get() != null) throw new AssertionError("game thread operation failed", error.get());
        }

        private void waitForClient(SimulatedClient client, BooleanSupplier condition, String message) throws IOException {
            long deadline = System.currentTimeMillis() + TIMEOUT_MILLIS;
            while (!condition.getAsBoolean() && System.currentTimeMillis() < deadline) {
                try {
                    client.parseNext();
                } catch (SocketTimeoutException ignored) {
                }
            }
            require(condition.getAsBoolean(), message);
        }

        private void waitFor(BooleanSupplier condition, String message) throws InterruptedException {
            long deadline = System.currentTimeMillis() + TIMEOUT_MILLIS;
            while (!condition.getAsBoolean() && System.currentTimeMillis() < deadline) Thread.sleep(50);
            require(condition.getAsBoolean(), message);
        }

        private Socket connectClientSocket() throws IOException {
            Socket socket = new Socket("127.0.0.1", port);
            socket.setSoTimeout(100);
            return socket;
        }

        private Hero firstHero() {
            for (Hero hero : Dungeon.heroes) if (hero != null) return hero;
            throw new AssertionError("server has no joined hero");
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable { void run() throws Exception; }

    private static final class StackItem extends Item {
        @Override public LocalizedString name() { return LocalizedString.raw("fuzz-stack"); }
    }

    private static final class LooseItem extends Item {
        private final int id;
        private LooseItem(int id) { this.id = id; stackable = false; }
        @Override public LocalizedString name() { return LocalizedString.raw("fuzz-loose-" + id); }
    }

    private static final class TestDesktopPlatformSupport extends DesktopPlatformSupport {
        @Override public List<PluginManifest> loadPlugins() { return new ArrayList<>(); }
        @Override public void registerService(int port, Map<String, String> properties) { }
        @Override public void updateService(Map<String, String> properties) { }
        @Override public void unregisterService() { }
    }
}
