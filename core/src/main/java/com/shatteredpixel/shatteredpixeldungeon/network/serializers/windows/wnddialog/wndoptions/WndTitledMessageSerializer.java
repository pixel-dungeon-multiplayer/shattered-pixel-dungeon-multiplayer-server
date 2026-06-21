package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wnddialog.wndoptions;

import com.nikita22007.multiplayer.utils.text.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.ui.ImageIcon;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wnddialog.WndDialogContract;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wnddialog.WndDialogSerializer;
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

