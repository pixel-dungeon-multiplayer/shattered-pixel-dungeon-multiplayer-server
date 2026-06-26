package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.UpdateWindowAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class UpdateWindowActionSerializer extends NetworkActionSerializer<UpdateWindowAction> {

    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull UpdateWindowAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        Object serialized = ctx.serialize(obj.window);
        if (serialized instanceof JSONObject) {
            return (JSONObject) serialized;
        }
        return null;
    }
}
