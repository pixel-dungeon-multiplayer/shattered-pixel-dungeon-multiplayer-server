package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers;

import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

public class HeapRemovalSerializer implements Serializer<Heap> {

    @Override
    public Object serialize(@NotNull Heap heap, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject heapObj = new JSONObject();
        try {
            heapObj.put("pos", heap.pos);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return heapObj;
    }
}
