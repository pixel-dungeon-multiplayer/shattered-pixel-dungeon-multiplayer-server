package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.RedirectServerAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class RedirectServerActionSerializer extends NetworkActionSerializer<RedirectServerAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull RedirectServerAction action, @NotNull SerializationContext ctx, @NotNull String profile) {
        return action.redirectPacket.toJSON();
    }
}
