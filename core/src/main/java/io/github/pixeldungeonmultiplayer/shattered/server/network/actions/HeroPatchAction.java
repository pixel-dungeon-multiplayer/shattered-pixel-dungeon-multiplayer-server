package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public abstract class HeroPatchAction implements ImmutableNetworkAction {
    @Override
    @Contract(pure = true)
    public final @NotNull String actionName() {
        return "hero_patch";
    }
}
