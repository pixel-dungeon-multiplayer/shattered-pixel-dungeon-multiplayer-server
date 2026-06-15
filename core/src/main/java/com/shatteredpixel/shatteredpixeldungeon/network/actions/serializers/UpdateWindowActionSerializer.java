package com.shatteredpixel.shatteredpixeldungeon.network.actions.serializers;

import com.shatteredpixel.shatteredpixeldungeon.network.actions.UpdateWindowAction;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class UpdateWindowActionSerializer extends NetworkActionSerializer<UpdateWindowAction> {

    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull UpdateWindowAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        Object serialized = ctx.serialize(obj.window);
        if (serialized instanceof JSONObject) {
            return (JSONObject) serialized;
        }
        return null;
    }
}
