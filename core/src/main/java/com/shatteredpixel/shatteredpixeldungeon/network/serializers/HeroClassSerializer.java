package com.shatteredpixel.shatteredpixeldungeon.network.serializers;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.dtos.TalentState;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class HeroClassSerializer implements Serializer<HeroClass> {

    @Override
    public @NotNull JSONObject serialize(@NotNull HeroClass obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject json = new JSONObject();
        json.put("id", obj.name());
        json.put("title", ctx.serialize(obj.title(), "default"));
        json.put("short_description", ctx.serialize(obj.shortDesc(), "default"));
        json.put("description", ctx.serialize(obj.desc(), "default"));
        json.put("spritesheet", obj.spritesheet());
        json.put("splash_art", obj.splashArt());
        json.put("unlocked", obj.isUnlocked());
        json.put("unlock_message", ctx.serialize(obj.unlockMsg(), "default"));

        JSONArray subclasses = new JSONArray();
        for (HeroSubClass subClass : obj.subClasses()) {
            subclasses.put(ctx.serialize(subClass, "default"));
        }
        json.put("subclasses", subclasses);

        JSONArray abilities = new JSONArray();
        for (ArmorAbility ability : obj.armorAbilities()) {
            abilities.put(ctx.serialize(ability, "default"));
        }
        json.put("armor_abilities", abilities);

        JSONArray talentTiers = new JSONArray();
        ArrayList<LinkedHashMap<Talent, Integer>> classTalents = new ArrayList<>();
        Talent.initClassTalents(obj, classTalents);
        classTalents.get(2).clear();
        for (int i = 0; i < classTalents.size(); i++) {
            JSONObject tier = new JSONObject();
            tier.put("tier", i + 1);
            JSONArray talents = new JSONArray();
            for (Talent talent : classTalents.get(i).keySet()) {
                talents.put(ctx.serialize(new TalentState(talent, 0), "default"));
            }
            tier.put("talents", talents);
            talentTiers.put(tier);
        }
        json.put("talent_tiers", talentTiers);

        return json;
    }
}
