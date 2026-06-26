package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.wnddialog;

import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAugmentation;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.ui.ImageIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import org.jetbrains.annotations.NotNull;

public class WndAugmentSerializer extends WndDialogSerializer<StoneOfAugmentation.WndAugment> {

    @Override
    protected @NotNull WndDialogContract getContract(@NotNull StoneOfAugmentation.WndAugment obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndDialogContract contract = new WndDialogContract();
        contract.fillFromTitlebar(obj.titlebar, ctx, profile);
        contract.message = obj.message.LocalizedStringText();
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



