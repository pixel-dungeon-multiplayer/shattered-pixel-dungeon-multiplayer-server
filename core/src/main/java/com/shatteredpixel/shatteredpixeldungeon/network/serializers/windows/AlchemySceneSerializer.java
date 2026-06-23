package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows;

import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.scenes.AlchemyScene;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

public class AlchemySceneSerializer extends WindowSerializer<AlchemyScene> {

    @Override
    protected @NotNull String type() {
        return "alchemy_scene";
    }

    @Override
    protected @Nullable JSONObject args(@NotNull AlchemyScene obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject args = new JSONObject();
        args.put("cancel_enabled", obj.cancel.active);
        args.put("repeat_enabled", obj.repeat.active);
        args.put("energy_add_enabled", obj.energyAdd.active);

        JSONArray inputs = new JSONArray();
        for (AlchemyScene.InputButton input : obj.inputs) {
            inputs.put(ctx.serialize(input.item(), profile));
        }
        args.put("inputs", inputs);

        JSONArray combineButtons = new JSONArray();
        for (AlchemyScene.CombineButton combineButton : obj.combines) {
            JSONObject combineButtonObject = new JSONObject();
            combineButtonObject.put("visible", combineButton.visible);
            combineButtonObject.put("enabled", combineButton.active);
            combineButtonObject.put("cost", combineButton.cost);
            combineButtons.put(combineButtonObject);
        }
        args.put("combine_buttons", combineButtons);

        JSONArray outputs = new JSONArray();
        for (AlchemyScene.OutputSlot output : obj.outputs) {
            JSONObject outputObject = new JSONObject();
            outputObject.put("visible", output.visible);
            outputObject.put("item", ctx.serialize(output.item(), profile));
            outputs.put(outputObject);
        }
        args.put("outputs", outputs);

        args.put("energy_icon", ctx.serialize(obj.energyIcon, profile));
        args.put("energy_text", ctx.serialize(obj.energyText, profile));
        args.put("energy_add_blinking", obj.energyAddBlinking);


        args.put("craft_effect", obj.craftEffect);
        args.put("create_energy_effect", obj.createEnergyEffect);

        if (obj.identifyEffect != null) {
            JSONObject identifyEffect = new JSONObject();
            identifyEffect.put("old_name", ctx.serialize(obj.identifyEffect.oldName, profile));
            identifyEffect.put("new_name", ctx.serialize(obj.identifyEffect.newName, profile));
            args.put("identify_effect", identifyEffect);
        } else {
            args.put("identify_effect", JSONObject.NULL);
        }
        return args;
    }
}
