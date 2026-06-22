package com.shatteredpixel.shatteredpixeldungeon.network.actions;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class HeroSubclassAction extends HeroPatchAction {
    public final @NotNull HeroSubClass subclass;

    @Contract(pure = true)
    public HeroSubclassAction(@NotNull HeroSubClass subclass) {
        this.subclass = subclass;
    }
}
