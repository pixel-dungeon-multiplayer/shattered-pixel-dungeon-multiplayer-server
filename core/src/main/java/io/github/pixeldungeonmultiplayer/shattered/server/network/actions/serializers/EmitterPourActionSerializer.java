package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.EmitterPourAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.emitters.BaseEmitterSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class EmitterPourActionSerializer extends NetworkActionSerializer<EmitterPourAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull EmitterPourAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = BaseEmitterSerializer.baseObject("emitter_pour", obj.emitter, ctx);
        if (actionObj != null) {
            actionObj.put("id", obj.emitter.networkId());
            return actionObj;
        }
        return new JSONObject();
    }
}
