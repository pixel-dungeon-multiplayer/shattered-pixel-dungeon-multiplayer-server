package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wndoptions;

import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.WindowSerializer;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBlacksmith;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class WndReforgeSerializer extends WindowSerializer<WndBlacksmith.WndReforge> {

    @Override
    protected @NotNull String type() {
        return "wnd_option";
    }

    @Override
    protected @Nullable JSONObject args(@NotNull WndBlacksmith.WndReforge obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndOptionContract contract = new WndOptionContract();
        contract.fillFromTitlebar(obj.titlebar, ctx, profile);
        contract.message = obj.message.LocalizedStringText();
        contract.layout = WndOptionContract.Layout.titledMessage();
        contract.itemSlots.add(new WndOptionContract.ItemSlot("first_item", obj.btnItem1.item(), true, "select_first_item", true));
        contract.itemSlots.add(new WndOptionContract.ItemSlot("second_item", obj.btnItem2.item(), true, "select_second_item", true));
        contract.actions.add(new WndOptionContract.Action("reforge", obj.btnReforge.LocalizedStringText(), obj.btnReforge.active));
        return contract.toJson(ctx, profile);
    }
}
