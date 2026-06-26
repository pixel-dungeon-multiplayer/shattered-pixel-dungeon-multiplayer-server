package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.ChatMessageAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
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
