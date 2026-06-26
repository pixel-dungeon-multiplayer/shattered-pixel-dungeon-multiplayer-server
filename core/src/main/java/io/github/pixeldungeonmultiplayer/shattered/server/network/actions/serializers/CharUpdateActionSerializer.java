package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.CharUpdateAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class CharUpdateActionSerializer extends NetworkActionSerializer<CharUpdateAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull CharUpdateAction action, @NotNull SerializationContext ctx, @NotNull String profile) {
        Object serialized = ctx.serialize(action.character, "default");
        if (serialized instanceof JSONObject) {
            return (JSONObject) serialized;
        }
        return new JSONObject();
    }
}
