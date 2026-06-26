package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.ResumeButtonVisibleAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class ResumeButtonVisibleActionSerializer extends NetworkActionSerializer<ResumeButtonVisibleAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull ResumeButtonVisibleAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = new JSONObject();
        actionObj.put("visible", obj.visible);
        return actionObj;
    }
}
