package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.HideWindowAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class HideWindowActionSerializer extends NetworkActionSerializer<HideWindowAction> {

    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull HideWindowAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject res =  new JSONObject();
        res.put("id", obj.wndId);
        return res;
    }
}
