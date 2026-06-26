package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.ShowStatusAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class ShowStatusActionSerializer extends NetworkActionSerializer<ShowStatusAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull ShowStatusAction action, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject obj = new JSONObject();
        if (action.x != null) {
            obj.put("x", action.x);
        }
        if (action.y != null) {
            obj.put("y", action.y);
        }
        if (action.key != null) {
            obj.put("key", action.key);
        }
        obj.put("text", action.text);
        obj.put("color", action.color);
        obj.put("ignore_position", action.ignorePosition);
        return obj;
    }
}
