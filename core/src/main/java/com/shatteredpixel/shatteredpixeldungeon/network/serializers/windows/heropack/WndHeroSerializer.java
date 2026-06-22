package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.heropack;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.dtos.TalentState;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
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
        args.put("title", ctx.serialize(obj.title(), profile));
        args.put("owner_hero", ctx.serialize(obj.getOwnerHero(), profile));

        // Dynamic stats
        JSONArray statsArray = new JSONArray();
        for (WndHero.Stat stat : obj.stats()) {
            if (stat == null) {
                statsArray.put(JSONObject.NULL);
                continue;
            }
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
                talents.put(ctx.serialize(new TalentState(talent, tierTalents.get(talent), hero), profile));
            }
            tierObj.put("talents", talents);
            tiers.put(tierObj);
        }
        args.put("talent_tiers", tiers);

        // Buffs
        JSONArray buffs = new JSONArray();
        for (Buff buff : hero.buffs()) {
            if (buff.icon() != BuffIndicator.NONE) {
                JSONObject buffObj = new JSONObject();
                buffObj.put("class", buff.getClass().getName());
                buffObj.put("icon", buff.icon());
                buffObj.put("name", ctx.serialize(buff.name(), profile));
                buffObj.put("description", ctx.serialize(buff.desc(), profile));
                buffs.put(buffObj);
            }
        }
        args.put("buffs", buffs);

        return args;
    }
}
