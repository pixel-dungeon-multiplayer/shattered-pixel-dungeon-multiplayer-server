package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class UpdateFloorInfoAction implements ImmutableNetworkAction {
    public final int depth;
    public final int branch;
    public final Level.Feeling feeling;

    @Contract(pure = true)
    public UpdateFloorInfoAction(int depth, int branch, Level.Feeling feeling) {
        this.depth = depth;
        this.branch = branch;
        this.feeling = feeling;
    }

    @Override
    public @NotNull String actionName() {
        return "update_floor_info";
    }
}
