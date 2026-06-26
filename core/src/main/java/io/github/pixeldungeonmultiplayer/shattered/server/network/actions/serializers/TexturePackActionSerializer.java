package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.TexturePackAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class TexturePackActionSerializer extends NetworkActionSerializer<TexturePackAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull TexturePackAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = new JSONObject();
        actionObj.put("texturepack", obj.data);
        return actionObj;
    }
}
