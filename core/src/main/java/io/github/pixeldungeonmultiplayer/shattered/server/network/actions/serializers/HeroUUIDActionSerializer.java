package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.HeroUUIDAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class HeroUUIDActionSerializer extends NetworkActionSerializer<HeroUUIDAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull HeroUUIDAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = new JSONObject();
        actionObj.put("uuid", obj.uuid);
        return actionObj;
    }
}
