package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.scenes.AlchemyScene;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

public class AlchemySceneSerializer extends WindowSerializer<AlchemyScene> {

    @Override
    protected @NotNull String type() {
        return "alchemy";
    }

    @Override
    protected @Nullable JSONObject args(@NotNull AlchemyScene obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject args = new JSONObject();
        args.put("energy", Dungeon.energy);
        args.put("has_toolkit", obj.hasToolkit());
        if (obj.hasToolkit()) {
            args.put("toolkit_energy", obj.toolkitEnergy());
        }

        JSONArray inputs = new JSONArray();
        for (Item input : obj.inputItems()) {
            if (input != null) {
                inputs.put(ctx.serialize(input, "inventory"));
            }
        }
        args.put("input", inputs);

        JSONArray outputs = new JSONArray();
        for (int i = 0; i < obj.outputItems().size(); i++) {
            Item output = obj.outputItems().get(i);
            if (output == null) {
                continue;
            }
            JSONObject outputObj = new JSONObject();
            outputObj.put("cost", obj.combineCosts().get(i));
            outputObj.put("enabled", obj.combineEnabled().get(i));
            outputObj.put("item", ctx.serialize(output, "inventory"));
            outputs.put(outputObj);
        }
        args.put("output", outputs);

        args.put("energyAddBlinking", obj.energyAddBlinking());
        args.put("repeat_enabled", obj.repeatEnabled());
        if (obj.shouldCreateEnergy()) {
            args.put("createEnergy", true);
        }
        if (obj.craftedItem()) {
            args.put("craftedItem", true);
        }
        return args;
    }
}
