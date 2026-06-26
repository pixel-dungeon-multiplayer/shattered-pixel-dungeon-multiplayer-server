package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import org.jetbrains.annotations.NotNull;
import java.util.Arrays;

public class SetLevelTilesAction implements ImmutableNetworkAction {
    public final int[] tiles;

    public SetLevelTilesAction(@NotNull Level level) {
        this.tiles = Arrays.copyOf(level.map, level.map.length);
    }

    public SetLevelTilesAction(int[] tiles) {
        this.tiles = tiles;
    }

    @Override
    public @NotNull String actionName() {
        return "set_level_tiles";
    }
}
