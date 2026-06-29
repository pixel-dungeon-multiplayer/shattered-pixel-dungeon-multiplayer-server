package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import com.shatteredpixel.shatteredpixeldungeon.effects.Lightning;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class LightningVisualAction implements ImmutableNetworkAction {
    public final @NotNull List<Lightning.Arc> arcs;
    public final float duration;

    @Contract(pure = true)
    public LightningVisualAction(@NotNull List<Lightning.Arc> arcs, float duration) {
        this.arcs = arcs;
        this.duration = duration;
    }

    @Override
    @Contract(pure = true)
    public @NotNull String actionName() {
        return "lightning_visual";
    }
}
