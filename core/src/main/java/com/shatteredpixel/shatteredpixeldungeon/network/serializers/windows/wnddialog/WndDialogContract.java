package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wnddialog;

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

public final class WndDialogContract {

    public @Nullable LocalizedString titleText = LocalizedString.raw("Untitled");
    public @Nullable Integer titleColor = null;
    public @NotNull LocalizedString message = LocalizedString.EMPTY;
    public @NotNull List<@NotNull ItemSlot> itemSlots = new ArrayList<>();
    public @NotNull List<@NotNull Action> actions = new ArrayList<>();
    public @NotNull ImageIcon titleIcon = ImageIcon.none();
    public @Nullable TopRightButton topRightButton = null;
    public @NotNull Layout layout = Layout.options();

    public @NotNull JSONObject toJson(@NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject args = new JSONObject();

        JSONObject title = new JSONObject();
        {
            title.put("text", ctx.serialize(titleText, profile));
            title.put("color", titleColor == null ? JSONObject.NULL : titleColor);
            title.put("title_icon", titleIcon.toJson());
        }
        args.put("title", title);
        args.put("message", ctx.serialize(message, profile));
        args.put("item_slots", itemSlots(ctx));
        args.put("actions", actions(ctx, profile));
        args.put("top_right_button", topRightButton == null ? JSONObject.NULL : topRightButton.toJson(ctx, profile));
        args.put("layout", layout.toJson());

        return args;
    }

    private @NotNull JSONArray itemSlots(@NotNull SerializationContext ctx) {
        JSONArray serialized = new JSONArray();
        for (ItemSlot itemSlot : itemSlots) {
            JSONObject itemSlotObj = new JSONObject();
            itemSlotObj.put("item", itemSlot.item == null ? JSONObject.NULL : ctx.serialize(itemSlot.item, "inventory"));
            itemSlotObj.put("enabled", itemSlot.enabled);
            serialized.put(itemSlotObj);
        }
        return serialized;
    }

    private @NotNull JSONArray actions(@NotNull SerializationContext ctx, @NotNull String profile) {
        JSONArray serialized = new JSONArray();
        for (Action action : actions) {
            JSONObject actionObj = new JSONObject();
            actionObj.put("text", ctx.serialize(action.text, profile));
            actionObj.put("has_info", action.hasInfo);
            actionObj.put("enabled", action.enabled);
            actionObj.put("icon", action.icon.toJson());
            actionObj.put("font_size", action.fontSize);
            serialized.put(actionObj);
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
            if (((IconTitle) titlebar).iconsIcon != null) {
                this.titleIcon = ImageIcon.uiIcon(((IconTitle) titlebar).iconsIcon);
            } else {
                this.titleIcon = ImageIcon.fromImage(((IconTitle) titlebar).imIcon, ctx, profile);
            }
            this.titleColor = ((IconTitle) titlebar).color;
            this.titleText = ((IconTitle) titlebar).text;
        } else {
            throw new IllegalArgumentException("Unknown titlebar type: " + titlebar.getClass().getName());
        }
    }

    public static final class Action {
        private final @NotNull LocalizedString text;
        private final boolean hasInfo;
        private final boolean enabled;
        private final @NotNull ImageIcon icon;
        private final int fontSize;

        public Action(@NotNull LocalizedString text) {
            this(text, false, true);
        }

        public Action(@NotNull LocalizedString text, boolean hasInfo, boolean enabled) {
            this(text, hasInfo, enabled, ImageIcon.none());
        }

        public Action(@NotNull LocalizedString text, @NotNull ImageIcon icon) {
            this(text, false, true, icon);
        }

        public Action(@NotNull LocalizedString text, boolean hasInfo, boolean enabled, @NotNull ImageIcon icon) {
            this(text, hasInfo, enabled, icon, 9);
        }

        public Action(@NotNull LocalizedString text, boolean hasInfo, boolean enabled, @NotNull ImageIcon icon, int fontSize) {
            this.text = text;
            this.hasInfo = hasInfo;
            this.enabled = enabled;
            this.icon = icon;
            this.fontSize = fontSize;
        }
    }

    public static final class ItemSlot {
        private final @Nullable com.shatteredpixel.shatteredpixeldungeon.items.Item item;
        private final boolean enabled;

        public ItemSlot(
                @Nullable com.shatteredpixel.shatteredpixeldungeon.items.Item item,
                boolean enabled) {
            this.item = item;
            this.enabled = enabled;
        }
    }

    public static final class TopRightButton {
        private final @NotNull LocalizedString text;
        private final boolean enabled;
        private final @NotNull ImageIcon icon;

        public TopRightButton(@NotNull LocalizedString text, @NotNull ImageIcon icon) {
            this(text, true, icon);
        }

        public TopRightButton(@NotNull LocalizedString text, boolean enabled, @NotNull ImageIcon icon) {
            this.text = text;
            this.enabled = enabled;
            this.icon = icon;
        }

        public @NotNull JSONObject toJson(@NotNull SerializationContext ctx, @NotNull String profile) {
            JSONObject json = new JSONObject();
            json.put("text", ctx.serialize(text, profile));
            json.put("enabled", enabled);
            json.put("icon", icon.toJson());
            return json;
        }
    }

    public static final class Layout {
        private final boolean expandInLandscape;
        private final boolean highlighting;

        private Layout(boolean expandInLandscape, boolean highlighting) {
            this.expandInLandscape = expandInLandscape;
            this.highlighting = highlighting;
        }

        public static @NotNull Layout options() {
            return new Layout(false, true);
        }

        public static @NotNull Layout titledMessage() {
            return new Layout(true, true);
        }

        public static @NotNull Layout titledMessage(boolean highlighting) {
            return new Layout(true, highlighting);
        }

        public @NotNull JSONObject toJson() {
            JSONObject json = new JSONObject();
            json.put("expand_in_landscape", expandInLandscape);
            json.put("highlighting", highlighting);
            return json;
        }
    }

}
