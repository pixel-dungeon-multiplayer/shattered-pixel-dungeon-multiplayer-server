package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import com.shatteredpixel.shatteredpixeldungeon.effects.BannerSprites;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class ShowBannerAction implements ImmutableNetworkAction {
    public final BannerSprites.Type banner;
    public final int color;
    public final float fadeTime;
    public final float showTime;

    @Contract(pure = true)
    public ShowBannerAction(@NotNull BannerSprites.Type banner, int color, float fadeTime, float showTime) {
        this.banner = banner;
        this.color = color;
        this.fadeTime = fadeTime;
        this.showTime = showTime;
    }

    @Override
    @Contract(pure = true)
    public @NotNull String actionName() {
        return "show_banner";
    }
}
