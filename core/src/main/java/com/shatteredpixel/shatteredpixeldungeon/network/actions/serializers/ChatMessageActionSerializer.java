package com.shatteredpixel.shatteredpixeldungeon.network.actions.serializers;

import com.shatteredpixel.shatteredpixeldungeon.network.actions.ChatMessageAction;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class ChatMessageActionSerializer extends NetworkActionSerializer<ChatMessageAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull ChatMessageAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = new JSONObject();
        actionObj.put("text", ctx.serialize(obj.text, profile));
        return actionObj;
    }
}
