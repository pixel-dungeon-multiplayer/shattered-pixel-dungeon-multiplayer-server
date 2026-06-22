package com.shatteredpixel.shatteredpixeldungeon.network.serializers;

import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

public class HeapSerializer implements Serializer<Heap> {

    @Override
    public Object serialize(@NotNull Heap heap, @NotNull SerializationContext ctx, @NotNull String profile) {
        if (heap == null || heap.isEmpty()) {
            return JSONObject.NULL;
        }
        
        JSONObject heapObj = new JSONObject();
        
        try {
            heapObj.put("pos", heap.pos);

            Object serializedItem = ctx.serialize(heap.peekVisual(), "ground");

            heapObj.put("peek", serializedItem);
            heapObj.put("title", ctx.serialize(heap.title()));
            heapObj.put("info", ctx.serialize(heap.info()));

            heapObj.put("seen", heap.isSeen());
            heapObj.put("hidden", heap.hidden);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return heapObj;
    }
}
