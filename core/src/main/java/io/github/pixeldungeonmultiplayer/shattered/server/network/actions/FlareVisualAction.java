package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FlareVisualAction implements ImmutableNetworkAction {
    public final int pos;
    @Nullable
    public final Float positionX;
    @Nullable
    public final Float positionY;
    public final int color;
    public final float duration;
    public final boolean lightMode;
    public final int rays;
    public final float radius;
    public final float angle;
    public final float angularSpeed;

    @Contract(pure = true)
    public FlareVisualAction(int pos, int color, float duration, boolean lightMode, int rays, float radius, float angle, float angularSpeed) {
        this.pos = pos;
        this.positionX = null;
        this.positionY = null;
        this.color = color;
        this.duration = duration;
        this.lightMode = lightMode;
        this.rays = rays;
        this.radius = radius;
        this.angle = angle;
        this.angularSpeed = angularSpeed;
    }

    @Contract(pure = true)
    public FlareVisualAction(float positionX, float positionY, int color, float duration, boolean lightMode, int rays, float radius, float angle, float angularSpeed) {
        this.pos = -1;
        this.positionX = positionX;
        this.positionY = positionY;
        this.color = color;
        this.duration = duration;
        this.lightMode = lightMode;
        this.rays = rays;
        this.radius = radius;
        this.angle = angle;
        this.angularSpeed = angularSpeed;
    }

    @Override
    @Contract(pure = true)
    public @NotNull String actionName() {
        return "flare_visual";
    }
}
