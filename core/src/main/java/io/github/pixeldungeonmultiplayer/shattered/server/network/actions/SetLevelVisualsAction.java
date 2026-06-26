package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class SetLevelVisualsAction implements ImmutableNetworkAction {
    public final String tilesTexture;
    public final String waterTexture;
    public final String feeling;

    @Contract(pure = true)
    public SetLevelVisualsAction(@NotNull Level level) {
        this.tilesTexture = level.tilesTex();
        this.waterTexture = level.waterTex();
        this.feeling = level.feeling.name();
    }

    @Contract(pure = true)
    public SetLevelVisualsAction(String tilesTexture, String waterTexture, String feeling) {
        this.tilesTexture = tilesTexture;
        this.waterTexture = waterTexture;
        this.feeling = feeling;
    }

    @Override
    public @NotNull String actionName() {
        return "set_level_visuals";
    }
}
