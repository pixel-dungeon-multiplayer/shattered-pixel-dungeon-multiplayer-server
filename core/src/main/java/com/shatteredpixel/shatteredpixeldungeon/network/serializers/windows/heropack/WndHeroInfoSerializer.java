package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.heropack;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.WindowSerializer;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndHeroInfo;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.watabou.utils.DeviceCompat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class WndHeroInfoSerializer extends WindowSerializer<WndHeroInfo> {

    @Override
    protected @NotNull String type() {
        return "hero_info";
    }

    @Override
    protected @Nullable JSONObject args(@NotNull WndHeroInfo obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject args = new JSONObject();
        HeroClass heroClass = obj.heroClass();
        args.put("selected_tab", obj.selectedTabIndex());
        args.put("hero_class", heroClass.name());
        args.put("title", ctx.serialize(Messages.titleCase(heroClass.title()), profile));
        args.put("description", ctx.serialize(heroClass.desc(), profile));

        // Tab availability based on badges/achievements
        args.put("subclass_unlocked", Badges.isUnlocked(Badges.Badge.BOSS_SLAIN_2) || DeviceCompat.isDebug());
        args.put("ability_unlocked", Badges.isUnlocked(Badges.Badge.BOSS_SLAIN_4) || DeviceCompat.isDebug());

        // Subclasses
        JSONArray subclasses = new JSONArray();
        for (HeroSubClass subClass : heroClass.subClasses()) {
            JSONObject subObj = new JSONObject();
            subObj.put("id", subClass.name());
            subObj.put("title", ctx.serialize(subClass.title(), profile));
            subObj.put("short_description", ctx.serialize(subClass.shortDesc(), profile));
            subclasses.put(subObj);
        }
        args.put("subclasses", subclasses);

        // Armor abilities
        JSONArray abilities = new JSONArray();
        for (ArmorAbility ability : heroClass.armorAbilities()) {
            JSONObject abilityObj = new JSONObject();
            abilityObj.put("id", ability.getClass().getName());
            abilityObj.put("name", ctx.serialize(ability.name(), profile));
            abilityObj.put("short_description", ctx.serialize(ability.shortDesc(), profile));
            abilities.put(abilityObj);
        }
        args.put("abilities", abilities);

        // Talents
        JSONArray tiers = new JSONArray();
        ArrayList<LinkedHashMap<Talent, Integer>> classTalents = new ArrayList<>();
        Talent.initClassTalents(heroClass, classTalents);
        classTalents.get(2).clear(); // remove T3 talents, as in original

        for (int i = 0; i < classTalents.size(); i++) {
            JSONObject tierObj = new JSONObject();
            tierObj.put("tier", i + 1);

            JSONArray talents = new JSONArray();
            LinkedHashMap<Talent, Integer> tierTalents = classTalents.get(i);
            for (Talent talent : tierTalents.keySet()) {
                JSONObject talentObj = new JSONObject();
                talentObj.put("id", talent.name());
                talentObj.put("title", ctx.serialize(talent.title(), profile));
                talentObj.put("description", ctx.serialize(talent.desc(), profile));
                talentObj.put("points", obj.getOwnerHero().pointsInTalent(talent));
                talentObj.put("max_points", talent.maxPoints());
                talents.put(talentObj);
            }
            tierObj.put("talents", talents);
            tiers.put(tierObj);
        }
        args.put("talent_tiers", tiers);

        return args;
    }
}



