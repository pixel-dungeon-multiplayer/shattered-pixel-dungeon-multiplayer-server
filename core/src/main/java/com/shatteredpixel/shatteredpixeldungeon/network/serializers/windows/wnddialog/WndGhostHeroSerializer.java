package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wnddialog;

import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.ui.ImageIcon;
import org.jetbrains.annotations.NotNull;

public class WndGhostHeroSerializer extends WndDialogSerializer<DriedRose.WndGhostHero> {

    @Override
    protected @NotNull WndDialogContract getContract(@NotNull DriedRose.WndGhostHero obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndDialogContract contract = new WndDialogContract();
        contract.titleText = obj.title();
        contract.message = obj.message();
        contract.titleIcon = ImageIcon.itemSprite(obj.rose().image(), null);
        contract.layout = WndDialogContract.Layout.titledMessage();
        contract.itemSlots.add(new WndDialogContract.ItemSlot(obj.weaponItem(), true));
        contract.itemSlots.add(new WndDialogContract.ItemSlot(obj.armorItem(), true));
        return contract;
    }
}
