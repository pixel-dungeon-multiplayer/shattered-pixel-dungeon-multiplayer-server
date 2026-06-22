package com.shatteredpixel.shatteredpixeldungeon.network.serializers;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.dtos.TalentState;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.dtos.ArmorAbilityState;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

public class ArmorAbilityStateSerializer implements Serializer<ArmorAbilityState> {

    @Override
    public @NotNull JSONObject serialize(@NotNull ArmorAbilityState obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject json = (JSONObject) ctx.serialize(obj.ability, "default");
        json.put("short_description", ctx.serialize(obj.ability.shortDesc(obj.hero), "default"));
        json.put("description", ctx.serialize(obj.ability.desc(obj.hero), "default"));
        JSONArray talents = new JSONArray();
        for (Talent talent : obj.ability.talents()) {
            talents.put(ctx.serialize(new TalentState(talent, obj.hero.pointsInTalent(talent), obj.hero), "default"));
        }
        json.put("talents", talents);
        return json;
    }
}
