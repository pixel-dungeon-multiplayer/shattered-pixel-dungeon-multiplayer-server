package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wnddialog.wndoptions;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric.Trinity;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.ui.ImageIcon;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wnddialog.WndDialogContract;
import org.jetbrains.annotations.NotNull;

public class WndItemConfirmSerializer extends WndTitledMessageSerializer<Trinity.WndItemConfirm> {

    @Override
    protected @NotNull WndDialogContract getContract(@NotNull Trinity.WndItemConfirm obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndDialogContract contract = super.getContract(obj, ctx, profile);

        if (obj.btnConfirm != null) {
            contract.actions.add(new WndDialogContract.Action(
                    obj.btnConfirm.LocalizedStringText(),
                    false,
                    obj.btnConfirm.activeForNetwork(),
                    ImageIcon.fromImage(obj.btnConfirm.icon(), ctx, profile)
            ));
        }

        return contract;
    }
}
