package com.shatteredpixel.shatteredpixeldungeon.android;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.os.Build;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.android.AndroidPreferences;
import com.badlogic.gdx.backends.android.DefaultAndroidFiles;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.watabou.noosa.Game;
import com.watabou.utils.FileUtils;

import java.lang.reflect.Proxy;

public final class AndroidHeadlessServerLauncher {

    private static HeadlessApplication application;

    @SuppressWarnings("deprecation")
    public static synchronized void launch(Context context) {
        if (application != null) {
            return;
        }

        try {
            Game.version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            Game.version = "Headless-Android-Local";
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Game.versionCode = (int) context.getPackageManager().getPackageInfo(context.getPackageName(), 0).getLongVersionCode();
            } else {
                Game.versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            }
        } catch (Exception e) {
            Game.versionCode = 1;
        }

        HeadlessApplicationConfiguration configuration = new HeadlessApplicationConfiguration();
        configuration.updatesPerSecond = 60;

        application = new HeadlessApplication(new HeadlessServer(context), configuration);
    }

    public static synchronized void stop() {
        if (application != null) {
            application.exit();
            application = null;
        }
    }

    private static final class HeadlessServer extends ShatteredPixelDungeon {
        private final Context context;

        private HeadlessServer(Context context) {
            super(new AndroidHeadlessPlatformSupport(context));
            this.context = context.getApplicationContext();
        }

        @Override
        public void create() {
            Gdx.files = new DefaultAndroidFiles(context.getAssets(), new ContextWrapper(context), true);
            installNoOpOpenGl();

            Game.width = 720;
            Game.height = 400;

            SharedPreferences sharedPrefs = context.getSharedPreferences("ShatteredPixelDungeonServer", Context.MODE_PRIVATE);
            Preferences preferences = new AndroidPreferences(sharedPrefs);
            SPDSettings.set(preferences);

            FileUtils.setDefaultFileProperties(com.badlogic.gdx.Files.FileType.External, "");

            super.create();
        }

        @Override
        public void render() {
            step();
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
