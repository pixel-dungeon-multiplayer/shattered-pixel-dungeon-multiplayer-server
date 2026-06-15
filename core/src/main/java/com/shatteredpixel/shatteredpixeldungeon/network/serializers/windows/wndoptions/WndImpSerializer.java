package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wndoptions;

import com.nikita22007.multiplayer.utils.text.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.WindowSerializer;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndImp;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

public class WndImpSerializer extends WindowSerializer<WndImp> {

    @Override
    protected @NotNull String type() {
        return "imp";
    }


    protected @Nullable JSONObject args(@NotNull WndImp obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndOptionContract contract = new WndOptionContract();
        contract.fillFromTitlebar(obj.titlebar, ctx, profile);
        contract.message = obj.message.LocalizedStringText();
        contract.layout = WndOptionContract.Layout.options();
        contract.options = List.of(new WndOptionContract.Option(obj.buttonText));

        return contract.toJson(ctx, profile);
    }
}



