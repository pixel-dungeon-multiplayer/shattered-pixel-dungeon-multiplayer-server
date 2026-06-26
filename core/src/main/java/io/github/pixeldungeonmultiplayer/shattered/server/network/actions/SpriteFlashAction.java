package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class SpriteFlashAction implements ImmutableNetworkAction {
    public final int actorId;
    public final float flashTime;

    @Contract(pure = true)
    public SpriteFlashAction(int actorId, float flashTime) {
        this.actorId = actorId;
        this.flashTime = flashTime;
    }

    @Override
    @Contract(pure = true)
    public @NotNull String actionName() {
        return "sprite_flash";
    }
}
