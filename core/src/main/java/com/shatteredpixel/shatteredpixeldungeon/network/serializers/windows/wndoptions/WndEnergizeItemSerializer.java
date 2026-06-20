package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wndoptions;

import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.ui.ImageIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndEnergizeItem;
import org.jetbrains.annotations.NotNull;

public class WndEnergizeItemSerializer extends WndInfoItemSerializer<WndEnergizeItem> {

    @Override
    protected @NotNull String type() {
        return "energize_item";
    }

    @Override
    protected @NotNull WndOptionContract getContract(@NotNull WndEnergizeItem obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndOptionContract contract = super.getContract(obj, ctx, profile);
        for (RedButton button : obj.buttons) {
            contract.options.add(new WndOptionContract.Option(
                    button.LocalizedStringText(),
                    ImageIcon.fromImage(button.icon(), ctx, profile)));
        }
        return contract;
    }
}



