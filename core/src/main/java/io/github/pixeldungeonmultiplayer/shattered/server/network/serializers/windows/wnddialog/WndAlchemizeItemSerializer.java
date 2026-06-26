package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.wnddialog;

import com.shatteredpixel.shatteredpixeldungeon.items.spells.Alchemize;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.ui.ImageIcon;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.wnddialog.wndoptions.WndInfoItemSerializer;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import org.jetbrains.annotations.NotNull;

public class WndAlchemizeItemSerializer extends WndInfoItemSerializer<Alchemize.WndAlchemizeItem> {

    @Override
    protected @NotNull WndDialogContract getContract(@NotNull Alchemize.WndAlchemizeItem obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndDialogContract contract = super.getContract(obj, ctx, profile);
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



