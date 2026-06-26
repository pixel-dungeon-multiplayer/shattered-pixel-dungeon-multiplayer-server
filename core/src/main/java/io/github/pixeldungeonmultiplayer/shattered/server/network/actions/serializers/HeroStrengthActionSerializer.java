package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.HeroStrengthAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class HeroStrengthActionSerializer extends NetworkActionSerializer<HeroStrengthAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull HeroStrengthAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = new JSONObject();
        actionObj.put("strength", obj.strength);
        return actionObj;
    }
}
