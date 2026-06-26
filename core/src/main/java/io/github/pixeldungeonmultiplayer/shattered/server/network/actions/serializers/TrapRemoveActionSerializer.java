package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.TrapRemoveAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class TrapRemoveActionSerializer extends NetworkActionSerializer<TrapRemoveAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull TrapRemoveAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject trapObj = new JSONObject();
        trapObj.put("pos", obj.pos);
        return trapObj;
    }
}
