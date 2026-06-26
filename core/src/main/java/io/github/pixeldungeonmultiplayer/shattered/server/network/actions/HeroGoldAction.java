package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class HeroGoldAction implements ImmutableNetworkAction {
    public final int gold;

    @Contract(pure = true)
    public HeroGoldAction(int gold) {
        this.gold = gold;
    }

    @Override
    @Contract(pure = true)
    public @NotNull String actionName() {
        return "hero_gold";
    }
}
