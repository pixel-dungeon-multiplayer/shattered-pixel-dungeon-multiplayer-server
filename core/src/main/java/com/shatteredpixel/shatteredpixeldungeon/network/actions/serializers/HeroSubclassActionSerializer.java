package com.shatteredpixel.shatteredpixeldungeon.network.actions.serializers;

import com.shatteredpixel.shatteredpixeldungeon.network.actions.HeroSubclassAction;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class HeroSubclassActionSerializer extends NetworkActionSerializer<HeroSubclassAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull HeroSubclassAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = new JSONObject();
        actionObj.put("subclass", ctx.serialize(obj.subclass, "default"));
        return actionObj;
    }
}
