package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.SpriteFlashAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class SpriteFlashActionSerializer extends NetworkActionSerializer<SpriteFlashAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull SpriteFlashAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = new JSONObject();
        actionObj.put("actor_id", obj.actorId);
        actionObj.put("flash_time", obj.flashTime);
        return actionObj;
    }
}
