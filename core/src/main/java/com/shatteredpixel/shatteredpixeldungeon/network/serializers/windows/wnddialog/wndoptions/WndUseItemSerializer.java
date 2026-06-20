package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wnddialog.wndoptions;

import com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wnddialog.WndDialogContract;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUseItem;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;

public class WndUseItemSerializer extends WndInfoItemSerializer<WndUseItem> {

    @Override
    protected @NotNull WndDialogContract getContract(@NotNull WndUseItem obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndDialogContract contract = super.getContract(obj, ctx, profile);
        for (RedButton button : obj.buttons) {
            contract.actions.add(new WndDialogContract.Action(button.LocalizedStringText()));
        }
        return contract;
    }
}

