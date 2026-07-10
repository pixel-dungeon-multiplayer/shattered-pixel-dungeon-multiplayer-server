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
import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import com.watabou.noosa.Game;
import com.watabou.plugins.PluginManifest;
import com.watabou.utils.FileUtils;
import io.github.pixeldungeonmultiplayer.shattered.server.network.SendData;
import io.github.pixeldungeonmultiplayer.shattered.server.network.Server;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.InventoryRebuildAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import io.github.pixeldungeonmultiplayer.shattered.testclient.ClientInventory;
import io.github.pixeldungeonmultiplayer.shattered.testclient.ClientItem;
import io.github.pixeldungeonmultiplayer.shattered.testclient.SimulatedClient;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

public final class DesktopInventoryCapacitySmoke {

    private static final long TIMEOUT_MILLIS = 20_000;
    private static final int ITERATIONS = 50;
    private static final AtomicReference<Throwable> failure = new AtomicReference<>();

    private DesktopInventoryCapacitySmoke() {
    }

    public static void main(String[] args) throws Exception {
        Game.version = System.getProperty("Specification-Version", "InventoryCapacitySmoke");
        Game.versionCode = Integer.parseInt(System.getProperty("Implementation-Version", "1"));

        int port = reservePort();
        String basePath = "build" + File.separator + "inventoryCapacitySmoke" + File.separator
                + System.currentTimeMillis() + File.separator;
        File testDir = new File(basePath);
        require(testDir.mkdirs() || testDir.isDirectory(), "could not create test directory");

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SPDMP Inventory Capacity Smoke");
        config.setWindowedMode(720, 400);
        config.setPreferencesConfig(basePath, Files.FileType.Local);

        SPDSettings.set(new Lwjgl3Preferences(
                new Lwjgl3FileHandle(basePath + SPDSettings.DEFAULT_PREFS_FILE, Files.FileType.Local)
        ));
        SPDSettings.put("server_uuid", "desktop-inventory-capacity-smoke-server");
        SPDSettings.put("server_port", port);
        SPDSettings.put("online_mode", false);
        SPDSettings.put("max_players", 1);
        SPDSettings.put("server_name", "SPDMP Inventory Capacity Test Server");
        SPDSettings.put("motd", "");
        FileUtils.setDefaultFileProperties(Files.FileType.Local, basePath);

        new Lwjgl3Application(new CapacityGame(new TestDesktopPlatformSupport(), port), config);
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
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static final class CapacityGame extends ShatteredPixelDungeon {
        private final int port;
        private final List<Item> fuzzItems = new ArrayList<>();

        private CapacityGame(TestDesktopPlatformSupport platform, int port) {
            super(platform);
            this.port = port;
        }

        @Override
        public void create() {
            super.create();
            Thread testThread = new Thread(this::runTest, "SPDMP inventory-capacity client");
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
                        if ("fade_out".equals(action.optString("state", ""))) {
                            sceneReady.set(true);
                        }
                    });
                    client.join("warrior", "");
                    waitForClient(client, sceneReady::get, "game scene was not initialized");

                    fillBackpack();
                    waitForClient(client, () -> client.inventory() != null
                            && client.inventory().backpack.items.size() == firstHero().belongings.backpack.capacity(),
                            "full inventory rebuild was not received");

                    AtomicInteger removes = new AtomicInteger();
                    AtomicInteger adds = new AtomicInteger();
                    AtomicInteger readyActions = new AtomicInteger();
                    client.afterAction("item_remove", (c, action) -> removes.incrementAndGet());
                    client.afterAction("item_add", (c, action) -> adds.incrementAndGet());
                    client.afterAction("hero_ready", (c, action) -> readyActions.incrementAndGet());

                    for (int i = 0; i < ITERATIONS; i++) {
                        Item incoming = placeOverflowItem();
                        int expectedReady = readyActions.get() + 1;
                        int expectedAdds = adds.get();
                        client.selectCell(firstHero().pos);
                        waitForClient(client, () -> readyActions.get() >= expectedReady,
                                "full-inventory pickup did not complete at iteration " + i);
                        require(adds.get() == expectedAdds,
                                "full-inventory pickup sent item_add at iteration " + i);
                        require(!firstHero().belongings.backpack.contains(incoming),
                                "overflow item was collected at iteration " + i);

                        int itemIndex = i % fuzzItems.size();
                        Item discarded = fuzzItems.get(itemIndex);
                        markDroppedItemBehindHeap(discarded);
                        int expectedRemoves = removes.get() + 1;
                        int expectedPickupAdds = adds.get() + 1;
                        client.itemAction(serverPath(discarded), "DROP");
                        waitForClient(client, () -> removes.get() >= expectedRemoves,
                                "drop action was not applied at iteration " + i);
                        require(!firstHero().belongings.backpack.contains(discarded),
                                "dropped item remained in inventory at iteration " + i);

                        client.selectCell(firstHero().pos);
                        waitForClient(client, () -> removes.get() >= expectedRemoves && adds.get() >= expectedPickupAdds,
                                "pickup action was not applied at iteration " + i);

                        Hero hero = firstHero();
                        require(hero.belongings.backpack.items().size() == hero.belongings.backpack.capacity(),
                                "server inventory is not full at iteration " + i);
                        require(hero.belongings.backpack.contains(incoming),
                                "overflow item was not collected after freeing a slot at iteration " + i);
                        fuzzItems.set(itemIndex, incoming);
                        compareInventory(serverInventory(), client.inventory());
                    }
                }
            } catch (Throwable t) {
                failure.set(t);
            } finally {
                Server.stopServer();
                Gdx.app.exit();
            }
        }

        private void fillBackpack() throws InterruptedException {
            onGameThread(() -> {
                Hero hero = firstHero();
                while (hero.belongings.backpack.items().size() < hero.belongings.backpack.capacity()) {
                    Item item = new TestInventoryItem();
                    require(hero.belongings.backpack.addItemDirect(item), "could not fill backpack");
                    fuzzItems.add(item);
                }
                SendData.packAndSendAction(hero, new InventoryRebuildAction(hero));
                SendData.forceFlush(hero);
            });
        }

        private Item placeOverflowItem() throws InterruptedException {
            AtomicReference<Item> result = new AtomicReference<>();
            onGameThread(() -> {
                Hero hero = firstHero();
                Item incoming = new TestInventoryItem();
                Dungeon.level.drop(incoming, hero.pos);
                SendData.forceFlush(hero);
                result.set(incoming);
            });
            return result.get();
        }

        private void markDroppedItemBehindHeap(Item item) throws InterruptedException {
            onGameThread(() -> item.dropsDownHeap = true);
        }

        private List<Integer> serverPath(Item item) {
            List<Integer> path = firstHero().belongings.pathOfItem(item);
            require(path != null && !path.isEmpty(), "server item has no path: " + item);
            return path;
        }

        private void onGameThread(ThrowingRunnable operation) throws InterruptedException {
            AtomicReference<Throwable> error = new AtomicReference<>();
            CountDownLatch done = new CountDownLatch(1);
            Gdx.app.postRunnable(() -> {
                try {
                    operation.run();
                } catch (Throwable t) {
                    error.set(t);
                } finally {
                    done.countDown();
                }
            });
            require(done.await(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS), "game thread operation timed out");
            if (error.get() != null) {
                throw new AssertionError("game thread operation failed", error.get());
            }
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
            while (!condition.getAsBoolean() && System.currentTimeMillis() < deadline) {
                Thread.sleep(50);
            }
            require(condition.getAsBoolean(), message);
        }

        private Socket connectClientSocket() throws IOException {
            Socket socket = new Socket("127.0.0.1", port);
            socket.setSoTimeout(1_000);
            return socket;
        }

        private Hero firstHero() {
            for (Hero hero : Dungeon.heroes) {
                if (hero != null) {
                    return hero;
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
                ClientItem clientItem = actual.itemAt(entry.getKey());
                require(clientItem != null, "missing client item at path " + entry.getKey());
                require(jsonValueEquals(entry.getValue().name, clientItem.name),
                        "item name mismatch at " + entry.getKey() + ": expected=" + entry.getValue().name
                                + ", actual=" + clientItem.name);
                require(entry.getValue().image == clientItem.image, "item image mismatch at " + entry.getKey());
                require(entry.getValue().quantity == clientItem.quantity, "item quantity mismatch at " + entry.getKey());
            }
        }

        private boolean jsonValueEquals(Object expected, Object actual) {
            if (expected instanceof JSONObject && actual instanceof JSONObject) {
                return ((JSONObject) expected).similar(actual);
            }
            if (expected instanceof JSONArray && actual instanceof JSONArray) {
                return ((JSONArray) expected).similar(actual);
            }
            return Objects.equals(expected, actual);
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static final class TestInventoryItem extends Item {
        private static int nextId;
        private final int id = nextId++;

        {
            stackable = false;
        }

        @Override
        public LocalizedString name() {
            return LocalizedString.raw("test-item-" + id);
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
