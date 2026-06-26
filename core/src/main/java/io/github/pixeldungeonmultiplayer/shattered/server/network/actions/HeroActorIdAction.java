package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class HeroActorIdAction implements ImmutableNetworkAction {
    public final int actorId;

    @Contract(pure = true)
    public HeroActorIdAction(int actorId) {
        this.actorId = actorId;
    }

    @Override
    @Contract(pure = true)
    public @NotNull String actionName() {
        return "change_hero_actor_id";
    }
}
