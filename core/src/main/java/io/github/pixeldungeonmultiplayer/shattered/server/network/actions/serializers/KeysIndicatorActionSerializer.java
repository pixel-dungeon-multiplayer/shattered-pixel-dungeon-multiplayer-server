package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.KeysIndicatorAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

public class KeysIndicatorActionSerializer extends NetworkActionSerializer<KeysIndicatorAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull KeysIndicatorAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = new JSONObject();
        JSONArray keysCount = new JSONArray();
        for (Integer count : obj.keysCount) {
            keysCount.put(count);
        }
        actionObj.put("keys_count", keysCount);
        return actionObj;
    }
}
