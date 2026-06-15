package com.shatteredpixel.shatteredpixeldungeon.network.actions.serializers;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.network.actions.WindowAction;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.nikita22007.multiplayer.utils.text.LocalizedString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public final class WindowActionSerializers {

    private WindowActionSerializers() {
        throw new RuntimeException();
    }

    public static abstract class Base<T extends WindowAction> extends NetworkActionSerializer<T> {
        @Override
        protected final @Nullable JSONObject serializeInternal(@NotNull T obj, @NotNull SerializationContext ctx, @NotNull String profile) {
            JSONObject envelope = new JSONObject();
            envelope.put("id", obj.windowId);
            envelope.put("type", obj.type);

            JSONObject args = serializeWindowArgs(obj, ctx, profile);
            if (args != null && args.length() > 0) {
                envelope.put("args", args);
            }
            return envelope;
        }

        protected abstract @Nullable JSONObject serializeWindowArgs(@NotNull T obj, @NotNull SerializationContext ctx, @NotNull String profile);
    }

    public static class Alchemy extends Base<WindowAction.Alchemy> {
        @Override
        protected @Nullable JSONObject serializeWindowArgs(@NotNull WindowAction.Alchemy obj, @NotNull SerializationContext ctx, @NotNull String profile) {
            JSONObject args = new JSONObject();
            args.put("energy", obj.energy);
            args.put("has_toolkit", obj.hasToolkit);
            if (obj.hasToolkit) {
                args.put("toolkit_energy", obj.toolkitEnergy);
            }

            JSONArray inputsArr = new JSONArray();
            for (Item input : obj.inputs) {
                if (input == null) continue;
                inputsArr.put(ctx.serialize(input, "inventory"));
            }
            args.put("input", inputsArr);

            JSONArray outputsArr = new JSONArray();
            for (int i = 0; i < obj.outputs.size(); i++) {
                Item output = obj.outputs.get(i);
                if (output == null) continue;
                JSONObject outputObj = new JSONObject();
                outputObj.put("cost", obj.combineCosts.get(i));
                outputObj.put("enabled", obj.combineEnabled.get(i));
                outputObj.put("item", ctx.serialize(output, "inventory"));
                outputsArr.put(outputObj);
            }
            args.put("output", outputsArr);

            args.put("energyAddBlinking", obj.energyAddBlinking);
            args.put("repeat_enabled", obj.repeatEnabled);
            if (obj.createEnergy) {
                args.put("createEnergy", true);
            }
            if (obj.craftedItem) {
                args.put("craftedItem", true);
            }
            return args;
        }
    }

    public static class Bag extends Base<WindowAction.Bag> {
        @Override
        protected @Nullable JSONObject serializeWindowArgs(@NotNull WindowAction.Bag obj, @NotNull SerializationContext ctx, @NotNull String profile) {
            JSONObject args = new JSONObject();
            args.put("title", ctx.serialize(obj.title, profile));
            
            JSONArray allowedArr = new JSONArray();
            for (List<Integer> list : obj.allowedItems) {
                JSONArray itemPath = new JSONArray();
                for (int p : list) {
                    itemPath.put(p);
                }
                allowedArr.put(itemPath);
            }
            args.put("allowed_items", allowedArr);
            args.put("has_listener", obj.hasListener);
            return args;
        }
    }

    public static class ChooseSubclass extends Base<WindowAction.ChooseSubclass> {
        @Override
        protected @Nullable JSONObject serializeWindowArgs(@NotNull WindowAction.ChooseSubclass obj, @NotNull SerializationContext ctx, @NotNull String profile) {
            JSONObject args = new JSONObject();
            args.put("option1", obj.option1);
            args.put("option2", obj.option2);
            return args;
        }
    }

    public static class ClericSpells extends Base<WindowAction.ClericSpells> {
        @Override
        protected @Nullable JSONObject serializeWindowArgs(@NotNull WindowAction.ClericSpells obj, @NotNull SerializationContext ctx, @NotNull String profile) {
            JSONObject args = new JSONObject();
            args.put("info", obj.info);
            JSONArray buttons = new JSONArray();
            for (WindowAction.SpellButtonInfo button : obj.buttons) {
                JSONObject btnObj = new JSONObject();
                btnObj.put("info", button.info);
                btnObj.put("alpha", button.alpha);
                btnObj.put("tier", button.tier);
                btnObj.put("icon", button.icon);
                btnObj.put("spell_id", button.spellId);
                btnObj.put("spell_short_desc", ctx.serialize(button.spellShortDesc, profile));
                btnObj.put("spell_name", ctx.serialize(button.spellName, profile));
                buttons.put(btnObj);
            }
            args.put("buttons", buttons);
            return args;
        }
    }

    public static class InfoCell extends Base<WindowAction.InfoCell> {
        @Override
        protected @Nullable JSONObject serializeWindowArgs(@NotNull WindowAction.InfoCell obj, @NotNull SerializationContext ctx, @NotNull String profile) {
            JSONObject args = new JSONObject();
            args.put("desc", ctx.serialize(obj.desc, profile));
            args.put("title_bar", obj.titlebar.toJson());
            return args;
        }
    }

    public static class Options extends Base<WindowAction.Options> {
        @Override
        protected @Nullable JSONObject serializeWindowArgs(@NotNull WindowAction.Options obj, @NotNull SerializationContext ctx, @NotNull String profile) {
            JSONObject args = new JSONObject();
            args.put("title", ctx.serialize(obj.params.title, profile));
            args.put("title_color", obj.params.titleColor);
            args.put("message", ctx.serialize(obj.params.message, profile));
            JSONArray optionsArr = new JSONArray();
            for (LocalizedString option : obj.params.options) {
                optionsArr.put(option);
            }
            args.put("options", optionsArr);
            if (obj.params.item != null) {
                args.put("item", ctx.serialize(obj.params.item, "inventory"));
            } else if (obj.params.charSprite != null) {
                String spriteAsset = obj.params.charSprite.getSpriteAsset();
                if (spriteAsset != null) {
                    args.put("sprite_asset", spriteAsset);
                } else {
                    args.put("sprite_class", obj.params.charSprite.spriteName());
                }
            }
            if (obj.params.icon != null) {
                args.put("image", obj.params.icon.toJson());
            }
            return args;
        }
    }

    public static class Quest extends Base<WindowAction.Quest> {
        @Override
        protected @Nullable JSONObject serializeWindowArgs(@NotNull WindowAction.Quest obj, @NotNull SerializationContext ctx, @NotNull String profile) {
            JSONObject args = new JSONObject();
            args.put("sprite_name", obj.spriteName);
            args.put("char_name", ctx.serialize(obj.charName, profile));
            args.put("text", ctx.serialize(obj.text, profile));
            return args;
        }
    }

    public static class SadGhost extends Base<WindowAction.SadGhost> {
        @Override
        protected @Nullable JSONObject serializeWindowArgs(@NotNull WindowAction.SadGhost obj, @NotNull SerializationContext ctx, @NotNull String profile) {
            JSONObject args = new JSONObject();
            args.put("type", obj.questType);
            args.put("weapon", ctx.serialize(obj.weapon, "inventory"));
            args.put("armor", ctx.serialize(obj.armor, "inventory"));
            return args;
        }
    }

    public static class TradeItem extends Base<WindowAction.TradeItem> {
        @Override
        protected @Nullable JSONObject serializeWindowArgs(@NotNull WindowAction.TradeItem obj, @NotNull SerializationContext ctx, @NotNull String profile) {
            JSONObject args = new JSONObject();
            args.put("selling", obj.selling);
            args.put("price", obj.price);
            args.put("item", ctx.serialize(obj.item, "inventory"));
            if (obj.steal) {
                args.put("steal", true);
                args.put("chance", obj.chance);
                args.put("charges", obj.charges);
            }
            return args;
        }
    }

    public static class Wandmaker extends Base<WindowAction.Wandmaker> {
        @Override
        protected @Nullable JSONObject serializeWindowArgs(@NotNull WindowAction.Wandmaker obj, @NotNull SerializationContext ctx, @NotNull String profile) {
            JSONObject args = new JSONObject();
            args.put("wand1", ctx.serialize(obj.wand1, "inventory"));
            args.put("wand2", ctx.serialize(obj.wand2, "inventory"));
            args.put("quest_item", ctx.serialize(obj.questItem, "inventory"));
            args.put("quest_item_class", obj.questItemClass);
            return args;
        }
    }

    public static class Guess extends Base<WindowAction.Guess> {
        @Override
        protected @Nullable JSONObject serializeWindowArgs(@NotNull WindowAction.Guess obj, @NotNull SerializationContext ctx, @NotNull String profile) {
            JSONObject args = new JSONObject();
            args.put("item", ctx.serialize(obj.item, "inventory"));
            
            JSONArray icons = new JSONArray();
            for (int i : obj.icons) {
                icons.put(i);
            }
            args.put("icons", icons);
            
            JSONArray keys = new JSONArray();
            for (Class<?> c : obj.guessOptions) {
                keys.put(c.getName());
            }
            args.put("keys", keys);
            
            return args;
        }
    }

    public static class GhostHero extends Base<WindowAction.GhostHero> {
        @Override
        protected @Nullable JSONObject serializeWindowArgs(@NotNull WindowAction.GhostHero obj, @NotNull SerializationContext ctx, @NotNull String profile) {
            JSONObject args = new JSONObject();
            args.put("weapon", ctx.serialize(obj.weapon, "inventory"));
            args.put("armor", ctx.serialize(obj.armor, "inventory"));
            args.put("rose", ctx.serialize(obj.rose, "inventory"));
            args.put("title", ctx.serialize(obj.title, profile));
            args.put("message", ctx.serialize(obj.message, profile));
            return args;
        }
    }
}
