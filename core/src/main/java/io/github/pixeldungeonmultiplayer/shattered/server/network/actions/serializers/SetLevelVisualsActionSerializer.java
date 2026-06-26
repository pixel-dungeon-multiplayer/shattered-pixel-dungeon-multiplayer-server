package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.SetLevelVisualsAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class SetLevelVisualsActionSerializer extends NetworkActionSerializer<SetLevelVisualsAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull SetLevelVisualsAction action, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject obj = new JSONObject();
        obj.put("tiles_texture", action.tilesTexture);
        obj.put("water_texture", action.waterTexture);
        obj.put("feeling", action.feeling);
        return obj;
    }
}
