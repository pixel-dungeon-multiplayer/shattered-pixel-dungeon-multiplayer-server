package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.wnddialog;

import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.ui.ImageIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMonkAbilities;
import org.jetbrains.annotations.NotNull;

public class WndMonkAbilitiesSerializer extends WndDialogSerializer<WndMonkAbilities> {

    @Override
    protected @NotNull WndDialogContract getContract(@NotNull WndMonkAbilities obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndDialogContract contract = new WndDialogContract();
        contract.titleText = obj.title.LocalizedStringText();
        contract.titleColor = WndMonkAbilities.TITLE_COLOR;
        contract.layout = WndDialogContract.Layout.options();

        for (RedButton button : obj.buttons) {
            contract.actions.add(new WndDialogContract.Action(
                    button.LocalizedStringText(),
                    false,
                    button.activeForNetwork(),
                    ImageIcon.fromImage(button.icon(), ctx, profile),
                    button.fontSize()));
        }

        return contract;
    }
}



