package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows;

import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.ui.ImageIcon;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wnddialog.WndDialogContract;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wnddialog.WndDialogSerializer;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBlacksmith;
import org.jetbrains.annotations.NotNull;

public class WndBlacksmithSerializer extends WndDialogSerializer<WndBlacksmith> {

    @Override
    protected @NotNull WndDialogContract getContract(@NotNull WndBlacksmith obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndDialogContract contract = new WndDialogContract();
        contract.fillFromTitlebar(obj.titlebar, ctx, profile);
        contract.message = obj.message.LocalizedStringText();

        for (RedButton button : obj.buttons) {
            contract.actions.add(new WndDialogContract.Action(
                    button.LocalizedStringText(),
                    false,
                    button.activeForNetwork(),
                    ImageIcon.none(),
                    button.fontSize()));
        }

        return contract;
    }
}



