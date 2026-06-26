package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class ShakeCameraAction implements ImmutableNetworkAction {
    public final float magnitude;
    public final float duration;

    @Contract(pure = true)
    public ShakeCameraAction(float magnitude, float duration) {
        this.magnitude = magnitude;
        this.duration = duration;
    }

    @Override
    @Contract(pure = true)
    public @NotNull String actionName() {
        return "shake_camera";
    }
}
