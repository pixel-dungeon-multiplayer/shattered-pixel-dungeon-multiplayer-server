package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.WoundVisualAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class WoundVisualActionSerializer extends NetworkActionSerializer<WoundVisualAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull WoundVisualAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = new JSONObject();
        actionObj.put("pos", obj.pos);
        actionObj.put("time_to_fade", obj.timeToFade);
        return actionObj;
    }
}
