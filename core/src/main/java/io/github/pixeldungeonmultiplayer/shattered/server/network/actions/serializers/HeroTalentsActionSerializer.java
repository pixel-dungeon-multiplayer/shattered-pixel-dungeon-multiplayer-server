package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.HeroTalentsAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.dtos.TalentState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

public class HeroTalentsActionSerializer extends NetworkActionSerializer<HeroTalentsAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull HeroTalentsAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = new JSONObject();
        JSONArray tiers = new JSONArray();
        for (var tierTalents : obj.talents) {
            JSONArray tiertalentsArr = new JSONArray();
            for (var talentPair: tierTalents.entrySet()) {
                tiertalentsArr.put(ctx.serialize(new TalentState(talentPair.getKey(), talentPair.getValue(), ctx.observer)));
            }
            tiers.put(tiertalentsArr);
        }
        actionObj.put("talents", tiers);
        return actionObj;
    }
}
