package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wnddialog.wndoptions;

import com.nikita22007.multiplayer.utils.text.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wnddialog.WndDialogContract;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wnddialog.WndDialogSerializer;
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

