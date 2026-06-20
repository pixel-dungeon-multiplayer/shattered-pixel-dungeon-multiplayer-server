package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wnddialog.wndoptions;

import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.ui.ImageIcon;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wnddialog.WndDialogContract;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndEnergizeItem;
import org.jetbrains.annotations.NotNull;

public class WndEnergizeItemSerializer extends WndInfoItemSerializer<WndEnergizeItem> {

    @Override
    protected @NotNull WndDialogContract getContract(@NotNull WndEnergizeItem obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndDialogContract contract = super.getContract(obj, ctx, profile);
        for (RedButton button : obj.buttons) {
            contract.actions.add(new WndDialogContract.Action(
                    button.LocalizedStringText(),
                    ImageIcon.fromImage(button.icon(), ctx, profile)));
        }
        return contract;
    }
}

