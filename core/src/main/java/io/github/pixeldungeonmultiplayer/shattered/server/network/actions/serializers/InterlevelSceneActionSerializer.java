package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.InterlevelSceneAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class InterlevelSceneActionSerializer extends NetworkActionSerializer<InterlevelSceneAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull InterlevelSceneAction scene, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject sceneObj = new JSONObject();
        if (scene.state != null) {
            sceneObj.put("state", scene.state);
        }
        if (scene.mode != null) {
            sceneObj.put("type", scene.mode.name().toLowerCase());
        }
        if (scene.customMessage != null) {
            sceneObj.put("custom_message", scene.customMessage.toJsonObject());
        }
        if (scene.scrollSpeed != null) {
            sceneObj.put("scroll_speed", scene.scrollSpeed);
        }
        if (scene.loadingTexture != null) {
            sceneObj.put("loading_texture", scene.loadingTexture);
        }
        if (scene.fadeTime != null) {
            sceneObj.put("fade_time", scene.fadeTime.name().toLowerCase());
        }
        if (scene.resetLevel) {
            sceneObj.put("reset_level", true);
        }
        return sceneObj;
    }
}
