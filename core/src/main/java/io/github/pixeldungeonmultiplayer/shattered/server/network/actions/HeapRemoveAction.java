package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class HeapRemoveAction implements ImmutableNetworkAction {
    public final int pos;

    @Contract(pure = true)
    public HeapRemoveAction(int pos) {
        this.pos = pos;
    }

    @Override
    @Contract(pure = true)
    public @NotNull String actionName() {
        return "heap_remove";
    }
}
