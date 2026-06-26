package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.EmitterStartAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.emitters.BaseEmitterSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class EmitterStartActionSerializer extends NetworkActionSerializer<EmitterStartAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull EmitterStartAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = BaseEmitterSerializer.baseObject("emitter_start", obj.emitter, ctx);
        return actionObj != null ? actionObj : new JSONObject();
    }
}
