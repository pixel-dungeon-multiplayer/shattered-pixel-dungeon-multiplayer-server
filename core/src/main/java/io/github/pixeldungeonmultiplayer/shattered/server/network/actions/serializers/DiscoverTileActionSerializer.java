package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.DiscoverTileAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class DiscoverTileActionSerializer extends NetworkActionSerializer<DiscoverTileAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull DiscoverTileAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = new JSONObject();
        actionObj.put("pos", obj.pos);
        actionObj.put("old_tile", obj.oldValue);
        return actionObj;
    }
}
