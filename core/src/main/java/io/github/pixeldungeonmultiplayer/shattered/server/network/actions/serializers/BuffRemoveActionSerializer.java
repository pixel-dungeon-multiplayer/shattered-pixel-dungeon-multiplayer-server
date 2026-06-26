package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.BuffRemoveAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class BuffRemoveActionSerializer extends NetworkActionSerializer<BuffRemoveAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull BuffRemoveAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject buffObj = new JSONObject();
        buffObj.put("id", obj.buffId);
        return buffObj;
    }
}
