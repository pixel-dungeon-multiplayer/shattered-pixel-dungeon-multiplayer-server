package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.HeroClassAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class HeroClassActionSerializer extends NetworkActionSerializer<HeroClassAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull HeroClassAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = new JSONObject();
        actionObj.put("class", ctx.serialize(obj.heroClass, "default"));
        return actionObj;
    }
}
