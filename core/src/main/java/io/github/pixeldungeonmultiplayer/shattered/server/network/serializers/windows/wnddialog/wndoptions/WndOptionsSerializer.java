package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.wnddialog.wndoptions;

import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.ui.ImageIcon;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.wnddialog.WndDialogContract;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.wnddialog.WndDialogSerializer;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Image;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class WndOptionsSerializer<T extends WndOptions> extends WndDialogSerializer<T> {

    @Override
    protected @NotNull WndDialogContract getContract(@NotNull T obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndDialogContract contract = new WndDialogContract();
        contract.fillFromTitlebar(obj.titlebar, ctx, profile);
        contract.message = obj.message.LocalizedStringText();
        contract.layout = WndDialogContract.Layout.options();
        for (int i = 0; i < obj.optionButtons.length; i++) {
            contract.actions.add(new WndDialogContract.Action(
                    obj.optionButtons[i].LocalizedStringText(),
                    obj.hasInfoForNetwork(i),
                    obj.enabledForNetwork(i),
                    optionIcon(obj, i, ctx, profile)));
        }

        return contract;
    }

    private @NotNull ImageIcon optionIcon(
            @NotNull WndOptions obj,
            int index,
            @NotNull SerializationContext ctx,
            @NotNull String profile) {
        Image icon = obj.optionIcon(index);
        return ImageIcon.fromImage(icon, ctx, profile);
    }
}
