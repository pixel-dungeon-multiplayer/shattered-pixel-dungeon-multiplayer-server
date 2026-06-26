package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class HeroClassAction extends HeroPatchAction {
    public final @NotNull HeroClass heroClass;

    @Contract(pure = true)
    public HeroClassAction(@NotNull HeroClass heroClass) {
        this.heroClass = heroClass;
    }
}
