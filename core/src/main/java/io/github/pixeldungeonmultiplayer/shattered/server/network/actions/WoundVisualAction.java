package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class WoundVisualAction implements ImmutableNetworkAction {
    public final int pos;
    public final float timeToFade;

    @Contract(pure = true)
    public WoundVisualAction(int pos, float timeToFade) {
        this.pos = pos;
        this.timeToFade = timeToFade;
    }

    @Override
    @Contract(pure = true)
    public @NotNull String actionName() {
        return "wound_visual";
    }
}
