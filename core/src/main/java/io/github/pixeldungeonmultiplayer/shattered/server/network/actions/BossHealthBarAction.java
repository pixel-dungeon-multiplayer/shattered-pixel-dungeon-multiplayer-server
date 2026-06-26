package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class BossHealthBarAction implements ImmutableNetworkAction {
    public final int id;
    public final boolean bleeding;

    @Contract(pure = true)
    public BossHealthBarAction(int id, boolean bleeding) {
        this.id = id;
        this.bleeding = bleeding;
    }

    @Override
    @Contract(pure = true)
    public @NotNull String actionName() {
        return "boss_health_bar";
    }
}
