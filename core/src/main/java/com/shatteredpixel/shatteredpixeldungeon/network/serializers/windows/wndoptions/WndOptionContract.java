package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wndoptions;

import com.nikita22007.multiplayer.utils.text.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.ui.ImageIcon;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoMob;
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
    @NotNull ImageIcon titleIcon = ImageIcon.none();
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
            optionObj.put("icon", option.icon.toJson());
            serialized.put(optionObj);
        }
        return serialized;
    }

    public void fillFromTitlebar(@Nullable Component titlebar, @NotNull SerializationContext ctx, @NotNull String profile) {
        if (titlebar == null) {
            return;
        }
        if (titlebar instanceof WndInfoMob.MobTitle) {
            this.titleIcon = ImageIcon.mobTitleBar(((WndInfoMob.MobTitle) titlebar).mob, ctx, profile);
            this.titleColor = ((WndInfoMob.MobTitle) titlebar).color;
            this.titleText = ((WndInfoMob.MobTitle) titlebar).title;
        } else if (titlebar instanceof IconTitle) {
            this.titleIcon = ImageIcon.fromImage(((IconTitle) titlebar).imIcon, ctx, profile);
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
        private final @NotNull ImageIcon icon;

        Option(@NotNull LocalizedString text) {
            this(text, false, true);
        }

        Option(@NotNull LocalizedString text, boolean hasInfo, boolean enabled) {
            this(text, hasInfo, enabled, ImageIcon.none());
        }

        Option(@NotNull LocalizedString text, @NotNull ImageIcon icon) {
            this(text, false, true, icon);
        }

        Option(@NotNull LocalizedString text, boolean hasInfo, boolean enabled, @NotNull ImageIcon icon) {
            this.text = text;
            this.hasInfo = hasInfo;
            this.enabled = enabled;
            this.icon = icon;
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

}

