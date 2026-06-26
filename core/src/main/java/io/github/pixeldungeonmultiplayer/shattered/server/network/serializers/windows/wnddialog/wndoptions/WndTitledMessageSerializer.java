package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.wnddialog.wndoptions;

import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.ui.ImageIcon;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.wnddialog.WndDialogContract;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.wnddialog.WndDialogSerializer;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class WndTitledMessageSerializer<T extends WndTitledMessage> extends WndDialogSerializer<T> {

    @Override
    protected @NotNull WndDialogContract getContract(@NotNull T obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndDialogContract contract = new WndDialogContract();
        contract.titleText = obj.title() == null ? LocalizedString.raw("") : Objects.requireNonNull(obj.title());
        contract.message = obj.message() == null ? LocalizedString.raw("") : Objects.requireNonNull(obj.message());
        contract.titleIcon = ImageIcon.fromImage(obj.titleIcon(), ctx, profile);
        contract.layout = WndDialogContract.Layout.titledMessage(obj.highlightingForNetwork());
        contract.fillFromTitlebar(obj.titlebar(), ctx, profile);

        return contract;
    }
}

