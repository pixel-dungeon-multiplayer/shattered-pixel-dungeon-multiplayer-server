package io.github.pixeldungeonmultiplayer.shattered.server.noosa;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import io.github.pixeldungeonmultiplayer.shattered.server.network.SendData;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.ShakeCameraAction;
import org.jetbrains.annotations.Nullable;

public class Camera {
    public static void shake(float magnitude, float duration) {
        shake(magnitude, duration, null);
    }

    public static void shake(float magnitude, float duration, @Nullable Hero heroForVisual) {
        ShakeCameraAction action = new ShakeCameraAction(magnitude, duration);
        if (heroForVisual != null) {
            SendData.sendAction(heroForVisual, action);
        } else {
            SendData.sendActionForAll(action);
        }
    }
}

