package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class MagicMissileVisualAction implements ImmutableNetworkAction {
    public final int type;
    public final int from;
    public final int to;

    @Contract(pure = true)
    public MagicMissileVisualAction(int type, int from, int to) {
        this.type = type;
        this.from = from;
        this.to = to;
    }

    @Override
    @Contract(pure = true)
    public @NotNull String actionName() {
        return "magic_missile_visual";
    }
}
