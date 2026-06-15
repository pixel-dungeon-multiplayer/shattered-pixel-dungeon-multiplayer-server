package com.shatteredpixel.shatteredpixeldungeon.network.actions.serializers;

import com.nikita22007.multiplayer.utils.text.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.network.actions.ChatMessagesAction;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

public class ChatMessagesActionSerializer extends NetworkActionSerializer<ChatMessagesAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull ChatMessagesAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = new JSONObject();
        JSONArray messages = new JSONArray();
        for (LocalizedString text : obj.messages()) {
            JSONObject messageObj = new JSONObject();
            messageObj.put("text", ctx.serialize(text, profile));
            messages.put(messageObj);
        }
        actionObj.put("messages", messages);
        return actionObj;
    }
}
