package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wndoptions;

import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUseItem;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;

public class WndUseItemSerializer extends WndInfoItemSerializer<WndUseItem> {

    @Override
    protected @NotNull String type() {
        return "use_item";
    }

    @Override
    protected @NotNull WndOptionContract getContract(@NotNull WndInfoItem obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndOptionContract contract = super.getContract(obj, ctx, profile);
        WndUseItem wnd = (WndUseItem)obj;
        for (RedButton button : wnd.buttons) {
            contract.options.add(new WndOptionContract.Option(button.LocalizedStringText()));
        }
        return contract;
    }
}



