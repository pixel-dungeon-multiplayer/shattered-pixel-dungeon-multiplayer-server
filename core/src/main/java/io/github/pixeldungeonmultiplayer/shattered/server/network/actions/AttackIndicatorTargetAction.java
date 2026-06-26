package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class AttackIndicatorTargetAction implements ImmutableNetworkAction {
    public final int target;

    @Contract(pure = true)
    public AttackIndicatorTargetAction(int target) {
        this.target = target;
    }

    @Override
    public @NotNull String actionName() {
        return "attack_indicator_target";
    }
}
