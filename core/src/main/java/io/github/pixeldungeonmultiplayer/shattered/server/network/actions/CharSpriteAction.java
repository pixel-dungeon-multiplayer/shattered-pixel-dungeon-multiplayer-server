package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CharSpriteAction implements ImmutableNetworkAction {
    public final int actorId;
    public final String action;
    @Nullable
    public final Integer from;
    @Nullable
    public final Integer to;

    @Contract(pure = true)
    public CharSpriteAction(int actorId, @NotNull String action, @Nullable Integer from, @Nullable Integer to) {
        this.actorId = actorId;
        this.action = action;
        this.from = from;
        this.to = to;
    }

    @Override
    @Contract(pure = true)
    public @NotNull String actionName() {
        return "sprite_action";
    }
}
