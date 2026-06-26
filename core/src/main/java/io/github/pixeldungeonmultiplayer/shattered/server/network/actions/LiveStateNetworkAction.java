package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface LiveStateNetworkAction {
    @Contract(pure = true)
    @NotNull String actionName();
}
