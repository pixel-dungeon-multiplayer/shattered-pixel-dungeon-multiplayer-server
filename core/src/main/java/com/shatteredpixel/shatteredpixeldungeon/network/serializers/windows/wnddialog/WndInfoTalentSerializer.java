package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wnddialog;

import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.ui.ImageIcon;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoTalent;
import org.jetbrains.annotations.NotNull;

public class WndInfoTalentSerializer extends WndDialogSerializer<WndInfoTalent> {

    @Override
    protected @NotNull WndDialogContract getContract(@NotNull WndInfoTalent obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndDialogContract contract = new WndDialogContract();
        contract.fillFromTitlebar(obj.titlebar, ctx, profile);
        contract.message = obj.txtInfo.LocalizedStringText();
        contract.layout = WndDialogContract.Layout.titledMessage();
        if (obj.button != null) {
            contract.actions.add(new WndDialogContract.Action(
                    obj.button.LocalizedStringText(),
                    false,
                    obj.button.activeForNetwork(),
                    ImageIcon.fromImage(obj.button.icon(), ctx, profile),
                    obj.button.fontSize()));
        }
        return contract;
    }
}



