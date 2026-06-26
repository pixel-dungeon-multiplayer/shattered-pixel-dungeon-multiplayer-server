package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.Ratmogrify;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.dtos.TalentState;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class TalentStateSerializer implements Serializer<TalentState> {

    @Override
    public @NotNull JSONObject serialize(@NotNull TalentState obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        boolean previousUseRatroicEnergy = Ratmogrify.useRatroicEnergy;
        Ratmogrify.useRatroicEnergy = obj.hero != null && obj.hero.armorAbility instanceof Ratmogrify;
        try {
            JSONObject json = (JSONObject) ctx.serialize(obj.talent, "default");
            json.put("points", obj.points);
            return json;
        } finally {
            Ratmogrify.useRatroicEnergy = previousUseRatroicEnergy;
        }
    }
}
