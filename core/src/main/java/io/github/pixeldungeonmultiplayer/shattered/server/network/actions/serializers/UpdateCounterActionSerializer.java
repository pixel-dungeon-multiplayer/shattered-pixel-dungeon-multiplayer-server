package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.UpdateCounterAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class UpdateCounterActionSerializer extends NetworkActionSerializer<UpdateCounterAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull UpdateCounterAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = new JSONObject();
        actionObj.put("counter", obj.counter);
        return actionObj;
    }
}
