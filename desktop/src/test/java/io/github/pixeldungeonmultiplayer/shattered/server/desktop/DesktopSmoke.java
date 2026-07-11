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
import com.watabou.noosa.Game;
import com.watabou.plugins.PluginManifest;
import com.watabou.utils.FileUtils;
import io.github.pixeldungeonmultiplayer.shattered.server.network.Server;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import io.github.pixeldungeonmultiplayer.shattered.testclient.ClientInventory;
import io.github.pixeldungeonmultiplayer.shattered.testclient.ClientItem;
import io.github.pixeldungeonmultiplayer.shattered.testclient.SimulatedClient;
import org.json.JSONArray;
import org.json.JSONObject;

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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

abstract class DesktopSmoke extends ShatteredPixelDungeon {

    protected static final long TIMEOUT_MILLIS = 20_000;
    private static final AtomicReference<Throwable> FAILURE = new AtomicReference<>();

    protected final int port;

    static void launch(String name, SmokeFactory factory) throws Exception {
        launch(name, 1, factory);
    }

    static void launch(String name, int maxPlayers, SmokeFactory factory) throws Exception {
        Game.version = System.getProperty("Specification-Version", name);
        Game.versionCode = Integer.parseInt(System.getProperty("Implementation-Version", "1"));

        int port = reservePort();
        String basePath = "build" + File.separator + name + File.separator + System.currentTimeMillis() + File.separator;
        File directory = new File(basePath);
        require(directory.mkdirs() || directory.isDirectory(), "could not create test directory: " + directory);

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SPDMP " + name);
        config.setWindowedMode(720, 400);
        config.setPreferencesConfig(basePath, Files.FileType.Local);

        SPDSettings.set(new Lwjgl3Preferences(
                new Lwjgl3FileHandle(basePath + SPDSettings.DEFAULT_PREFS_FILE, Files.FileType.Local)
        ));
        SPDSettings.put("server_uuid", "desktop-" + name + "-server");
        SPDSettings.put("server_port", port);
        SPDSettings.put("online_mode", false);
        SPDSettings.put("max_players", maxPlayers);
        SPDSettings.put("server_name", "SPDMP " + name + " Test Server");
        SPDSettings.put("motd", "");
        FileUtils.setDefaultFileProperties(Files.FileType.Local, basePath);

        new Lwjgl3Application(factory.create(port), config);
        Throwable failure = FAILURE.getAndSet(null);
        if (failure != null) {
            failure.printStackTrace();
            System.exit(1);
        }
    }

    protected DesktopSmoke(int port) {
        super(new TestDesktopPlatformSupport());
        this.port = port;
    }

    @Override
    public final void create() {
        super.create();
        Thread testThread = new Thread(this::runSmoke, smokeThreadName());
        testThread.setDaemon(true);
        testThread.start();
    }

    private void runSmoke() {
        try {
            runTest();
        } catch (Throwable t) {
            FAILURE.set(t);
        } finally {
            Server.stopServer();
            Gdx.app.exit();
        }
    }

    protected abstract String smokeThreadName();

    protected abstract void runTest() throws Exception;

    protected final void waitForServer() throws InterruptedException {
        waitFor(() -> Server.started, "server did not start");
    }

    protected final SimulatedClient connectClient() throws IOException {
        Socket socket = new Socket("127.0.0.1", port);
        socket.setSoTimeout(500);
        return new SimulatedClient().connect(socket);
    }

    protected final void joinAndWaitForScene(SimulatedClient client) throws IOException {
        AtomicReference<Boolean> sceneReady = new AtomicReference<>(false);
        client.parseNext();
        client.afterAction("interlevel_scene", (ignored, action) -> {
            if ("fade_out".equals(action.optString("state", ""))) {
                sceneReady.set(true);
            }
        });
        client.join("warrior", "");
        waitForClient(client, () -> sceneReady.get(), "game scene was not initialized");
    }

    protected final void waitForClient(SimulatedClient client, BooleanSupplier condition, String message) throws IOException {
        long deadline = System.currentTimeMillis() + TIMEOUT_MILLIS;
        while (!condition.getAsBoolean() && System.currentTimeMillis() < deadline) {
            try {
                client.parseNext();
            } catch (SocketTimeoutException ignored) {
            }
        }
        require(condition.getAsBoolean(), message);
    }

    protected final void waitFor(BooleanSupplier condition, String message) throws InterruptedException {
        long deadline = System.currentTimeMillis() + TIMEOUT_MILLIS;
        while (!condition.getAsBoolean() && System.currentTimeMillis() < deadline) {
            Thread.sleep(20);
        }
        require(condition.getAsBoolean(), message);
    }

    protected final void onGameThread(ThrowingRunnable operation) throws InterruptedException {
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
        if (error.get() != null) {
            throw new AssertionError("game thread operation failed", error.get());
        }
    }

    protected final Hero hero() {
        if (Dungeon.heroes != null) {
            for (Hero hero : Dungeon.heroes) {
                if (hero != null) {
                    return hero;
                }
            }
        }
        throw new AssertionError("server has no joined hero");
    }

    protected final ClientInventory serverInventory() {
        Hero hero = hero();
        Object serialized = new SerializationContext(Server.SERIALIZERS, hero).serialize(hero.belongings, "rebuild");
        return ClientInventory.fromJson((JSONObject) serialized);
    }

    protected final void assertInventoryMatches(ClientInventory expected, ClientInventory actual) {
        require(actual != null, "client inventory was not built");
        require(expected.itemsByPath().keySet().equals(actual.itemsByPath().keySet()),
                "inventory paths differ: expected=" + expected.itemsByPath().keySet()
                        + ", actual=" + actual.itemsByPath().keySet());
        for (Map.Entry<List<Integer>, ClientItem> entry : expected.itemsByPath().entrySet()) {
            ClientItem actualItem = actual.itemAt(entry.getKey());
            require(actualItem != null, "missing client item at path " + entry.getKey());
            ClientItem expectedItem = entry.getValue();
            require(jsonValueEquals(expectedItem.name, actualItem.name), "item name mismatch at " + entry.getKey());
            require(expectedItem.image == actualItem.image, "item image mismatch at " + entry.getKey());
            require(expectedItem.quantity == actualItem.quantity, "item quantity mismatch at " + entry.getKey());
        }
    }

    protected static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static int reservePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        }
    }

    private static boolean jsonValueEquals(Object expected, Object actual) {
        if (expected instanceof JSONObject && actual instanceof JSONObject) {
            return ((JSONObject) expected).similar(actual);
        }
        if (expected instanceof JSONArray && actual instanceof JSONArray) {
            return ((JSONArray) expected).similar(actual);
        }
        return Objects.equals(expected, actual);
    }

    @FunctionalInterface
    interface SmokeFactory {
        DesktopSmoke create(int port);
    }

    @FunctionalInterface
    protected interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static final class TestDesktopPlatformSupport extends DesktopPlatformSupport {
        @Override public List<PluginManifest> loadPlugins() { return new ArrayList<>(); }
        @Override public void registerService(int port, Map<String, String> properties) { }
        @Override public void updateService(Map<String, String> properties) { }
        @Override public void unregisterService() { }
    }
}
