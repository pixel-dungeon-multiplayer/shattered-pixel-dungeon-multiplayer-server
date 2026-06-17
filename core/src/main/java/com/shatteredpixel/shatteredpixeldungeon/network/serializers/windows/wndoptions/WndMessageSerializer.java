package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wndoptions;

import com.nikita22007.multiplayer.utils.text.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.WindowSerializer;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class WndMessageSerializer extends WindowSerializer<WndMessage> {

    @Override
    protected @NotNull String type() {
        return "wnd_option";
    }

    @Override
    protected @Nullable JSONObject args(@NotNull WndMessage obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndOptionContract contract = new WndOptionContract();
        contract.titleText = LocalizedString.raw("");
        contract.message = obj.text();
        contract.layout = WndOptionContract.Layout.titledMessage();
        return contract.toJson(ctx, profile);
    }
}


