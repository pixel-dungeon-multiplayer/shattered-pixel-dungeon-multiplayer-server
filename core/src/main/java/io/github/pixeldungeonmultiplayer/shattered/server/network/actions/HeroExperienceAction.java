package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import org.jetbrains.annotations.Contract;

public class HeroExperienceAction extends HeroPatchAction {
    public final int lvl;
    public final int exp;

    @Contract(pure = true)
    public HeroExperienceAction(int lvl, int exp) {
        this.lvl = lvl;
        this.exp = exp;
    }
}
