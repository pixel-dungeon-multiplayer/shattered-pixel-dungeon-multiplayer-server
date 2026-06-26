package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import org.jetbrains.annotations.NotNull;

public class RippleVisualAction implements ImmutableNetworkAction {
    public final int pos;

    public RippleVisualAction(int pos) {
        this.pos = pos;
    }

    @Override
    public @NotNull String actionName() {
        return "ripple_visual";
    }
}
