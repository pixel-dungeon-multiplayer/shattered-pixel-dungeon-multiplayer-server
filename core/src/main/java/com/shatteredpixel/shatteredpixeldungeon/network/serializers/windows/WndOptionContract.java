package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows;

import com.nikita22007.multiplayer.utils.text.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

final class WndOptionContract {

    @NotNull LocalizedString titleText = LocalizedString.raw("Untitled");
    @Nullable Integer titleColor = null;
    @NotNull LocalizedString message = LocalizedString.raw("MissingNo");
    @NotNull List<Option> options = new ArrayList<>();
    @NotNull TitleIcon titleIcon = TitleIcon.none();
    @NotNull Layout layout = Layout.options();

    @NotNull JSONObject toJson(@NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject args = new JSONObject();

        JSONObject title = new JSONObject();
        title.put("text", ctx.serialize(titleText, profile));
        title.put("color", titleColor == null ? JSONObject.NULL : titleColor);
        args.put("title", title);

        args.put("message", ctx.serialize(message, profile));
        args.put("options", options(ctx, profile));
        args.put("title_icon", titleIcon.toJson());
        args.put("layout", layout.toJson());

        return args;
    }

    private @NotNull JSONArray options(@NotNull SerializationContext ctx, @NotNull String profile) {
        JSONArray serialized = new JSONArray();
        for (Option option : options) {
            JSONObject optionObj = new JSONObject();
            optionObj.put("text", ctx.serialize(option.text, profile));
            optionObj.put("has_info", option.hasInfo);
            optionObj.put("enabled", option.enabled);
            serialized.put(optionObj);
        }
        return serialized;
    }

    static final class Option {
        private final @NotNull LocalizedString text;
        private final boolean hasInfo;
        private final boolean enabled;

        Option(@NotNull LocalizedString text, boolean hasInfo, boolean enabled) {
            this.text = text;
            this.hasInfo = hasInfo;
            this.enabled = enabled;
        }
    }

    static final class Layout {
        private final boolean expandInLandscape;
        private final boolean highlighting;

        private Layout(boolean expandInLandscape, boolean highlighting) {
            this.expandInLandscape = expandInLandscape;
            this.highlighting = highlighting;
        }

        static @NotNull Layout options() {
            return new Layout(false, true);
        }

        static @NotNull Layout titledMessage() {
            return new Layout(true, true);
        }

        @NotNull JSONObject toJson() {
            JSONObject json = new JSONObject();
            json.put("expand_in_landscape", expandInLandscape);
            json.put("highlighting", highlighting);
            return json;
        }
    }

    static final class TitleIcon {
        private final @NotNull String type;
        private final @NotNull JSONObject args;

        private TitleIcon(@NotNull String type, @NotNull JSONObject args) {
            this.type = type;
            this.args = args;
        }

        static @NotNull TitleIcon none() {
            return new TitleIcon("none", new JSONObject());
        }

        static @NotNull TitleIcon itemSprite(int image, @Nullable ItemSprite.Glowing glowing) {
            JSONObject args = new JSONObject();
            args.put("image", image);
            if (glowing != null) {
                args.put("glowing", glowing.toJsonObject());
            }
            return new TitleIcon("item_sprite", args);
        }

        static @NotNull TitleIcon charSprite(@Nullable String spriteAsset, @NotNull String spriteClass) {
            JSONObject args = new JSONObject();
            if (spriteAsset != null) {
                args.put("sprite_asset", spriteAsset);
            } else {
                args.put("sprite_class", spriteClass);
            }
            return new TitleIcon("char_sprite", args);
        }

        static @NotNull TitleIcon charSprite(@NotNull String spriteClass) {
            return charSprite(null, spriteClass);
        }

        @NotNull JSONObject toJson() {
            JSONObject json = new JSONObject();
            json.put("type", type);
            json.put("args", args);
            return json;
        }
    }
}
