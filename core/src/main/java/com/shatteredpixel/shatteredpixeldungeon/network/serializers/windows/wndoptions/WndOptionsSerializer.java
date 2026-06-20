package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wndoptions;

import com.nikita22007.multiplayer.utils.text.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.ui.ImageIcon;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.WindowSerializer;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Image;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class WndOptionsSerializer<T extends WndOptions> extends WindowSerializer<T> {

    @Override
    protected @NotNull String type() {
        return "wnd_option";
    }

    @Override
    protected @Nullable JSONObject args(@NotNull T obj, @NotNull SerializationContext ctx, @NotNull String profile) {
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
                    obj.enabledForNetwork(i),
                    optionIcon(obj, i, ctx, profile)));
        }

        return contract.toJson(ctx, profile);
    }

    private @NotNull ImageIcon titleIcon(@NotNull WndOptions.WndOptionsParams params) {
        if (params.itemSpriteImage != null) {
            return ImageIcon.itemSprite(params.itemSpriteImage, params.itemSpriteGlowing);
        }
        if (params.charSprite != null) {
            return ImageIcon.charSprite(
                    params.charSprite.getSpriteAsset(),
                    params.charSprite.spriteName());
        }
        return ImageIcon.none();
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
