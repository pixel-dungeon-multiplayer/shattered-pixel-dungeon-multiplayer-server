package com.shatteredpixel.shatteredpixeldungeon.network.serializers.dtos;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TalentState {
    public final @NotNull Talent talent;
    public final int points;
    public final @Nullable Hero hero;

    public TalentState(@NotNull Talent talent, int points) {
        this(talent, points, null);
    }

    public TalentState(@NotNull Talent talent, int points, @Nullable Hero hero) {
        this.talent = talent;
        this.points = points;
        this.hero = hero;
    }
}
