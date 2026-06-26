package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import org.jetbrains.annotations.NotNull;

public class ResizeLevelAction implements ImmutableNetworkAction {
    public final int width;
    public final int height;

    public ResizeLevelAction(@NotNull Level level) {
        this.width = level.width();
        this.height = level.height();
    }

    public ResizeLevelAction(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public @NotNull String actionName() {
        return "resize_level";
    }
}
