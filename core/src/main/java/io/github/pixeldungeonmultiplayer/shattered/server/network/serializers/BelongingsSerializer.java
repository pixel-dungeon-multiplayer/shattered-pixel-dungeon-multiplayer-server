package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import io.github.pixeldungeonmultiplayer.shattered.server.network.SpecialSlot;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BelongingsSerializer implements Serializer<Belongings> {

    @Override
    public Object serialize(@NotNull Belongings belongings, @NotNull SerializationContext ctx, @NotNull String profile) {

        try {
            // Default or "rebuild" profile
            JSONObject payload = new JSONObject();
            
            // Use context to serialize backpack and items
            payload.put("backpack", ctx.serialize(belongings.backpack));

            JSONArray specialSlotItems = new JSONArray();
            for (SpecialSlot specialSlot: belongings.getSpecialSlots()) {
                JSONObject specialSlotObj = new JSONObject();
                specialSlotObj.put("id", specialSlot.id);
                specialSlotObj.put("item", specialSlot.item == null? JSONObject.NULL: ctx.serialize(specialSlot.item));
                specialSlotItems.put(specialSlotObj);
            }
            payload.put("special_slots", specialSlotItems);

            return payload;

        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }
}
