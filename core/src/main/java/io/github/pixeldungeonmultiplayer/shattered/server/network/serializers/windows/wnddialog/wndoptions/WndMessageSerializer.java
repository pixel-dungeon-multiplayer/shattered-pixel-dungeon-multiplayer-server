package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.wnddialog.wndoptions;

import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.wnddialog.WndDialogContract;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.wnddialog.WndDialogSerializer;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage;
import org.jetbrains.annotations.NotNull;

public class WndMessageSerializer extends WndDialogSerializer<WndMessage> {

    @Override
    protected @NotNull WndDialogContract getContract(@NotNull WndMessage obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndDialogContract contract = new WndDialogContract();
        contract.titleText = LocalizedString.raw("");
        contract.message = obj.text();
        contract.layout = WndDialogContract.Layout.titledMessage();
        return contract;
    }
}

