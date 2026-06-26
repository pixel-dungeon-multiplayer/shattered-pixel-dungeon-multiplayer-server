package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.ActorRemoveAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class ActorRemoveActionSerializer extends NetworkActionSerializer<ActorRemoveAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull ActorRemoveAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject object = new JSONObject();
        object.put("id", obj.actorId);
        return object;
    }
}
