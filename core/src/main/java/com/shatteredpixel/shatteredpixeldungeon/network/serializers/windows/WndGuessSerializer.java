package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfIntuition;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

public class WndGuessSerializer extends WindowSerializer<StoneOfIntuition.WndGuess> {

    @Override
    protected @NotNull String type() {
        return "guess";
    }

    @Override
    protected @Nullable JSONObject args(@NotNull StoneOfIntuition.WndGuess obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject args = new JSONObject();
        args.put("item", ctx.serialize(obj.item(), "inventory"));

        JSONArray icons = new JSONArray();
        for (int icon : obj.icons()) {
            icons.put(icon);
        }
        args.put("icons", icons);

        JSONArray keys = new JSONArray();
        for (Class<? extends Item> option : obj.guessOptions()) {
            keys.put(option.getName());
        }
        args.put("keys", keys);
        return args;
    }
}
