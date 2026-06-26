package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import org.jetbrains.annotations.NotNull;

public class TrapRemoveAction implements ImmutableNetworkAction {
    public final int pos;

    public TrapRemoveAction(int pos) {
        this.pos = pos;
    }

    @Override
    public @NotNull String actionName() {
        return "trap_remove";
    }
}
