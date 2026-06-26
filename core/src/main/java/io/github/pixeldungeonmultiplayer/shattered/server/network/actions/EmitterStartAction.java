package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import com.shatteredpixel.shatteredpixeldungeon.particles.Emitter;
import org.jetbrains.annotations.NotNull;

public class EmitterStartAction implements LiveStateNetworkAction {
    public final Emitter emitter;

    public EmitterStartAction(@NotNull Emitter emitter) {
        this.emitter = emitter;
    }

    @Override
    public @NotNull String actionName() {
        return "emitter_start";
    }
}
