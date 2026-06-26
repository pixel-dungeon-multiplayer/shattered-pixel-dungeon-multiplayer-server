package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import org.jetbrains.annotations.NotNull;

public class BuffRemoveAction implements ImmutableNetworkAction {
    public final int buffId;

    public BuffRemoveAction(int buffId) {
        this.buffId = buffId;
    }

    public BuffRemoveAction(@NotNull Buff buff) {
        this(buff.id());
    }

    @Override
    public @NotNull String actionName() {
        return "buff_remove";
    }
}
