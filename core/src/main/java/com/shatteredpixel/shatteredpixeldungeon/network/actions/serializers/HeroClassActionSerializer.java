package com.shatteredpixel.shatteredpixeldungeon.network.actions.serializers;

import com.shatteredpixel.shatteredpixeldungeon.network.actions.HeroClassAction;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class HeroClassActionSerializer extends NetworkActionSerializer<HeroClassAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull HeroClassAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = new JSONObject();
        actionObj.put("class", ctx.serialize(obj.heroClass, "default"));
        return actionObj;
    }
}
