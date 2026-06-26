package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class HeroReadyAction implements ImmutableNetworkAction {
    public final boolean ready;

    @Contract(pure = true)
    public HeroReadyAction(boolean ready) {
        this.ready = ready;
    }

    @Override
    @Contract(pure = true)
    public @NotNull String actionName() {
        return "hero_ready";
    }
}
