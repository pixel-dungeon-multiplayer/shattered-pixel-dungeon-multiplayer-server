package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.MagicMissileVisualAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class MagicMissileVisualActionSerializer extends NetworkActionSerializer<MagicMissileVisualAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull MagicMissileVisualAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = new JSONObject();
        actionObj.put("type", obj.type);
        actionObj.put("from", obj.from);
        actionObj.put("to", obj.to);
        return actionObj;
    }
}
