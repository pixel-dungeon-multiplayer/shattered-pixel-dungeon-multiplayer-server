package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wndoptions;

import com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.WindowSerializer;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class WndInfoItemSerializer<T extends WndInfoItem> extends WindowSerializer<T> {

    @Override
    protected @NotNull String type() {
        return "info_item";
    }

    @Override
    protected @NotNull JSONObject args(@NotNull WndInfoItem obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        return getContract(obj, ctx, profile).toJson(ctx, profile);
    }

    protected @NotNull WndOptionContract getContract(@NotNull WndInfoItem obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndOptionContract contract = new WndOptionContract();
        contract.fillFromTitlebar(obj.titlebar, ctx, profile);
        contract.message = obj.txtInfo.LocalizedStringText();
        contract.layout = WndOptionContract.Layout.titledMessage();
        return contract;
    }
}



