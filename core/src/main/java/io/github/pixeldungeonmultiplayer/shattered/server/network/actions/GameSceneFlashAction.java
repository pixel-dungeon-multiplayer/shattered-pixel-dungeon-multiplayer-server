package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class GameSceneFlashAction implements ImmutableNetworkAction {
    public final int color;
    public final boolean light;

    @Contract(pure = true)
    public GameSceneFlashAction(int color) {
        this(color, true);
    }

    @Contract(pure = true)
    public GameSceneFlashAction(int color, boolean light) {
        this.color = color;
        this.light = light;
    }

    @Override
    @Contract(pure = true)
    public @NotNull String actionName() {
        return "game_scene_flash";
    }

}
