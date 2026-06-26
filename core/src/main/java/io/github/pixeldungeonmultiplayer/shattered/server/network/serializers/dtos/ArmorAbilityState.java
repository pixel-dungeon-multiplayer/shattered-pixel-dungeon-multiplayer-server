package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.dtos;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import org.jetbrains.annotations.NotNull;

public class ArmorAbilityState {
    public final @NotNull ArmorAbility ability;
    public final @NotNull Hero hero;

    public ArmorAbilityState(@NotNull ArmorAbility ability, @NotNull Hero hero) {
        this.ability = ability;
        this.hero = hero;
    }
}
