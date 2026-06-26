package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.GameSceneFlashAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class GameSceneFlashActionSerializer extends NetworkActionSerializer<GameSceneFlashAction> {
    @Override
    public @Nullable JSONObject serializeInternal(@NotNull GameSceneFlashAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject object  = new JSONObject();
        object.put("color", obj.color);
        object.put("light", obj.light);
        return object;
    }
}
