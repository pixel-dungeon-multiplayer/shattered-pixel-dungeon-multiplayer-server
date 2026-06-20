package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wnddialog;

import com.nikita22007.multiplayer.utils.text.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.ui.ImageIcon;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndChooseAbility;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import org.jetbrains.annotations.NotNull;

public class WndChooseAbilitySerializer extends WndDialogSerializer<WndChooseAbility> {

    @Override
    protected @NotNull WndDialogContract getContract(@NotNull WndChooseAbility obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndDialogContract contract = new WndDialogContract();
        contract.fillFromTitlebar(obj.titlebar, ctx, profile);
        contract.message = obj.message.LocalizedStringText();

        for (int i = 0; i < obj.abilityButtons.length; i++) {
            RedButton button = obj.abilityButtons[i];
            contract.actions.add(new WndDialogContract.Action(
                    button.LocalizedStringText(),
                    obj.abilityInfoButtons[i] != null,
                    button.activeForNetwork(),
                    ImageIcon.none(),
                    button.fontSize()));
        }
        contract.actions.add(new WndDialogContract.Action(
                obj.cancelButton.LocalizedStringText(),
                false,
                obj.cancelButton.activeForNetwork(),
                ImageIcon.none(),
                obj.cancelButton.fontSize()));

        if (obj.randomButton.parent != null) {
            contract.topRightButton = new WndDialogContract.TopRightButton(
                    text(obj.randomButton),
                    obj.randomButton.activeForNetwork(),
                    icon(obj.randomButton));
        }

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



