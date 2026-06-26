package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.wnddialog.wndoptions;

import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.ui.ImageIcon;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.wnddialog.WndDialogContract;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.wnddialog.WndDialogSerializer;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem;
import org.jetbrains.annotations.NotNull;

public class WndInfoItemSerializer<T extends WndInfoItem> extends WndDialogSerializer<T> {

    @Override
    protected @NotNull WndDialogContract getContract(@NotNull T obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndDialogContract contract = new WndDialogContract();
        contract.fillFromTitlebar(obj.titlebar, ctx, profile);
        contract.message = obj.txtInfo.LocalizedStringText();
        contract.layout = WndDialogContract.Layout.titledMessage();
        for (RedButton button : obj.actionsForNetwork()) {
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

