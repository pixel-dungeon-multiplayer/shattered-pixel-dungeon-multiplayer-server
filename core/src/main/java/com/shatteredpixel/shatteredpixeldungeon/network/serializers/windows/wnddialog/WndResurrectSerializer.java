package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wnddialog;

import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndResurrect;
import org.jetbrains.annotations.NotNull;

public class WndResurrectSerializer extends WndDialogSerializer<WndResurrect> {

    @Override
    protected @NotNull WndDialogContract getContract(@NotNull WndResurrect obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndDialogContract contract = new WndDialogContract();
        contract.fillFromTitlebar(obj.titlebar, ctx, profile);
        contract.message = obj.message.LocalizedStringText();
        contract.layout = WndDialogContract.Layout.titledMessage();
        contract.itemSlots.add(new WndDialogContract.ItemSlot(obj.firstItem(), true));
        contract.itemSlots.add(new WndDialogContract.ItemSlot(obj.secondItem(), true));
        contract.actions.add(new WndDialogContract.Action(obj.confirmButton().LocalizedStringText(), false, obj.confirmButton().active));
        return contract;
    }
}
