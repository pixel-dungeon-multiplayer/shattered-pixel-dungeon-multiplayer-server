package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wndoptions;

import com.nikita22007.multiplayer.utils.text.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoMob;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

final class WndOptionContract {

    @Nullable LocalizedString titleText = LocalizedString.raw("Untitled");
    @Nullable Integer titleColor = null;
    @NotNull LocalizedString message = LocalizedString.EMPTY;
    @NotNull List<Option> options = new ArrayList<>();
    @NotNull TitleIcon titleIcon = TitleIcon.none();
    @NotNull Layout layout = Layout.options();

    @NotNull JSONObject toJson(@NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject args = new JSONObject();

        JSONObject title = new JSONObject();
        {
            title.put("text", ctx.serialize(titleText, profile));
            title.put("color", titleColor == null ? JSONObject.NULL : titleColor);
            title.put("title_icon", titleIcon.toJson());
        }
        args.put("title", title);
        args.put("message", ctx.serialize(message, profile));
        args.put("options", options(ctx, profile));
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

    public void fillFromTitlebar(@Nullable Component titlebar, SerializationContext ctx, @NotNull String profile) {
        if (titlebar == null) {
            return;
        }
        if (titlebar instanceof WndInfoMob.MobTitle) {
            this.titleIcon = TitleIcon.mobTitleBar(((WndInfoMob.MobTitle) titlebar).mob, ctx, profile);
            this.titleColor = ((WndInfoMob.MobTitle) titlebar).color;
            this.titleText = ((WndInfoMob.MobTitle) titlebar).title;
        } else if (titlebar instanceof IconTitle) {
            this.titleIcon = TitleIcon.fromImage(((IconTitle) titlebar).imIcon);
            this.titleColor = ((IconTitle) titlebar).color;
            this.titleText = ((IconTitle) titlebar).text;
        } else {
            throw new IllegalArgumentException("Unknown titlebar type: " + titlebar.getClass().getName());
        }
    }

    static final class Option {
        private final @NotNull LocalizedString text;
        private final boolean hasInfo;
        private final boolean enabled;

        Option(@NotNull LocalizedString text) {
            this.text = text;
            this.hasInfo = false;
            this.enabled = true;
        }

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

        static @NotNull Layout titledMessage(boolean highlighting) {
            return new Layout(true, highlighting);
        }

        @NotNull JSONObject toJson() {
            JSONObject json = new JSONObject();
            json.put("expand_in_landscape", expandInLandscape);
            json.put("highlighting", highlighting);
            return json;
        }
    }

    static final class TitleIcon {
        private final @NotNull JSONObject json;

        private TitleIcon(@NotNull String type, @NotNull JSONObject args) {
            args.put("type", type);
            this.json = args;
        }

        static @NotNull TitleIcon fromImage(@Nullable Image image) {
            if (image == null) {
                return none();
            }
            if (image instanceof CharSprite) {
                return charSprite(((CharSprite) image).getSpriteAsset(), ((CharSprite) image).spriteName());
            } else if (image instanceof ItemSprite) {
                return itemSprite((ItemSprite) image);
            }
            throw new IllegalArgumentException("Unsupported image type: " + image.getClass().getName());
        }

        static @NotNull TitleIcon none() {
            return new TitleIcon("none", new JSONObject());
        }

        static @NotNull TitleIcon itemSprite(ItemSprite sprite) {
            if (sprite == null) {
                return none();
            }
            return itemSprite(sprite.image(), sprite.glowing());
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

        static @NotNull TitleIcon mobTitleBar(@NotNull Mob mob, @NotNull SerializationContext ctx, @NotNull String profile) {
            JSONObject args = new JSONObject();
            args.put("mob", ctx.serialize(mob));
            JSONArray buffs = new JSONArray();
            for (Buff buff : mob.buffs()) {
                buffs.put(ctx.serialize(buff, profile));
            }
            args.put("buff", buffs);
            return new TitleIcon("mob_titlebar", args);
        }

        @NotNull JSONObject toJson() {
            return json;
        }
    }
}


