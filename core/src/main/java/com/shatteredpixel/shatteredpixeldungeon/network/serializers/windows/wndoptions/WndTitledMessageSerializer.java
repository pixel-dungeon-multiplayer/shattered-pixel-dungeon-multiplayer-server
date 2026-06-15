package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wndoptions;

import com.nikita22007.multiplayer.utils.text.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.WindowSerializer;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.Objects;

public class WndTitledMessageSerializer<T extends WndTitledMessage> extends WindowSerializer<T> {

    @Override
    protected @NotNull String type() {
        return "wnd_option";
    }

    @Override
    protected @Nullable JSONObject args(@NotNull T obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndOptionContract contract = new WndOptionContract();
        contract.titleText = obj.title() == null ? LocalizedString.raw("") : Objects.requireNonNull(obj.title());
        contract.message = obj.message() == null ? LocalizedString.raw("") : Objects.requireNonNull(obj.message());
        contract.titleIcon = WndOptionContract.TitleIcon.fromImage(obj.titleIcon());
        contract.layout = WndOptionContract.Layout.titledMessage(obj.highlightingForNetwork());
        contract.fillFromTitlebar(obj.titlebar(), ctx, profile);

        return contract.toJson(ctx, profile);
    }
}


