package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import io.github.pixeldungeonmultiplayer.shattered.server.network.SpecialSlot;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SpecialSlotDefinitionsSerializer implements Serializer<Belongings> {

    @Override
    public Object serialize(@NotNull Belongings belongings, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONArray slotsArr = new JSONArray();
        try {
            for (SpecialSlot slot : belongings.getSpecialSlots()) {
                JSONObject slotObj = new JSONObject();
                slotObj.put("id", slot.id);
                slotObj.put("sprite", slot.sprite);
                slotObj.put("image_id", slot.image_id);
                slotsArr.put(slotObj);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONArray();
        }
        return slotsArr;
    }
}
