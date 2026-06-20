package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wndoptions;

import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.WindowSerializer;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndResurrect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class WndResurrectSerializer extends WindowSerializer<WndResurrect> {

    @Override
    protected @NotNull String type() {
        return "wnd_option";
    }

    @Override
    protected @Nullable JSONObject args(@NotNull WndResurrect obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndOptionContract contract = new WndOptionContract();
        contract.fillFromTitlebar(obj.titlebar, ctx, profile);
        contract.message = obj.message.LocalizedStringText();
        contract.layout = WndOptionContract.Layout.titledMessage();
        contract.itemSlots.add(new WndOptionContract.ItemSlot("first_item", obj.firstItem(), true, "select_first_item", true));
        contract.itemSlots.add(new WndOptionContract.ItemSlot("second_item", obj.secondItem(), true, "select_second_item", true));
        contract.actions.add(new WndOptionContract.Action("confirm", obj.confirmButton().LocalizedStringText(), obj.confirmButton().active));
        return contract.toJson(ctx, profile);
    }
}



