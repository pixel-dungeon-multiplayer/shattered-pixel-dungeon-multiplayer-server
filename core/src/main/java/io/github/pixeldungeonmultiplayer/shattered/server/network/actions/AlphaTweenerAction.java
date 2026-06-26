package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class AlphaTweenerAction implements ImmutableNetworkAction {
    public final int actorId;
    public final float targetAlpha;
    public final float interval;

    @Contract(pure = true)
    public AlphaTweenerAction(int actorId, float targetAlpha, float interval) {
        this.actorId = actorId;
        this.targetAlpha = targetAlpha;
        this.interval = interval;
    }

    @Override
    @Contract(pure = true)
    public @NotNull String actionName() {
        return "sprite_action";
    }
}
