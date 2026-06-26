package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.EmitterStopAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class EmitterStopActionSerializer extends NetworkActionSerializer<EmitterStopAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull EmitterStopAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = new JSONObject();
        actionObj.put("id", obj.id);
        return actionObj;
    }
}
