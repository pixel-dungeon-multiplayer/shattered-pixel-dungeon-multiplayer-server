package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows;

import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndQuest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class WndQuestSerializer extends WindowSerializer<WndQuest> {

    @Override
    protected @NotNull String type() {
        return "wnd_option";
    }

    @Override
    protected @Nullable JSONObject args(@NotNull WndQuest obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndOptionContract contract = new WndOptionContract();
        contract.titleText = obj.charName();
        contract.message = obj.text();
        contract.titleIcon = WndOptionContract.TitleIcon.charSprite(obj.spriteName());
        contract.layout = WndOptionContract.Layout.titledMessage();
        return contract.toJson(ctx, profile);
    }
}
