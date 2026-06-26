package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.UpdateFloorInfoAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class UpdateFloorInfoActionSerializer extends NetworkActionSerializer<UpdateFloorInfoAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull UpdateFloorInfoAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = new JSONObject();
        actionObj.put("depth", obj.depth);
        actionObj.put("branch", obj.branch);
        actionObj.put("feeling", obj.feeling.name());
        return actionObj;
    }
}
