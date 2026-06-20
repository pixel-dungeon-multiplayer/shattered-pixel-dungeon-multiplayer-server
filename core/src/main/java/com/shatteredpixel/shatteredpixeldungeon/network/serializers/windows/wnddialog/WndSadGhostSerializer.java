package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wnddialog;

import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndSadGhost;
import org.jetbrains.annotations.NotNull;

public class WndSadGhostSerializer extends WndDialogSerializer<WndSadGhost> {

    @Override
    protected @NotNull WndDialogContract getContract(@NotNull WndSadGhost obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndDialogContract contract = new WndDialogContract();
        contract.fillFromTitlebar(obj.titlebar, ctx, profile);
        contract.message = obj.message.LocalizedStringText();
        contract.layout = WndDialogContract.Layout.titledMessage();
        contract.itemSlots.add(new WndDialogContract.ItemSlot(obj.btnWeapon.item(), true, false));
        contract.itemSlots.add(new WndDialogContract.ItemSlot(obj.btnArmor.item(), true, false));
        return contract;
    }
}
