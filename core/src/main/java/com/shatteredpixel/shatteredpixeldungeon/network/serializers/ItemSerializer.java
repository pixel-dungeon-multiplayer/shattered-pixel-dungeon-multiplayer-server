package com.shatteredpixel.shatteredpixeldungeon.network.serializers;

import com.nikita22007.multiplayer.utils.Log;
import com.nikita22007.multiplayer.noosa.particles.Emitter;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.network.actions.LiveStateNetworkAction;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ItemSerializer implements Serializer<Item> {

    @Override
    public Object serialize(@NotNull Item item, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject itemObj = new JSONObject();
        Hero hero = ctx.observer;

        try {
            if (hero != null) {
                JSONArray actionsArr = new JSONArray();
                for (String action : item.actions(hero)) {
                    actionsArr.put(action);
                }
                itemObj.put("actions", actionsArr);

                JSONObject actionNames = new JSONObject();
                for (int i = 0; i < actionsArr.length(); i++) {
                    String action = actionsArr.getString(i);
                    actionNames.put(action, ctx.serialize(item.actionName(action, hero), profile));
                }
                itemObj.put("action_names", actionNames);

                itemObj.put("default_action", item.defaultAction == null ? "null" : item.defaultAction);
                itemObj.put("info", ctx.serialize(item.info(hero), profile));
                itemObj.put("ui", item.itemUI(hero));
            }

            itemObj.put("sprite_sheet", item.spriteSheet());
            itemObj.put("image", item.image());
            itemObj.put("icon", item.icon);
            itemObj.put("name", ctx.serialize(item.name(), profile));
            itemObj.put("stackable", item.stackable);
            itemObj.put("quantity", item.quantity());
            itemObj.put("known", item.isIdentified());
            itemObj.put("cursed", item.visiblyCursed());
            itemObj.put("identified", item.isIdentified());
            itemObj.put("level_known", item.levelKnown);
            itemObj.put("level", item.visiblyUpgraded());
            itemObj.put("energy_value", item.energyVal());

            ItemSprite.Glowing glowing = item.glowing();
            if (glowing != null) {
                itemObj.put("glowing", glowing.toJsonObject());
            } else {
                itemObj.put("glowing", JSONObject.NULL);
            }

            Emitter emitter = item.emitter();
            LiveStateNetworkAction emitterAction = emitter == null ? null : emitter.networkStartAction();
            itemObj.put("emitter", emitterAction == null ? JSONObject.NULL : ctx.serialize(emitterAction));

        } catch (JSONException e) {
            Log.e("Packet", "JSONException inside ItemSerializer. " + e.toString());
        }

        return itemObj;
    }
}
