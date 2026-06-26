package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.ShakeCameraAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class ShakeCameraActionSerializer extends NetworkActionSerializer<ShakeCameraAction> {
    @Override
    public @Nullable JSONObject serializeInternal(@NotNull ShakeCameraAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject object = new JSONObject();
        object.put("magnitude", obj.magnitude);
        object.put("duration", obj.duration);
        return object;
    }
}
