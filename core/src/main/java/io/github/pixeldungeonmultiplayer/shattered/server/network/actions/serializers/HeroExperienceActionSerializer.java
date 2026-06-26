package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.HeroExperienceAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class HeroExperienceActionSerializer extends NetworkActionSerializer<HeroExperienceAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull HeroExperienceAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = new JSONObject();
        actionObj.put("lvl", obj.lvl);
        actionObj.put("exp", obj.exp);
        return actionObj;
    }
}
