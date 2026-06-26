package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.CellListenerPromptAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class CellListenerPromptActionSerializer extends NetworkActionSerializer<CellListenerPromptAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull CellListenerPromptAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = new JSONObject();
        actionObj.put("prompt", obj.prompt == null ? JSONObject.NULL : obj.prompt);
        return actionObj;
    }
}
