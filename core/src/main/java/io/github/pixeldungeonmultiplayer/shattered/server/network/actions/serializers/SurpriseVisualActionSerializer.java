package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.SurpriseVisualAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class SurpriseVisualActionSerializer extends NetworkActionSerializer<SurpriseVisualAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull SurpriseVisualAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = new JSONObject();
        actionObj.put("action_name", "surprise_visual");
        actionObj.put("pos", obj.pos);
        actionObj.put("angle", obj.angle);
        actionObj.put("time_to_fade", obj.timeToFade);
        return actionObj;
    }
}
