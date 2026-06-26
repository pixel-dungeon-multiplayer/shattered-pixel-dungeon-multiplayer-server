package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.UpdateCellsAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UpdateCellsActionSerializer extends NetworkActionSerializer<UpdateCellsAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull UpdateCellsAction action, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject obj = new JSONObject();
        try {
            JSONArray posArr = new JSONArray();
            for (int p : action.positions) posArr.put(p);
            obj.put("positions", posArr);

            if (action.tiles != null) {
                JSONArray tilesArr = new JSONArray();
                for (int t : action.tiles) tilesArr.put(t);
                obj.put("tiles", tilesArr);
            }

            if (action.states != null) {
                JSONArray statesArr = new JSONArray();
                for (int s : action.states) statesArr.put(s);
                obj.put("states", statesArr);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }
}
