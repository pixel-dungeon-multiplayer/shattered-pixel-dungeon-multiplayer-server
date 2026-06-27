package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.wnddialog;

import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.ui.ImageIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndChooseSubclass;
import org.jetbrains.annotations.NotNull;

public class WndChooseSubclassSerializer extends WndDialogSerializer<WndChooseSubclass> {

    @Override
    protected @NotNull WndDialogContract getContract(@NotNull WndChooseSubclass obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndDialogContract contract = new WndDialogContract();
        contract.fillFromTitlebar(obj.titlebar, ctx, profile);
        contract.message = obj.message.LocalizedStringText();
        contract.layout = WndDialogContract.Layout.titledMessage();
        for (RedButton button : obj.subclassButtons) {
            contract.actions.add(new WndDialogContract.Action(
                    button.LocalizedStringText(),
                    true,
                    button.activeForNetwork(),
                    ImageIcon.fromImage(button.icon(), ctx, profile),
                    button.fontSize(),
                    button.leftJustify));
        }
        contract.actions.add(new WndDialogContract.Action(
                obj.cancelButton.LocalizedStringText(),
                false,
                obj.cancelButton.activeForNetwork(),
                ImageIcon.fromImage(obj.cancelButton.icon(), ctx, profile),
                obj.cancelButton.fontSize(),
                obj.cancelButton.leftJustify));
        return contract;
    }
}
