package io.github.pixeldungeonmultiplayer.shattered.server.headless;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.watabou.noosa.Game;
import com.watabou.utils.FileUtils;
import io.github.pixeldungeonmultiplayer.shattered.server.network.Server;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;

/** Experimental launcher for running the complete server without a window. */
public final class HeadlessDesktopLauncher {

    private HeadlessDesktopLauncher() {
    }

    public static void main(String[] args) {
		installExceptionHandler();
        Game.version = System.getProperty("Specification-Version", "Headless-local");
        Game.versionCode = Integer.parseInt(System.getProperty("Implementation-Version", "1"));

        HeadlessApplicationConfiguration configuration = new HeadlessApplicationConfiguration();
        configuration.updatesPerSecond = Integer.getInteger("spd.headless.ups", 60);
        new HeadlessApplication(new HeadlessServer(), configuration);
    }

	private static void installExceptionHandler() {
		AtomicBoolean terminating = new AtomicBoolean();
		Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
			PrintStream error = System.err;
			error.println("Fatal uncaught exception in thread \"" + thread.getName() + "\"");
			throwable.printStackTrace(error);
			error.flush();
			if (terminating.compareAndSet(false, true)) {
				System.exit(1);
			} else {
				Runtime.getRuntime().halt(1);
			}
		});
	}

    private static final class HeadlessServer extends ShatteredPixelDungeon {

        private HeadlessServer() {
            super(new HeadlessPlatformSupport());
        }

        @Override
        public void create() {
            installNoOpOpenGl();
			Game.width = positiveIntProperty("spd.virtualWidth", 720);
			Game.height = positiveIntProperty("spd.virtualHeight", 400);
			System.out.println("Headless virtual resolution: " + Game.width + "x" + Game.height);
			String dataDirectory = System.getProperty("spd.dataDir", "headless-data/");
			String configFileName = System.getProperty("spd.configFile",
					new File(dataDirectory, "config.json").getPath());
			FileHandle configFile = fileHandle(configFileName);
			Preferences preferences = new JsonPreferences(configFile);
            SPDSettings.set(preferences);
			System.out.println("Headless server configuration: " + configFile.file().getAbsolutePath());
            applySystemProperties();
			File dataDirectoryFile = new File(dataDirectory);
			String dataPath = dataDirectoryFile.getPath();
			if (!dataPath.endsWith(File.separator)) dataPath += File.separator;
			FileUtils.setDefaultFileProperties(
					dataDirectoryFile.isAbsolute() ? Files.FileType.Absolute : Files.FileType.Local,
					dataPath);
            System.out.println("Starting headless SPDMP server on port " + SPDSettings.serverPort());
            super.create();
			if (!Server.started) {
				System.err.println("Headless SPDMP startup aborted because the server socket could not be opened");
				System.err.flush();
				System.exit(1);
			}
        }

		private static int positiveIntProperty(String name, int fallback) {
			int value = Integer.getInteger(name, fallback);
			if (value <= 0) {
				throw new IllegalArgumentException(name + " must be greater than zero, got " + value);
			}
			return value;
		}

		private static FileHandle fileHandle(String path) {
			return new File(path).isAbsolute() ? Gdx.files.absolute(path) : Gdx.files.local(path);
		}

        @Override
        public void render() {
            // The headless backend still drives the regular game update loop, but
            // there is no OpenGL context in which scenes could be drawn.
            step();
        }

        private static void applySystemProperties() {
            putInt("spd.port", "server_port");
            putInt("spd.maxPlayers", SPDSettings.KEY_MAX_PLAYERS);
            putBoolean("spd.onlineMode", "online_mode");
            putString("spd.serverName", SPDSettings.KEY_SERVER_NAME);
            putString("spd.serverUuid", "server_uuid");
            putString("spd.motd", "motd");
			putBoolean("spd.heroInvulnerable", SPDSettings.KEY_HERO_INVULNERABLE);
			putBoolean("spd.heroGodStrike", SPDSettings.KEY_HERO_GOD_STRIKE);
        }

        private static void putInt(String property, String setting) {
            String value = System.getProperty(property);
            if (value != null) SPDSettings.put(setting, Integer.parseInt(value));
        }

        private static void putBoolean(String property, String setting) {
            String value = System.getProperty(property);
            if (value != null) SPDSettings.put(setting, Boolean.parseBoolean(value));
        }

        private static void putString(String property, String setting) {
            String value = System.getProperty(property);
            if (value != null) SPDSettings.put(setting, value);
        }

        private static void installNoOpOpenGl() {
            GL20 gl = (GL20) Proxy.newProxyInstance(GL20.class.getClassLoader(), new Class<?>[]{GL20.class},
                    (proxy, method, arguments) -> {
                        Class<?> type = method.getReturnType();
                        if (type == boolean.class) return false;
                        if (type == int.class) return 0;
                        if (type == float.class) return 0f;
                        if (type == long.class) return 0L;
                        return null;
                    });
            Gdx.gl = gl;
            Gdx.gl20 = gl;
        }
    }
}
