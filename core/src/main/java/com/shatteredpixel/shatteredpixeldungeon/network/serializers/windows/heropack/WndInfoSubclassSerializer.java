package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.heropack;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.dtos.TalentState;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.WindowSerializer;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoSubclass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

public class WndInfoSubclassSerializer extends WindowSerializer<WndInfoSubclass> {

    @Override
    protected @NotNull String type() {
        return "info_subclass";
    }

    @Override
    protected @Nullable JSONObject args(@NotNull WndInfoSubclass obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject args = new JSONObject();
        args.put("title", ctx.serialize(obj.subClass().title(), profile));
        args.put("description", ctx.serialize(obj.subClass().desc(), profile));
        
        JSONArray talents = new JSONArray();
        for (Talent talent : obj.subclassTalents().keySet()) {
            talents.put(ctx.serialize(new TalentState(talent, obj.subclassTalents().get(talent), obj.getOwnerHero()), profile));
        }
        args.put("talents", talents);
        return args;
    }
}
