package io.github.pixeldungeonmultiplayer.shattered.server.effects;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import io.github.pixeldungeonmultiplayer.shattered.server.network.SendData;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.SurpriseVisualAction;
import org.jetbrains.annotations.Contract;

public class Surprise {

    private static final float TIME_TO_FADE = 1f;

    @Deprecated
    @Contract(value = " -> fail", pure = true)
    private Surprise() {
        throw new RuntimeException();
    }

    public static void reset(int p, float angle) {
        SendData.sendActionForAll(new SurpriseVisualAction(p, angle, TIME_TO_FADE));
    }

    public static void hit(Char ch) {
        hit(ch, 0);
    }

    public static void hit(Char ch, float angle) {
        reset(ch.pos, angle);
    }

    public static void hit(int pos) {
        reset(pos, 0);
    }
}
