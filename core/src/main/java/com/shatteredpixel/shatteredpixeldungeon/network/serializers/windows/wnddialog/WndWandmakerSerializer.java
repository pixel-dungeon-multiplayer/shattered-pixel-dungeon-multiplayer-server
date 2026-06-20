package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wnddialog;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Wandmaker;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.ui.ImageIcon;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndWandmaker;
import org.jetbrains.annotations.NotNull;

public class WndWandmakerSerializer extends WndDialogSerializer<WndWandmaker> {

    @Override
    protected @NotNull WndDialogContract getContract(@NotNull WndWandmaker obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndDialogContract contract = new WndDialogContract();
        contract.titleText = obj.wandmaker.name();
        contract.message = obj.message;
        contract.titleIcon = ImageIcon.charSprite(obj.wandmaker.getSprite().getClass().getName());
        contract.layout = WndDialogContract.Layout.titledMessage();
        contract.itemSlots.add(new WndDialogContract.ItemSlot(Wandmaker.Quest.wand1, true, true));
        contract.itemSlots.add(new WndDialogContract.ItemSlot(Wandmaker.Quest.wand2, true, true));
        return contract;
    }
}
