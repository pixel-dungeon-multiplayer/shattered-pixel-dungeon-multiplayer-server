package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wnddialog;

import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.TrinketCatalyst;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.ui.ImageIcon;
import org.jetbrains.annotations.NotNull;

public class WndTrinketSerializer extends WndDialogSerializer<TrinketCatalyst.WndTrinket> {

    @Override
    protected @NotNull WndDialogContract getContract(@NotNull TrinketCatalyst.WndTrinket obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndDialogContract contract = new WndDialogContract();
        contract.fillFromTitlebar(obj.titlebar, ctx, profile);
        contract.message = Messages.get(TrinketCatalyst.class, "window_text");
        contract.layout = WndDialogContract.Layout.titledMessage();
        for (Trinket trinket : obj.rolledTrinkets()) {
            contract.itemSlots.add(new WndDialogContract.ItemSlot(trinket, true, true));
        }
        return contract;
    }
}
