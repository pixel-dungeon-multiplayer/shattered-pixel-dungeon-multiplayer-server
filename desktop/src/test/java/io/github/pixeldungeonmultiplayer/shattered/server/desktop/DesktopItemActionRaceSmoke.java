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
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.watabou.noosa.Game;
import com.watabou.plugins.PluginManifest;
import com.watabou.utils.FileUtils;
import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import io.github.pixeldungeonmultiplayer.shattered.server.network.SendData;
import io.github.pixeldungeonmultiplayer.shattered.server.network.Server;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.InventoryRebuildAction;
import io.github.pixeldungeonmultiplayer.shattered.testclient.SimulatedClient;

import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

public final class DesktopItemActionRaceSmoke {
    private static final long TIMEOUT = 10_000;
    private static final AtomicReference<Throwable> failure = new AtomicReference<>();

    public static void main(String[] args) throws Exception {
        Game.version = System.getProperty("Specification-Version", "ItemActionRaceSmoke");
        Game.versionCode = Integer.parseInt(System.getProperty("Implementation-Version", "1"));
        int port;
        try (ServerSocket socket = new ServerSocket(0)) { port = socket.getLocalPort(); }
        String base = "build" + File.separator + "itemActionRaceSmoke" + File.separator + System.currentTimeMillis() + File.separator;
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(720, 400);
        config.setPreferencesConfig(base, Files.FileType.Local);
        SPDSettings.set(new Lwjgl3Preferences(new Lwjgl3FileHandle(base + SPDSettings.DEFAULT_PREFS_FILE, Files.FileType.Local)));
        SPDSettings.put("server_uuid", "item-action-race-smoke");
        SPDSettings.put("server_port", port);
        SPDSettings.put("online_mode", false);
        SPDSettings.put("max_players", 1);
        FileUtils.setDefaultFileProperties(Files.FileType.Local, base);
        new Lwjgl3Application(new RaceGame(new TestDesktopPlatformSupport(), port), config);
        if (failure.get() != null) {
            failure.get().printStackTrace();
            System.exit(1);
        }
    }

    private static final class RaceGame extends ShatteredPixelDungeon {
        private final int port;
        private RaceGame(TestDesktopPlatformSupport platform, int port) { super(platform); this.port = port; }
        @Override public void create() { super.create(); Thread thread = new Thread(this::run, "item-action-race"); thread.setDaemon(true); thread.start(); }

        private void run() {
            BlockingBag bag = new BlockingBag();
            Thread actor = null;
            try {
                waitFor(() -> Server.started, "server did not start");
                try (SimulatedClient client = new SimulatedClient().connect(socket())) {
                    client.parseNext();
                    AtomicBoolean ready = new AtomicBoolean();
                    client.afterAction("interlevel_scene", (c, a) -> { if ("fade_out".equals(a.optString("state"))) ready.set(true); });
                    client.join("warrior", "");
                    waitClient(client, ready::get, "scene was not initialized");
                    setupBag(bag);
                    waitClient(client, () -> client.inventory() != null && client.inventory().itemsByPath().containsKey(pathOf(bag)), "bag was not rebuilt");

                    RaceItem item = new RaceItem();
                    client.beforeAction("item_update", (c, a) -> bag.allowAdd.countDown());
                    actor = new Thread(() -> item.collect(bag), "test-actor");
                    actor.start();
                    require(bag.added.await(TIMEOUT, TimeUnit.MILLISECONDS), "item was not internally added");
                    runOnRender(() -> { item.setNeedUpdateVisual(true); GameScene.setUpdateItemDisplays(hero()); });
                    long deadline = System.currentTimeMillis() + TIMEOUT;
                    while (System.currentTimeMillis() < deadline) {
                        try { client.parseNext(); } catch (SocketTimeoutException ignored) { }
                    }
                    throw new AssertionError("GameScene did not send item_update during the collect race");
                }
            } catch (Throwable t) {
                failure.set(t);
            } finally {
                bag.allowAdd.countDown();
                if (actor != null) try { actor.join(TIMEOUT); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
                Server.stopServer();
                Gdx.app.exit();
            }
        }

        private void setupBag(BlockingBag bag) throws InterruptedException {
            runOnRender(() -> { Hero hero = hero(); require(hero.belongings.backpack.addItemDirect(bag), "could not add bag"); bag.owner = hero; SendData.packAndSendAction(hero, new InventoryRebuildAction(hero)); SendData.forceFlush(hero); });
        }
        private List<Integer> pathOf(Item item) { List<Integer> path = hero().belongings.pathOfItem(item); require(path != null, "no item path"); return path; }
        private Hero hero() { for (Hero hero : Dungeon.heroes) if (hero != null) return hero; throw new AssertionError("no hero"); }
        private Socket socket() throws Exception { Socket socket = new Socket("127.0.0.1", port); socket.setSoTimeout(500); return socket; }
        private void waitClient(SimulatedClient c, BooleanSupplier condition, String message) throws Exception { long end = System.currentTimeMillis()+TIMEOUT; while (!condition.getAsBoolean() && System.currentTimeMillis()<end) try { c.parseNext(); } catch (SocketTimeoutException ignored) {} require(condition.getAsBoolean(), message); }
        private void waitFor(BooleanSupplier condition, String message) throws InterruptedException { long end=System.currentTimeMillis()+TIMEOUT; while (!condition.getAsBoolean() && System.currentTimeMillis()<end) Thread.sleep(20); require(condition.getAsBoolean(), message); }
        private void runOnRender(ThrowingRunnable operation) throws InterruptedException { AtomicReference<Throwable> error=new AtomicReference<>(); CountDownLatch done=new CountDownLatch(1); Game.runOnRenderThread(() -> { try { operation.run(); } catch(Throwable t) { error.set(t); } finally { done.countDown(); }}); require(done.await(TIMEOUT, TimeUnit.MILLISECONDS), "render operation timed out"); if(error.get()!=null) throw new AssertionError("render operation failed", error.get()); }
    }

    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
    @FunctionalInterface private interface ThrowingRunnable { void run() throws Exception; }

    private static final class RaceItem extends Item { @Override public LocalizedString name() { return LocalizedString.raw("race-item"); } }
    private static final class BlockingBag extends Bag {
        private final CountDownLatch added = new CountDownLatch(1);
        private final CountDownLatch allowAdd = new CountDownLatch(1);
        @Override public boolean addItemDirect(Item item) { boolean result=super.addItemDirect(item); if (result) { added.countDown(); try { allowAdd.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } } return result; }
        @Override public boolean canHold(Item item) { return item instanceof RaceItem && super.canHold(item); }
        @Override public Icons getBagIcon() { return Icons.BACKPACK; }
        @Override public LocalizedString name() { return LocalizedString.raw("race-bag"); }
    }
    private static final class TestDesktopPlatformSupport extends DesktopPlatformSupport {
        @Override public List<PluginManifest> loadPlugins() { return new ArrayList<>(); }
        @Override public void registerService(int port, Map<String,String> properties) { }
        @Override public void updateService(Map<String,String> properties) { }
        @Override public void unregisterService() { }
    }
}
