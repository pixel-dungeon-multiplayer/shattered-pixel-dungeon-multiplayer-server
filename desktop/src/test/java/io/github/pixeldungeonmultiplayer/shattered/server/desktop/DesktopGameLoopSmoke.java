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
import io.github.pixeldungeonmultiplayer.shattered.server.network.Protocol;
import io.github.pixeldungeonmultiplayer.shattered.server.network.Server;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import io.github.pixeldungeonmultiplayer.shattered.testclient.ClientInventory;
import io.github.pixeldungeonmultiplayer.shattered.testclient.ClientItem;
import io.github.pixeldungeonmultiplayer.shattered.testclient.ClientState;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class DesktopGameLoopSmoke {

    private static final String SERVER_UUID = "desktop-game-loop-smoke-server";
    private static final AtomicReference<Throwable> failure = new AtomicReference<>();

    private DesktopGameLoopSmoke() {
    }

    public static void main(String[] args) throws Exception {
        Game.version = System.getProperty("Specification-Version", "GameLoopSmoke");
        Game.versionCode = Integer.parseInt(System.getProperty("Implementation-Version", "1"));

        int port = reservePort();
        String basePath = "build" + File.separator
                + "gameLoopSmoke" + File.separator
                + Long.toString(System.currentTimeMillis()) + File.separator;
        File testDir = new File(basePath);
        if (!testDir.mkdirs() && !testDir.isDirectory()) {
            throw new IOException("Could not create test directory: " + testDir);
        }

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SPDMP Game Loop Smoke");
        config.setWindowedMode(720, 400);
        config.setPreferencesConfig(basePath, Files.FileType.Local);

        SPDSettings.set(new Lwjgl3Preferences(
                new Lwjgl3FileHandle(basePath + SPDSettings.DEFAULT_PREFS_FILE, Files.FileType.Local)
        ));
        SPDSettings.put("server_uuid", SERVER_UUID);
        SPDSettings.put("server_port", port);
        SPDSettings.put("online_mode", false);
        SPDSettings.put("max_players", 2);
        SPDSettings.put("server_name", "SPDMP Test Server");
        SPDSettings.put("motd", "");
        FileUtils.setDefaultFileProperties(Files.FileType.Local, basePath);

        new Lwjgl3Application(new SmokeGame(new TestDesktopPlatformSupport(), port), config);

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

    private static final class SmokeGame extends ShatteredPixelDungeon {
        private final int port;

        private SmokeGame(TestDesktopPlatformSupport platform, int port) {
            super(platform);
            this.port = port;
        }

        @Override
        public void create() {
            super.create();
            Thread testThread = new Thread(this::runSmoke, "SPDMP game-loop smoke client");
            testThread.setDaemon(true);
            testThread.start();
        }

        private void runSmoke() {
            try {
                waitForServer();
                runHandshakeSmoke();
                runJoinInventorySmoke();
            } catch (Throwable t) {
                failure.set(t);
            } finally {
                Server.stopServer();
                Gdx.app.exit();
            }
        }

        private void runHandshakeSmoke() throws IOException {
            try (SimulatedClient client = new SimulatedClient().connect(connectClientSocket())) {
                client.parseNext();
                require(client.state() == ClientState.HELLO_RECEIVED, "client did not receive hello");
                require(Protocol.NAME.equals(client.protocolName()), "unexpected protocol");
                require(Protocol.VERSION == client.protocolVersion(), "unexpected protocol version");
                require(SERVER_UUID.equals(client.serverId()), "unexpected server id");
            }
        }

        private void runJoinInventorySmoke() throws IOException {
            AtomicBoolean checkedInventory = new AtomicBoolean(false);
            try (SimulatedClient client = new SimulatedClient().connect(connectClientSocket())) {
                client.parseNext();
                client.afterAction("interlevel_scene", (c, action) -> {
                    if ("fade_out".equals(action.optString("state", ""))) {
                        compareInventory(serverInventory(), c.inventory());
                        checkedInventory.set(true);
                    }
                });
                client.join("warrior", "");

                long deadline = System.currentTimeMillis() + 20_000;
                while (!checkedInventory.get() && System.currentTimeMillis() < deadline) {
                    try {
                        client.parseNext();
                    } catch (SocketTimeoutException ignored) {
                    }
                }
                require(checkedInventory.get(), "did not receive interlevel fade_out before timeout");
            }
        }

        private Socket connectClientSocket() throws IOException {
            Socket socket = new Socket("127.0.0.1", port);
            socket.setSoTimeout(1_000);
            return socket;
        }

        private void waitForServer() throws InterruptedException {
            long deadline = System.currentTimeMillis() + 10_000;
            while (!Server.started && System.currentTimeMillis() < deadline) {
                Thread.sleep(50);
            }
            require(Server.started, "server did not start");
        }

        private void require(boolean condition, String message) {
            if (!condition) {
                throw new AssertionError(message);
            }
        }

        private ClientInventory serverInventory() {
            Hero hero = firstHero();
            Object serialized = new SerializationContext(Server.SERIALIZERS, hero)
                    .serialize(hero.belongings, "rebuild");
            return ClientInventory.fromJson((JSONObject) serialized);
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

        private void compareInventory(ClientInventory serverInventory, ClientInventory clientInventory) {
            require(clientInventory != null, "client inventory was not built");
            require(
                    serverInventory.itemsByPath().size() == clientInventory.itemsByPath().size(),
                    "inventory path count mismatch"
            );
            for (Map.Entry<List<Integer>, ClientItem> entry : serverInventory.itemsByPath().entrySet()) {
                ClientItem expected = entry.getValue();
                ClientItem actual = clientInventory.itemAt(entry.getKey());
                require(actual != null, "missing client item at path " + entry.getKey());
                require(jsonValueEquals(expected.name, actual.name), "item name mismatch at path " + entry.getKey());
                require(expected.quantity == actual.quantity, "item quantity mismatch at path " + entry.getKey());
                require(expected.image == actual.image, "item image mismatch at path " + entry.getKey());
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
