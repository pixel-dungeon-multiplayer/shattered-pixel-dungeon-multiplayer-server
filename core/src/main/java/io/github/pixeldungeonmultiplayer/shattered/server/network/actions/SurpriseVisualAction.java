package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class SurpriseVisualAction implements ImmutableNetworkAction {
    public final int pos;
    public final float angle;
    public final float timeToFade;

    @Contract(pure = true)
    public SurpriseVisualAction(int pos, float angle, float timeToFade) {
        this.pos = pos;
        this.angle = angle;
        this.timeToFade = timeToFade;
    }

    @Override
    @Contract(pure = true)
    public @NotNull String actionName() {
        return "surprise_visual";
    }
}
