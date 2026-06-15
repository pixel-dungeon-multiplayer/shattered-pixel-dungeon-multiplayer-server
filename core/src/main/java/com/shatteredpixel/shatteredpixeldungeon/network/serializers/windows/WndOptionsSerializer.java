package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows;

import com.nikita22007.multiplayer.utils.text.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class WndOptionsSerializer extends WindowSerializer<WndOptions> {

    @Override
    protected @NotNull String type() {
        return "wnd_option";
    }

    @Override
    protected @Nullable JSONObject args(@NotNull WndOptions obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndOptions.WndOptionsParams params = obj.params();
        if (params == null) {
            return null;
        }

        WndOptionContract contract = new WndOptionContract();
        contract.titleText = params.title;
        contract.titleColor = params.titleColor;
        contract.message = params.message;
        contract.layout = WndOptionContract.Layout.options();
        contract.titleIcon = titleIcon(params);
        for (int i = 0; i < params.options.size(); i++) {
            LocalizedString option = params.options.get(i);
            contract.options.add(new WndOptionContract.Option(
                    option,
                    obj.hasInfoForNetwork(i),
                    obj.enabledForNetwork(i)));
        }

        return contract.toJson(ctx, profile);
    }

    private @NotNull WndOptionContract.TitleIcon titleIcon(@NotNull WndOptions.WndOptionsParams params) {
        if (params.itemSpriteImage != null) {
            return WndOptionContract.TitleIcon.itemSprite(params.itemSpriteImage, params.itemSpriteGlowing);
        }
        if (params.charSprite != null) {
            return WndOptionContract.TitleIcon.charSprite(
                    params.charSprite.getSpriteAsset(),
                    params.charSprite.spriteName());
        }
        return WndOptionContract.TitleIcon.none();
    }
}
