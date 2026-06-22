package com.shatteredpixel.shatteredpixeldungeon.network.serializers;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class TalentSerializer implements Serializer<Talent> {

    @Override
    public @NotNull JSONObject serialize(@NotNull Talent obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject json = new JSONObject();
        json.put("id", obj.name());
        json.put("title", ctx.serialize(obj.title(), profile));
        json.put("description", ctx.serialize(obj.desc(), profile));
        json.put("metamorph_description", ctx.serialize(obj.desc(true), profile));
        json.put("icon", obj.icon());
        json.put("max_points", obj.maxPoints());
        return json;
    }

}
