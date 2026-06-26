package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import org.jetbrains.annotations.Contract;

public class HeroStrengthAction extends HeroPatchAction {
    public final int strength;

    @Contract(pure = true)
    public HeroStrengthAction(int strength) {
        this.strength = strength;
    }
}
