package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class HideWindowAction implements ImmutableNetworkAction {
    public final int wndId;

    @Contract(pure = true)
    public HideWindowAction(int id) {
        wndId = id;
    }

    @Override
    public @NotNull String actionName() {
        return "hide_window";
    }
}
