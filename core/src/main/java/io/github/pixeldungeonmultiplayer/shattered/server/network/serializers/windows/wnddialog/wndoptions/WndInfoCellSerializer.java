package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.wnddialog.wndoptions;

import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.wnddialog.WndDialogContract;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.wnddialog.WndDialogSerializer;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoCell;
import org.jetbrains.annotations.NotNull;

public class WndInfoCellSerializer extends WndDialogSerializer<WndInfoCell> {

    @Override
    protected @NotNull WndDialogContract getContract(@NotNull WndInfoCell obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndDialogContract contract = new WndDialogContract();
        contract.fillFromTitlebar(obj.titlebar(), ctx, profile);
        contract.message = obj.desc();
        contract.layout = WndDialogContract.Layout.titledMessage();
        return contract;
    }
}
