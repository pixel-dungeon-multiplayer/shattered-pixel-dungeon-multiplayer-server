package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class ActorRemoveAction implements ImmutableNetworkAction {
    public final int actorId;

    @Contract(pure = true)
    public ActorRemoveAction(int actorId) {
        this.actorId = actorId;
    }

    public ActorRemoveAction(@NotNull Actor actor) {
        this(actor.id());
    }

    @Override
    public @NotNull String actionName() {
        return "actor_remove";
    }
}
