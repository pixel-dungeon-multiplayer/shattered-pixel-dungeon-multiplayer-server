package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UpdateCellsAction implements ImmutableNetworkAction {
    public final int @NotNull [] positions;
    public final int @Nullable [] tiles;
    public final int @Nullable [] states;

    @Contract(pure = true)
    public UpdateCellsAction(int @NotNull [] positions, int @Nullable [] tiles, int @Nullable [] states) {
        this.positions = positions;
        this.tiles = tiles;
        this.states = states;
    }

    @Contract(pure = true)
    public UpdateCellsAction(int cell, int tile, int state) {
        this.positions = new int[]{cell};
        this.tiles = new int[]{tile};
        this.states = new int[]{state};
    }

    @Contract(pure = true)
    public UpdateCellsAction(int cell, @NotNull Level level) {
        this.positions = new int[]{cell};
        this.tiles = new int[]{level.map[cell]};
        int state = 0;
        if (level.visited[cell]) state = 1;
        else if (level.mapped[cell]) state = 2;
        this.states = new int[]{state};
    }

    public UpdateCellsAction(@NotNull Level level, boolean @NotNull [] diff) {
        int count = 0;
        for (int i = 0; i < diff.length; i++) {
            if (diff[i]) {
                count++;
            }
        }

        this.positions = new int[count];
        this.tiles = new int[count];
        this.states = new int[count];

        int idx = 0;
        for (int i = 0; i < diff.length; i++) {
            if (diff[i]) {
                this.positions[idx] = i;
                this.tiles[idx] = level.map[i];

                int state = 0;
                if (level.visited[i]) state = 1;
                else if (level.mapped[i]) state = 2;
                this.states[idx] = state;

                idx++;
            }
        }
    }

    @Override
    public @NotNull String actionName() {
        return "update_cells";
    }
}
