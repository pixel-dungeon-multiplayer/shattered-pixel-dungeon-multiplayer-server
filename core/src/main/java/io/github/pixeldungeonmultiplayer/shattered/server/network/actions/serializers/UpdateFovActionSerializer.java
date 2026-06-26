package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.UpdateFovAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

public class UpdateFovActionSerializer extends NetworkActionSerializer<UpdateFovAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull UpdateFovAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONArray visiblePos = new JSONArray();
        boolean[] visible = obj.visible();
        for (int i = 0; i < visible.length; i++) {
            if (visible[i]) {
                visiblePos.put(i);
            }
        }

        JSONObject actionObj = new JSONObject();
        actionObj.put("visible_pos", visiblePos);
        return actionObj;
    }
}
