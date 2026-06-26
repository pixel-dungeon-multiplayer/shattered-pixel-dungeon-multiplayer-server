package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class LockedFloorStateAction implements ImmutableNetworkAction {
    public final boolean locked;

    @Contract(pure = true)
    public LockedFloorStateAction(boolean locked) {
        this.locked = locked;
    }

    @Override
    public @NotNull String actionName() {
        return "locked_floor_state";
    }
}
