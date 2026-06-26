package io.github.pixeldungeonmultiplayer.shattered.server.network;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers.NetworkActionSerializer;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class SerializedActionSerializer extends NetworkActionSerializer<NetworkPacket.SerializedAction> {

    @Override
    protected @Nullable JSONObject serializeInternal(NetworkPacket.@NotNull SerializedAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        return obj.actionObj();
    }
}
