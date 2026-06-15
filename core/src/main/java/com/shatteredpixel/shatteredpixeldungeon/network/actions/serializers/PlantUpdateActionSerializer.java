package com.shatteredpixel.shatteredpixeldungeon.network.actions.serializers;

import com.shatteredpixel.shatteredpixeldungeon.network.actions.PlantUpdateAction;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class PlantUpdateActionSerializer extends NetworkActionSerializer<PlantUpdateAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull PlantUpdateAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject plantObj = new JSONObject();
        plantObj.put("pos", obj.pos);
        plantObj.put("texture", "plants.png");

        JSONObject plantInfoObj = new JSONObject();
        plantInfoObj.put("sprite_id", obj.plant.image);
        plantInfoObj.put("name", ctx.serialize(obj.plant.name(), profile));
        plantInfoObj.put("desc", ctx.serialize(obj.plant.desc(), profile));
        plantObj.put("plant_info", plantInfoObj);

        return plantObj;
    }
}
