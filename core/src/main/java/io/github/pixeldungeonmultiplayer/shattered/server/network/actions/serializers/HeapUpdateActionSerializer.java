package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.HeapUpdateAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class HeapUpdateActionSerializer extends NetworkActionSerializer<HeapUpdateAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull HeapUpdateAction action, @NotNull SerializationContext ctx, @NotNull String profile) {
        Object serialized = ctx.serialize(action.heap, "default");
        if (serialized instanceof JSONObject) {
            return (JSONObject) serialized;
        }
        return new JSONObject();
    }
}
