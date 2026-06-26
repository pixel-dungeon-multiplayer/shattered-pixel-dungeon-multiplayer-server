package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.wnddialog.wndoptions;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric.Trinity;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.ui.ImageIcon;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.wnddialog.WndDialogContract;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import org.jetbrains.annotations.NotNull;

public class WndUseTrinitySerializer extends WndTitledMessageSerializer<Trinity.WndUseTrinity> {

    @Override
    protected @NotNull WndDialogContract getContract(@NotNull Trinity.WndUseTrinity obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndDialogContract contract = super.getContract(obj, ctx, profile);

        for (RedButton button : obj.buttons) {
            contract.actions.add(new WndDialogContract.Action(
                    button.LocalizedStringText(),
                    false,
                    button.activeForNetwork(),
                    ImageIcon.fromImage(button.icon(), ctx, profile)
            ));
        }

        return contract;
    }
}
