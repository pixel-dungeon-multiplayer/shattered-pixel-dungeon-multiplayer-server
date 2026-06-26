package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.InventoryRebuildAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import io.github.pixeldungeonmultiplayer.shattered.server.network.Server;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class InventoryRebuildActionSerializer extends NetworkActionSerializer<InventoryRebuildAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull InventoryRebuildAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        SerializationContext innerCtx = new SerializationContext(Server.SERIALIZERS, obj.hero);
        Object payload = innerCtx.serialize(obj.hero.belongings, "rebuild");

        if (payload instanceof JSONObject) {
            return (JSONObject) payload;
        }
        return new JSONObject();
    }
}
