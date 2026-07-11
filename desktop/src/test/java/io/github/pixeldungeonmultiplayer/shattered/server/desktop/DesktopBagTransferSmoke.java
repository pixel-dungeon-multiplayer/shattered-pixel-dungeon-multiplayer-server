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
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.watabou.noosa.Game;
import com.watabou.plugins.PluginManifest;
import com.watabou.utils.FileUtils;
import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import io.github.pixeldungeonmultiplayer.shattered.server.network.SendData;
import io.github.pixeldungeonmultiplayer.shattered.server.network.Server;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.InventoryRebuildAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import io.github.pixeldungeonmultiplayer.shattered.testclient.ClientInventory;
import io.github.pixeldungeonmultiplayer.shattered.testclient.ClientItem;
import io.github.pixeldungeonmultiplayer.shattered.testclient.SimulatedClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

public final class DesktopBagTransferSmoke {

    private static final long TIMEOUT_MILLIS = 20_000;
    private static final AtomicReference<Throwable> failure = new AtomicReference<>();

    private DesktopBagTransferSmoke() {
    }

    @Test
    @Tag("desktop")
    void smoke() throws Exception {
        Game.version = System.getProperty("Specification-Version", "BagTransferSmoke");
        Game.versionCode = Integer.parseInt(System.getProperty("Implementation-Version", "1"));

        int port = reservePort();
        String basePath = "build" + File.separator + "bagTransferSmoke" + File.separator
                + System.currentTimeMillis() + File.separator;
        File testDir = new File(basePath);
        require(testDir.mkdirs() || testDir.isDirectory(), "could not create test directory");

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SPDMP Bag Transfer Smoke");
        config.setWindowedMode(720, 400);
        config.setPreferencesConfig(basePath, Files.FileType.Local);

        SPDSettings.set(new Lwjgl3Preferences(
                new Lwjgl3FileHandle(basePath + SPDSettings.DEFAULT_PREFS_FILE, Files.FileType.Local)
        ));
        SPDSettings.put("server_uuid", "desktop-bag-transfer-smoke-server");
        SPDSettings.put("server_port", port);
        SPDSettings.put("online_mode", false);
        SPDSettings.put("max_players", 1);
        SPDSettings.put("server_name", "SPDMP Bag Transfer Test Server");
        SPDSettings.put("motd", "");
        FileUtils.setDefaultFileProperties(Files.FileType.Local, basePath);

        new Lwjgl3Application(new BagTransferGame(new TestDesktopPlatformSupport(), port), config);
        if (failure.get() != null) {
            throw new AssertionError("bag transfer smoke failed", failure.get());
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

    private static final class BagTransferGame extends ShatteredPixelDungeon {
        private final int port;
        private Item trackedItem;
        private TestBag collectingBag;
        private final Deque<String> receivedPackets = new ArrayDeque<>();

        private BagTransferGame(TestDesktopPlatformSupport platform, int port) {
            super(platform);
            this.port = port;
        }

        @Override
        public void create() {
            super.create();
            Thread testThread = new Thread(this::runTest, "SPDMP bag-transfer client");
            testThread.setDaemon(true);
            testThread.start();
        }

        private void runTest() {
            try {
                waitFor(() -> Server.started, "server did not start");
                try (SimulatedClient client = new SimulatedClient().connect(connectClientSocket())) {
                    client.beforePacket((ignored, packet) -> recordPacket(packet));
                    client.parseNext();
                    AtomicBoolean sceneReady = new AtomicBoolean(false);
                    client.afterAction("interlevel_scene", (c, action) -> {
                        if ("fade_out".equals(action.optString("state", ""))) {
                            sceneReady.set(true);
                        }
                    });
                    client.join("warrior", "");
                    waitForClient(client, sceneReady::get, "game scene was not initialized");

                    addTrackedItemAndRebuild();
                    waitForClient(client, () -> client.inventory() != null
                                    && client.inventory().itemAt(serverPath(trackedItem)) != null,
                            "initial tracked item was not received");

                    placeCollectingBag();
                    client.selectCell(firstHero().pos);
                    waitForClient(client, () -> {
                                List<Integer> path = currentServerPath(trackedItem);
                                return path != null && path.size() == 2
                                        && client.inventory().itemsByPath().containsKey(path);
                            },
                            "bag pickup did not complete");

                    List<Integer> nestedPath = serverPath(trackedItem);
                    require(nestedPath.size() == 2, "tracked item was not moved into the bag: " + nestedPath);
                    increaseTrackedQuantity();
                    waitForClient(client, () -> {
                        ClientItem item = client.inventory().itemAt(nestedPath);
                        return item != null && item.quantity == 2;
                    }, "nested item_update was not applied at " + nestedPath);
                    assertInventoryMatches(client.inventory());

                    AtomicInteger itemAdds = new AtomicInteger();
                    AtomicInteger itemRemoves = new AtomicInteger();
                    client.afterAction("item_add", (c, action) -> itemAdds.incrementAndGet());
                    client.afterAction("item_remove", (c, action) -> itemRemoves.incrementAndGet());

                    Item secondItem = placeOnHeroCell(new TestInventoryItem());
                    int secondItemAdds = itemAdds.get() + 1;
                    client.selectCell(firstHero().pos);
                    waitForClient(client, () -> itemAdds.get() >= secondItemAdds
                                    && hasNestedPath(secondItem),
                            "second matching item was not added to the collected bag");

                    int expectedRemoves = itemRemoves.get() + 1;
                    client.itemAction(serverPath(collectingBag), "DROP");
                    waitForClient(client, () -> itemRemoves.get() >= expectedRemoves
                                    && currentServerPath(collectingBag) == null,
                            "drop action did not remove the bag from the inventory");

                    int repickupAdds = itemAdds.get() + 1;
                    client.selectCell(firstHero().pos);
                    waitForClient(client, () -> itemAdds.get() >= repickupAdds
                                    && hasNestedPath(trackedItem)
                                    && hasNestedPath(secondItem),
                            "dropped bag was not picked up with its nested contents");

                    FullTestBag fullBag = new FullTestBag();
                    Item fullBagItem = new FullBagItem();
                    require(fullBag.addItemDirect(fullBagItem), "could not prefill test bag");
                    placeOnHeroCell(fullBag);
                    int fullBagAdds = itemAdds.get() + 1;
                    client.selectCell(firstHero().pos);
                    waitForClient(client, () -> itemAdds.get() >= fullBagAdds
                                    && hasNestedPath(fullBagItem),
                            "prefilled bag was not picked up with its contents");

                    Item overflowItem = placeOnHeroCell(new FullBagItem());
                    int overflowAdds = itemAdds.get() + 1;
                    client.selectCell(firstHero().pos);
                    waitForClient(client, () -> itemAdds.get() >= overflowAdds
                                    && hasRootPath(overflowItem),
                            "item accepted by a full bag instead of staying in the backpack");
                    assertInventoryMatches(client.inventory());
                }
            } catch (Throwable t) {
                failure.set(new AssertionError("bag transfer packet trace:\n" + receivedPackets, t));
            } finally {
                Server.stopServer();
                Gdx.app.exit();
            }
        }

        private void recordPacket(JSONObject packet) {
            JSONArray actions = packet.optJSONArray("actions");
            if (actions == null) {
                return;
            }
            StringBuilder trace = new StringBuilder();
            for (int i = 0; i < actions.length(); i++) {
                JSONObject action = actions.getJSONObject(i);
                String actionName = action.optString("action_name", "");
                if (!actionName.startsWith("item_") && !"inventory_rebuild".equals(actionName)) {
                    continue;
                }
                if (trace.length() > 0) {
                    trace.append(", ");
                }
                trace.append(actionName);
                if (action.has("path")) {
                    trace.append(' ').append(action.getJSONArray("path"));
                }
            }
            if (trace.length() == 0) {
                return;
            }
            if (receivedPackets.size() == 20) {
                receivedPackets.removeFirst();
            }
            receivedPackets.addLast(trace.toString());
        }

        private void addTrackedItemAndRebuild() throws InterruptedException {
            onGameThread(() -> {
                Hero hero = firstHero();
                trackedItem = new TestInventoryItem();
                require(hero.belongings.backpack.addItemDirect(trackedItem), "could not add tracked item");
                SendData.packAndSendAction(hero, new InventoryRebuildAction(hero));
                SendData.forceFlush(hero);
            });
        }

        private void placeCollectingBag() throws InterruptedException {
            onGameThread(() -> {
                Hero hero = firstHero();
                collectingBag = new TestBag();
                Dungeon.level.drop(collectingBag, hero.pos);
                SendData.forceFlush(hero);
            });
        }

        private Item placeOnHeroCell(Item item) throws InterruptedException {
            onGameThread(() -> {
                Hero hero = firstHero();
                Dungeon.level.drop(item, hero.pos);
                SendData.forceFlush(hero);
            });
            return item;
        }

        private void increaseTrackedQuantity() throws InterruptedException {
            onGameThread(() -> {
                trackedItem.quantity(2, true);
                SendData.forceFlush(firstHero());
            });
        }

        private List<Integer> serverPath(Item item) {
            List<Integer> path = currentServerPath(item);
            require(path != null && !path.isEmpty(), "server item has no path: " + item);
            return path;
        }

        private List<Integer> currentServerPath(Item item) {
            return firstHero().belongings.pathOfItem(item);
        }

        private boolean hasNestedPath(Item item) {
            List<Integer> path = currentServerPath(item);
            return path != null && path.size() == 2;
        }

        private boolean hasRootPath(Item item) {
            List<Integer> path = currentServerPath(item);
            return path != null && path.size() == 1;
        }

        private void assertInventoryMatches(ClientInventory actual) {
            ClientInventory expected = new ClientInventoryBuilder(firstHero()).build();
            require(expected.itemsByPath().keySet().equals(actual.itemsByPath().keySet()),
                    "client inventory paths differ from server: expected (server)=" + expected.itemsByPath().keySet()
                            + ", actual (client)=" + actual.itemsByPath().keySet());
            for (List<Integer> path : expected.itemsByPath().keySet()) {
                ClientItem expectedItem = expected.itemAt(path);
                ClientItem actualItem = actual.itemAt(path);
                require(expectedItem.quantity == actualItem.quantity,
                        "client item quantity differs at " + path);
            }
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
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static final class ClientInventoryBuilder {
        private final Hero hero;

        private ClientInventoryBuilder(Hero hero) {
            this.hero = hero;
        }

        private ClientInventory build() {
            Object serialized = new SerializationContext(Server.SERIALIZERS, hero)
                    .serialize(hero.belongings, "rebuild");
            return ClientInventory.fromJson((JSONObject) serialized);
        }
    }

    private static final class TestInventoryItem extends Item {
        {
            stackable = false;
        }

        @Override
        public LocalizedString name() {
            return LocalizedString.raw("bag-transfer-item");
        }
    }

    private static final class TestBag extends Bag {
        @Override
        public boolean canHold(Item item) {
            return item instanceof TestInventoryItem && super.canHold(item);
        }

        @Override
        public Icons getBagIcon() {
            return Icons.BACKPACK;
        }

        @Override
        public LocalizedString name() {
            return LocalizedString.raw("bag-transfer-bag");
        }
    }

    private static final class FullBagItem extends Item {
        {
            stackable = false;
        }

        @Override
        public LocalizedString name() {
            return LocalizedString.raw("full-bag-item");
        }
    }

    private static final class FullTestBag extends Bag {
        @Override
        public int capacity() {
            return 1;
        }

        @Override
        public boolean canHold(Item item) {
            return item instanceof FullBagItem && super.canHold(item);
        }

        @Override
        public Icons getBagIcon() {
            return Icons.BACKPACK;
        }

        @Override
        public LocalizedString name() {
            return LocalizedString.raw("full-test-bag");
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
