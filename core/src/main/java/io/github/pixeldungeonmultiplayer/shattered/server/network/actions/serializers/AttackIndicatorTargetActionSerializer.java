package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.AttackIndicatorTargetAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class AttackIndicatorTargetActionSerializer extends NetworkActionSerializer<AttackIndicatorTargetAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull AttackIndicatorTargetAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = new JSONObject();
        actionObj.put("target", obj.target);
        return actionObj;
    }
}
