package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.wnddialog.wndoptions;

import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.wnddialog.WndDialogContract;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.wnddialog.WndDialogSerializer;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBlacksmith;
import org.jetbrains.annotations.NotNull;

public class WndReforgeSerializer extends WndDialogSerializer<WndBlacksmith.WndReforge> {

    @Override
    protected @NotNull WndDialogContract getContract(@NotNull WndBlacksmith.WndReforge obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndDialogContract contract = new WndDialogContract();
        contract.fillFromTitlebar(obj.titlebar, ctx, profile);
        contract.message = obj.message.LocalizedStringText();
        contract.layout = WndDialogContract.Layout.titledMessage();
        contract.itemSlots.add(new WndDialogContract.ItemSlot(obj.btnItem1.item(), true));
        contract.itemSlots.add(new WndDialogContract.ItemSlot(obj.btnItem2.item(), true));
        contract.actions.add(new WndDialogContract.Action(obj.btnReforge.LocalizedStringText(), false, obj.btnReforge.active));
        return contract;
    }
}
