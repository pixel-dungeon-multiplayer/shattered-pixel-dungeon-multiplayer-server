package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.SpecialSlotsDefinitionAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import io.github.pixeldungeonmultiplayer.shattered.server.network.Server;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class SpecialSlotsDefinitionActionSerializer extends NetworkActionSerializer<SpecialSlotsDefinitionAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull SpecialSlotsDefinitionAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        SerializationContext innerCtx = new SerializationContext(Server.SERIALIZERS, obj.hero);
        Object payload = innerCtx.serialize(obj.hero.belongings, "special_slot_definitions");

        JSONObject actionObj = new JSONObject();
        actionObj.put("slots", payload);
        return actionObj;
    }
}
