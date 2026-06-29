package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.BossHealthBarAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class BossHealthBarActionSerializer extends NetworkActionSerializer<BossHealthBarAction> {
    @Override
    @Contract(pure = true)
    protected @Nullable JSONObject serializeInternal(@NotNull BossHealthBarAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = new JSONObject();
        actionObj.put("id", obj.id == null? JSONObject.NULL: obj.id );
        actionObj.put("bleeding", obj.bleeding);
        return actionObj;
    }
}
