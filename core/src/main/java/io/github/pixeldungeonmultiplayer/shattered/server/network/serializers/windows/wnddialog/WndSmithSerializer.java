package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.wnddialog;

import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.ui.ItemButton;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBlacksmith;
import org.jetbrains.annotations.NotNull;

public class WndSmithSerializer extends WndDialogSerializer<WndBlacksmith.WndSmith> {

    @Override
    protected @NotNull WndDialogContract getContract(@NotNull WndBlacksmith.WndSmith obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndDialogContract contract = new WndDialogContract();
        contract.fillFromTitlebar(obj.titlebar, ctx, profile);
        contract.message = obj.message.LocalizedStringText();
        contract.layout = WndDialogContract.Layout.titledMessage();

        for (ItemButton button : obj.rewardButtons) {
            contract.itemSlots.add(new WndDialogContract.ItemSlot(button.item(), true));
        }

        return contract;
    }
}



