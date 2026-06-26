package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.HeroGoldAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class HeroGoldActionSerializer extends NetworkActionSerializer<HeroGoldAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull HeroGoldAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = new JSONObject();
        actionObj.put("gold", obj.gold);
        return actionObj;
    }
}
