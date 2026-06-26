package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import org.jetbrains.annotations.NotNull;

public class SetLevelStatesAction implements ImmutableNetworkAction {
    public final int[] states;

    public SetLevelStatesAction(@NotNull Level level) {
        this.states = new int[level.length()];
        for (int i = 0; i < level.length(); i++) {
            int state = 0; // UNVISITED
            if (level.visited[i]) state = 1; // VISITED
            else if (level.mapped[i]) state = 2; // MAPPED
            this.states[i] = state;
        }
    }

    public SetLevelStatesAction(int[] states) {
        this.states = states;
    }

    @Override
    public @NotNull String actionName() {
        return "set_level_states";
    }
}
