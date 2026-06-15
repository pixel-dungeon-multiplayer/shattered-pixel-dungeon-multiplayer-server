package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows;

import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTradeItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class WndTradeItemSerializer extends WindowSerializer<WndTradeItem> {

    @Override
    protected @NotNull String type() {
        return "trade_item";
    }

    @Override
    protected @Nullable JSONObject args(@NotNull WndTradeItem obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject args = new JSONObject();
        args.put("selling", obj.selling());
        args.put("price", obj.price());
        args.put("item", ctx.serialize(obj.tradeItem(), "inventory"));
        if (obj.stealAvailable()) {
            args.put("steal", true);
            args.put("chance", obj.stealChance());
            args.put("charges", obj.stealCharges());
        }
        return args;
    }
}
