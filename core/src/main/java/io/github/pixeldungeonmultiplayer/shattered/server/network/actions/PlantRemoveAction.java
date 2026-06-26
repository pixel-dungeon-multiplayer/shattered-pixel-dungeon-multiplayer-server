package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import org.jetbrains.annotations.NotNull;

public class PlantRemoveAction implements ImmutableNetworkAction {
    public final int pos;

    public PlantRemoveAction(int pos) {
        this.pos = pos;
    }

    @Override
    public @NotNull String actionName() {
        return "plant_remove";
    }
}
