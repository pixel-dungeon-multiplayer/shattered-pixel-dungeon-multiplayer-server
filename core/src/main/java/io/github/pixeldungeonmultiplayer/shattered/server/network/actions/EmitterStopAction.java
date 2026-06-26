package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import org.jetbrains.annotations.NotNull;

public class EmitterStopAction implements ImmutableNetworkAction {
    public final int id;

    public EmitterStopAction(int id) {
        this.id = id;
    }

    @Override
    public @NotNull String actionName() {
        return "emitter_stop";
    }
}
