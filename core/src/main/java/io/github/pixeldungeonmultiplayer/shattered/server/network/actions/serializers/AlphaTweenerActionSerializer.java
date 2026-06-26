package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.AlphaTweenerAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class AlphaTweenerActionSerializer extends NetworkActionSerializer<AlphaTweenerAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull AlphaTweenerAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = new JSONObject();
        actionObj.put("action", "alpha_tweener");
        actionObj.put("actor_id", obj.actorId);
        actionObj.put("target_alpha", obj.targetAlpha);
        actionObj.put("interval", obj.interval);
        return actionObj;
    }
}
