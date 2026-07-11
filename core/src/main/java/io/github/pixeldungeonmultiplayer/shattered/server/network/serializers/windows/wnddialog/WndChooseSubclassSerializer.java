package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.wnddialog;

import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.ui.ImageIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndChooseSubclass;
import org.jetbrains.annotations.NotNull;

public class WndChooseSubclassSerializer extends WndDialogSerializer<WndChooseSubclass> {

    @Override
    protected @NotNull WndDialogContract getContract(@NotNull WndChooseSubclass obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndDialogContract contract = new WndDialogContract();
        contract.fillFromTitlebar(obj.titlebar, ctx, profile);
        contract.message = obj.message.LocalizedStringText();
        contract.layout = WndDialogContract.Layout.titledMessage();
        for (int i = 0; i < obj.subclassButtons.size(); i++) {
            RedButton button = obj.subclassButtons.get(i);
            contract.actions.add(new WndDialogContract.Action(
                    button.LocalizedStringText(),
                    i < obj.subclassInfoButtons.size(),
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
        contract.topRightButton = new WndDialogContract.TopRightButton(
                text(obj.randomButton),
                obj.randomButton.activeForNetwork(),
                icon(obj.randomButton));
        return contract;
    }

    private @NotNull LocalizedString text(@NotNull IconButton button) {
        LocalizedString text = button.hoverTextForNetwork();
        return text == null ? LocalizedString.EMPTY : text;
    }

    private @NotNull ImageIcon icon(@NotNull IconButton button) {
        return button.iconType() == null ? ImageIcon.none() : ImageIcon.uiIcon(button.iconType());
    }
}
