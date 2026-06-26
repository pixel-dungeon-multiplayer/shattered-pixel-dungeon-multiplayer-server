package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.EmitterBurstAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.emitters.BaseEmitterSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class EmitterBurstActionSerializer extends NetworkActionSerializer<EmitterBurstAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull EmitterBurstAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = BaseEmitterSerializer.baseObject("emitter_burst", obj.emitter, ctx);
        return actionObj != null ? actionObj : new JSONObject();
    }
}
