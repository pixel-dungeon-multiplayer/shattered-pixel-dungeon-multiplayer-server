package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.CharEmoAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class CharEmoActionSerializer extends NetworkActionSerializer<CharEmoAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull CharEmoAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = new JSONObject();
        actionObj.put("actor_id", obj.actorId);
        actionObj.put("emotion", obj.emotion == null ? JSONObject.NULL : obj.emotion);
        return actionObj;
    }
}
