package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.ChatMessagesAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
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
