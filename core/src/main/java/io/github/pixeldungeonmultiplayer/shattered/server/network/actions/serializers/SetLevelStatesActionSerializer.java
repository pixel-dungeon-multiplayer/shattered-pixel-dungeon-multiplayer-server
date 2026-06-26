package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.SetLevelStatesAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

public class SetLevelStatesActionSerializer extends NetworkActionSerializer<SetLevelStatesAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull SetLevelStatesAction action, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject obj = new JSONObject();
        JSONArray arr = new JSONArray();
        for (int state : action.states) {
            arr.put(state);
        }
        obj.put("states", arr);
        return obj;
    }
}
