package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.EnchantingVisualAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class EnchantingVisualActionSerializer extends NetworkActionSerializer<EnchantingVisualAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull EnchantingVisualAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = new JSONObject();
        actionObj.put("target", obj.targetId);
        actionObj.put("item", ctx.serialize(obj.item, "inventory"));
        return actionObj;
    }
}
