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
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CapeOfThorns;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.ChaliceOfBlood;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfAccuracy;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEvasion;
import com.watabou.noosa.Game;
import com.watabou.plugins.PluginManifest;
import com.watabou.utils.FileUtils;
import io.github.pixeldungeonmultiplayer.shattered.server.network.Protocol;
import io.github.pixeldungeonmultiplayer.shattered.server.network.SendData;
import io.github.pixeldungeonmultiplayer.shattered.server.network.Server;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import io.github.pixeldungeonmultiplayer.shattered.testclient.ClientInventory;
import io.github.pixeldungeonmultiplayer.shattered.testclient.ClientItem;
import io.github.pixeldungeonmultiplayer.shattered.testclient.ClientWindow;
import io.github.pixeldungeonmultiplayer.shattered.testclient.SimulatedClient;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

public final class DesktopEquipSwapSmoke {

    private static final String SERVER_UUID = "desktop-equip-swap-smoke-server";
    private static final long TIMEOUT_MILLIS = 20_000;
    private static final AtomicReference<Throwable> failure = new AtomicReference<>();

    private DesktopEquipSwapSmoke() {
    }

    public static void main(String[] args) throws Exception {
        Game.version = System.getProperty("Specification-Version", "EquipSwapSmoke");
        Game.versionCode = Integer.parseInt(System.getProperty("Implementation-Version", "1"));

        int port = reservePort();
        String basePath = "build" + File.separator + "equipSwapSmoke" + File.separator
                + System.currentTimeMillis() + File.separator;
        File testDir = new File(basePath);
        require(testDir.mkdirs() || testDir.isDirectory(), "could not create test directory");

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SPDMP Equip Swap Smoke");
        config.setWindowedMode(720, 400);
        config.setPreferencesConfig(basePath, Files.FileType.Local);

        SPDSettings.set(new Lwjgl3Preferences(
                new Lwjgl3FileHandle(basePath + SPDSettings.DEFAULT_PREFS_FILE, Files.FileType.Local)
        ));
        SPDSettings.put("server_uuid", SERVER_UUID);
        SPDSettings.put("server_port", port);
        SPDSettings.put("online_mode", false);
        SPDSettings.put("max_players", 1);
        SPDSettings.put("server_name", "SPDMP Equip Swap Test Server");
        SPDSettings.put("motd", "");
        FileUtils.setDefaultFileProperties(Files.FileType.Local, basePath);

        new Lwjgl3Application(new EquipSwapGame(new TestDesktopPlatformSupport(), port), config);

        Throwable thrown = failure.get();
        if (thrown != null) {
            thrown.printStackTrace();
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
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static final class EquipSwapGame extends ShatteredPixelDungeon {
        private final int port;

        private EquipSwapGame(TestDesktopPlatformSupport platform, int port) {
            super(platform);
            this.port = port;
        }

        @Override
        public void create() {
            super.create();
            Thread testThread = new Thread(this::runTest, "SPDMP equip-swap client");
            testThread.setDaemon(true);
            testThread.start();
        }

        private void runTest() {
            try {
                waitFor(() -> Server.started, "server did not start");
                try (SimulatedClient client = new SimulatedClient().connect(connectClientSocket())) {
                    client.parseNext();
                    AtomicBoolean gameSceneReady = new AtomicBoolean(false);
                    client.afterAction("interlevel_scene", (c, action) -> {
                        if ("fade_out".equals(action.optString("state", ""))) {
                            gameSceneReady.set(true);
                        }
                    });
                    client.join("warrior", "");
                    waitForClient(client, gameSceneReady::get, "game scene was not initialized");

                    List<Item> items = seedInventory();
                    waitForClient(client, () -> allPresent(client.inventory(), items), "seed items were not received");

                    equip(client, items.get(0), -3);
                    equip(client, items.get(1), -5);
                    equip(client, items.get(2), -4);

                    AtomicReference<Integer> equipWindowId = new AtomicReference<>();
                    client.afterAction("update_window", (c, action) -> equipWindowId.set(action.getInt("id")));
                    client.itemAction(serverPath(items.get(3)), "EQUIP");
                    waitForClient(client, () -> equipWindowId.get() != null, "equip slot window was not received");
                    ClientWindow window = client.windows().get(equipWindowId.get());
                    require(window != null, "equip slot window was not retained by the client");

                    AtomicBoolean windowHidden = new AtomicBoolean(false);
                    client.afterAction("hide_window", (c, action) -> {
                        if (action.getInt("id") == window.id) {
                            windowHidden.set(true);
                        }
                    });
                    client.selectWindow(window.id, 1);
                    waitForClient(client, windowHidden::get, "equip slot window was not hidden");

                    compareInventory(serverInventory(), client.inventory());
                    require(client.inventory().itemAt(Arrays.asList(-4)) != null, "replacement item is missing from misc slot");
                }
            } catch (Throwable t) {
                failure.set(t);
            } finally {
                Server.stopServer();
                Gdx.app.exit();
            }
        }

        private List<Item> seedInventory() throws InterruptedException {
            AtomicReference<List<Item>> result = new AtomicReference<>();
            AtomicReference<Throwable> error = new AtomicReference<>();
            CountDownLatch done = new CountDownLatch(1);
            Gdx.app.postRunnable(() -> {
                try {
                    Hero hero = firstHero();
                    List<Item> items = Arrays.asList(
                            new ChaliceOfBlood(),
                            new RingOfAccuracy(),
                            new RingOfEvasion(),
                            new CapeOfThorns()
                    );
                    for (Item item : items) {
                        require(item.collect(hero.belongings.backpack), "could not seed " + item.getClass().getSimpleName());
                    }
                    SendData.forceFlush(hero);
                    result.set(items);
                } catch (Throwable t) {
                    error.set(t);
                } finally {
                    done.countDown();
                }
            });
            require(done.await(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS), "timed out seeding inventory");
            if (error.get() != null) {
                throw new AssertionError("could not seed inventory", error.get());
            }
            return result.get();
        }

        private void equip(SimulatedClient client, Item item, int targetSlot) throws IOException {
            client.itemAction(serverPath(item), "EQUIP");
            waitForClient(client, () -> client.inventory().itemAt(Arrays.asList(targetSlot)) != null,
                    "item was not equipped into slot " + targetSlot);
        }

        private List<Integer> serverPath(Item item) {
            List<Integer> path = firstHero().belongings.pathOfItem(item);
            require(path != null && !path.isEmpty(), "server item has no inventory path: " + item);
            return path;
        }

        private boolean allPresent(ClientInventory inventory, List<Item> items) {
            if (inventory == null) {
                return false;
            }
            int count = 0;
            for (Map.Entry<List<Integer>, ClientItem> entry : inventory.itemsByPath().entrySet()) {
                if (!entry.getKey().isEmpty() && entry.getKey().get(0) >= 0) {
                    count++;
                }
            }
            return count >= firstHero().belongings.backpack.items().size();
        }

        private void waitForClient(SimulatedClient client, BooleanSupplier condition, String failureMessage)
                throws IOException {
            long deadline = System.currentTimeMillis() + TIMEOUT_MILLIS;
            while (!condition.getAsBoolean() && System.currentTimeMillis() < deadline) {
                try {
                    client.parseNext();
                } catch (SocketTimeoutException ignored) {
                }
            }
            require(condition.getAsBoolean(), failureMessage);
        }

        private void waitFor(BooleanSupplier condition, String failureMessage) throws InterruptedException {
            long deadline = System.currentTimeMillis() + TIMEOUT_MILLIS;
            while (!condition.getAsBoolean() && System.currentTimeMillis() < deadline) {
                Thread.sleep(50);
            }
            require(condition.getAsBoolean(), failureMessage);
        }

        private Socket connectClientSocket() throws IOException {
            Socket socket = new Socket("127.0.0.1", port);
            socket.setSoTimeout(1_000);
            return socket;
        }

        private Hero firstHero() {
            if (Dungeon.heroes != null) {
                for (Hero hero : Dungeon.heroes) {
                    if (hero != null) {
                        return hero;
                    }
                }
            }
            throw new AssertionError("server has no joined hero");
        }

        private ClientInventory serverInventory() {
            Hero hero = firstHero();
            Object serialized = new SerializationContext(Server.SERIALIZERS, hero)
                    .serialize(hero.belongings, "rebuild");
            return ClientInventory.fromJson((JSONObject) serialized);
        }

        private void compareInventory(ClientInventory expected, ClientInventory actual) {
            require(actual != null, "client inventory was not built");
            require(expected.itemsByPath().size() == actual.itemsByPath().size(), "inventory path count mismatch");
            for (Map.Entry<List<Integer>, ClientItem> entry : expected.itemsByPath().entrySet()) {
                ClientItem actualItem = actual.itemAt(entry.getKey());
                require(actualItem != null, "missing client item at path " + entry.getKey());
                require(entry.getValue().image == actualItem.image, "item image mismatch at path " + entry.getKey());
                require(entry.getValue().quantity == actualItem.quantity,
                        "item quantity mismatch at path " + entry.getKey());
            }
        }
    }

    private static final class TestDesktopPlatformSupport extends DesktopPlatformSupport {
        @Override
        public List<PluginManifest> loadPlugins() {
            return new ArrayList<>();
        }

        @Override
        public void registerService(int port, Map<String, String> properties) {
        }

        @Override
        public void updateService(Map<String, String> properties) {
        }

        @Override
        public void unregisterService() {
        }
    }
}
