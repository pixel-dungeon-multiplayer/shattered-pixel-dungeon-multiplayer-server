package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows;

import com.nikita22007.multiplayer.utils.text.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
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

        JSONObject args = new JSONObject();

        JSONObject title = new JSONObject();
        title.put("text", ctx.serialize(params.title, profile));
        title.put("color", params.titleColor == null ? JSONObject.NULL : params.titleColor);
        args.put("title", title);

        args.put("message", ctx.serialize(params.message, profile));

        JSONArray options = new JSONArray();
        for (int i = 0; i < params.options.size(); i++) {
            LocalizedString option = params.options.get(i);
            JSONObject optionObj = new JSONObject();
            optionObj.put("text", ctx.serialize(option, profile));
            optionObj.put("has_info", obj.hasInfoForNetwork(i));
            optionObj.put("enabled", obj.enabledForNetwork(i));
            options.put(optionObj);
        }
        args.put("options", options);

        args.put("title_icon", titleIcon(params));
        args.put("layout", layout());
        return args;
    }

    private @NotNull JSONObject layout() {
        JSONObject layout = new JSONObject();
        layout.put("expand_in_landscape", false);
        layout.put("highlighting", true);
        return layout;
    }

    private @NotNull JSONObject titleIcon(@NotNull WndOptions.WndOptionsParams params) {
        JSONObject icon = new JSONObject();
        JSONObject iconArgs = new JSONObject();

        if (params.itemSpriteImage != null) {
            icon.put("type", "item_sprite");
            iconArgs.put("image", params.itemSpriteImage);
            if (params.itemSpriteGlowing != null) {
                iconArgs.put("glowing", params.itemSpriteGlowing.toJsonObject());
            }
        } else if (params.charSprite != null) {
            icon.put("type", "char_sprite");
            String spriteAsset = params.charSprite.getSpriteAsset();
            if (spriteAsset != null) {
                iconArgs.put("sprite_asset", spriteAsset);
            } else {
                iconArgs.put("sprite_class", params.charSprite.spriteName());
            }
        } else {
            icon.put("type", "none");
        }

        icon.put("args", iconArgs);
        return icon;
    }
}
