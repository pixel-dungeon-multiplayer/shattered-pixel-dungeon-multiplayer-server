package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.heropack;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.WindowSerializer;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndHero;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;

public class WndHeroSerializer extends WindowSerializer<WndHero> {

    @Override
    protected @NotNull String type() {
        return "hero";
    }

    @Override
    protected @Nullable JSONObject args(@NotNull WndHero obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject args = new JSONObject();
        args.put("selected_tab", obj.selectedTabIndex());
        args.put("owner_hero", ctx.serialize(obj.getOwnerHero(), profile));

        // Dynamic stats
        JSONArray statsArray = new JSONArray();
        for (WndHero.Stat stat : obj.stats()) {
            JSONObject statObj = new JSONObject();
            statObj.put("label", ctx.serialize(stat.label, profile));
            statObj.put("value", stat.value);
            statsArray.put(statObj);
        }
        args.put("stats", statsArray);

        // Talents
        JSONArray tiers = new JSONArray();
        Hero hero = obj.getOwnerHero();
        for (int i = 0; i < hero.talents.size(); i++) {
            JSONObject tierObj = new JSONObject();
            tierObj.put("tier", i + 1);
            tierObj.put("points_available", hero.talentPointsAvailable(i + 1));
            tierObj.put("points_spent", hero.talentPointsSpent(i + 1));

            JSONArray talents = new JSONArray();
            LinkedHashMap<Talent, Integer> tierTalents = hero.talents.get(i);
            for (Talent talent : tierTalents.keySet()) {
                JSONObject talentObj = new JSONObject();
                talentObj.put("id", talent.name());
                talentObj.put("title", ctx.serialize(talent.title(), profile));
                talentObj.put("description", ctx.serialize(talent.desc(), profile));
                talentObj.put("points", tierTalents.get(talent));
                talentObj.put("max_points", talent.maxPoints());
                talents.put(talentObj);
            }
            tierObj.put("talents", talents);
            tiers.put(tierObj);
        }
        args.put("talent_tiers", tiers);

        // Buffs
        JSONArray buffs = new JSONArray();
        for (Buff buff : hero.buffs()) {
            if (buff.icon() != 0) { // BuffIndicator.NONE
                JSONObject buffObj = new JSONObject();
                buffObj.put("class", buff.getClass().getName());
                buffObj.put("name", ctx.serialize(buff.name(), profile));
                buffObj.put("description", ctx.serialize(buff.desc(), profile));
                buffs.put(buffObj);
            }
        }
        args.put("buffs", buffs);

        return args;
    }
}



