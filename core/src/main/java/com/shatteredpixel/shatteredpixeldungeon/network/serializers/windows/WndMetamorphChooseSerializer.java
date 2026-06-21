package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;

public class WndMetamorphChooseSerializer extends WindowSerializer<ScrollOfMetamorphosis.WndMetamorphChoose> {

    @Override
    protected @NotNull String type() {
        return "metamorph_choose";
    }

    @Override
    protected @Nullable JSONObject args(@NotNull ScrollOfMetamorphosis.WndMetamorphChoose obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject args = new JSONObject();
        args.put("message", ctx.serialize(Messages.get(ScrollOfMetamorphosis.class, "choose_desc"), profile));
        JSONArray tiers = new JSONArray();
        int tierIndex = 1;
        for (LinkedHashMap<Talent, Integer> tier : obj.talents()) {
            JSONObject tierObj = new JSONObject();
            tierObj.put("tier", tierIndex++);
            JSONArray talents = new JSONArray();
            for (Talent talent : tier.keySet()) {
                JSONObject talentObj = new JSONObject();
                talentObj.put("id", talent.name());
                talentObj.put("title", ctx.serialize(talent.title(), profile));
                talentObj.put("points", tier.get(talent));
                talents.put(talentObj);
            }
            tierObj.put("talents", talents);
            tiers.put(tierObj);
        }
        args.put("tiers", tiers);
        return args;
    }
}



