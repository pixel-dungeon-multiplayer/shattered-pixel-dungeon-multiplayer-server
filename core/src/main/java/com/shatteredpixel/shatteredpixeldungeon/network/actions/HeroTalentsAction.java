package com.shatteredpixel.shatteredpixeldungeon.network.actions;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class HeroTalentsAction extends HeroPatchAction {
    public final ArrayList<LinkedHashMap<Talent, Integer>> talents;

    @Contract(pure = true)
    public HeroTalentsAction(ArrayList<LinkedHashMap<Talent, Integer>> talents) {
        this.talents = talents;
    }
}
