package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import org.jetbrains.annotations.NotNull;

public class TrapUpdateAction implements ImmutableNetworkAction {
    public final int pos;
    public final int shape;
    public final int color;
    public final boolean active;

    public TrapUpdateAction(int pos, @NotNull Trap trap) {
        this.pos = pos;
        this.shape = trap.shape;
        this.color = trap.color;
        this.active = trap.isActive();
    }

    @Override
    public @NotNull String actionName() {
        return "trap_update";
    }
}
