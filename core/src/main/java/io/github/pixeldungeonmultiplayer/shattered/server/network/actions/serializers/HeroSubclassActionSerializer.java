package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.HeroSubclassAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class HeroSubclassActionSerializer extends NetworkActionSerializer<HeroSubclassAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull HeroSubclassAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = new JSONObject();
        actionObj.put("subclass", ctx.serialize(obj.subclass, "default"));
        return actionObj;
    }
}
