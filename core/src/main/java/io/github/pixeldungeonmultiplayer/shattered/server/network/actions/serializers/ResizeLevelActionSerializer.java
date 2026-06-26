package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.ResizeLevelAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class ResizeLevelActionSerializer extends NetworkActionSerializer<ResizeLevelAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull ResizeLevelAction action, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject obj = new JSONObject();
        obj.put("width", action.width);
        obj.put("height", action.height);
        return obj;
    }
}
