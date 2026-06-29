package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.ShowFloatingTextAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class ShowFloatingTextActionSerializer extends NetworkActionSerializer<ShowFloatingTextAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull ShowFloatingTextAction action, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject obj = new JSONObject();
        obj.put("anchor", ctx.serialize(action.anchor, profile));
        obj.put("text", ctx.serialize(action.text, profile));
        obj.put("color", action.color);
        obj.put("icon", action.icon);
        obj.put("left", action.left);
        return obj;
    }
}
