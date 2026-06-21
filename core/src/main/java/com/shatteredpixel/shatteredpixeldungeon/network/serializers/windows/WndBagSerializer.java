package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows;

import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class WndBagSerializer extends WindowSerializer<WndBag> {

    @Override
    protected @NotNull String type() {
        return "bag_listener";
    }

    @Override
    protected @Nullable JSONObject args(@NotNull WndBag obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject args = new JSONObject();
        args.put("title", ctx.serialize(obj.title(), profile));

        JSONArray allowed = new JSONArray();
        for (List<Integer> itemPath : obj.allowedItems()) {
            JSONArray path = new JSONArray();
            for (int part : itemPath) {
                path.put(part);
            }
            allowed.put(path);
        }
        args.put("allowed_items", allowed);
        args.put("has_listener", obj.hasListener());
        return args;
    }
}
