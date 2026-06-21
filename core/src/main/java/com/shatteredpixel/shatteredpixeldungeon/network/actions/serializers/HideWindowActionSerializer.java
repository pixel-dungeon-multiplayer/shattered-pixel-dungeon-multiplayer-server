package com.shatteredpixel.shatteredpixeldungeon.network.actions.serializers;

import com.shatteredpixel.shatteredpixeldungeon.network.actions.HideWindowAction;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class HideWindowActionSerializer extends NetworkActionSerializer<HideWindowAction> {

    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull HideWindowAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject res =  new JSONObject();
        res.put("id", obj.wndId);
        return res;
    }
}
