package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.LockedFloorStateAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class LockedFloorStateActionSerializer extends NetworkActionSerializer<LockedFloorStateAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull LockedFloorStateAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = new JSONObject();
        actionObj.put("locked", obj.locked);
        return actionObj;
    }
}
