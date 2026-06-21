package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wnddialog.wndoptions;

import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.ui.ImageIcon;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wnddialog.WndDialogContract;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wnddialog.WndDialogSerializer;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndQuest;
import org.jetbrains.annotations.NotNull;

public class WndQuestSerializer extends WndDialogSerializer<WndQuest> {

    @Override
    protected @NotNull WndDialogContract getContract(@NotNull WndQuest obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndDialogContract contract = new WndDialogContract();
        contract.titleText = obj.charName();
        contract.message = obj.text();
        contract.titleIcon = ImageIcon.charSprite(obj.spriteName());
        contract.layout = WndDialogContract.Layout.titledMessage();
        return contract;
    }
}

