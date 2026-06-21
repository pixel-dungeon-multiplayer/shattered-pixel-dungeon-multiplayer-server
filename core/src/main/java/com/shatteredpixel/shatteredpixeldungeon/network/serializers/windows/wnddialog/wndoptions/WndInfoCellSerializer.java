package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wnddialog.wndoptions;

import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wnddialog.WndDialogContract;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wnddialog.WndDialogSerializer;
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
