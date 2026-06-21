package com.shatteredpixel.shatteredpixeldungeon.network.serializers.ui;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.TerrainFeaturesTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.watabou.noosa.Image;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

public final class ImageIcon {
    private final @NotNull JSONObject json;

    private ImageIcon(@NotNull String type, @NotNull JSONObject args) {
        args.put("type", type);
        this.json = args;
    }

    public static @NotNull ImageIcon fromImage(@Nullable Image image, SerializationContext ctx, String profile) {
        if (image == null) {
            return none();
        }
        if (image instanceof CharSprite) {
            return charSprite(((CharSprite) image).getSpriteAsset(), ((CharSprite) image).spriteName());
        } else if (image instanceof ItemSprite) {
            return itemSprite((ItemSprite) image);
        } else if (image instanceof BuffIcon) {
            return buffIcon((BuffIcon) image, ctx, profile);
        } else if (image instanceof TerrainFeaturesTilemap.TileImage) {
            return tileImage((TerrainFeaturesTilemap.TileImage) image, ctx, profile);
        } else {
            return new ImageIcon("asset_image", (JSONObject) ctx.serialize(image, profile));
        }
    }

    public static @NotNull ImageIcon none() {
        return new ImageIcon("none", new JSONObject());
    }

    public static @NotNull ImageIcon uiIcon(@NotNull Icons icon) {
        JSONObject args = new JSONObject();
        args.put("name", icon.name());
        return new ImageIcon("ui_icon", args);
    }

    public static @NotNull ImageIcon itemSprite(ItemSprite sprite) {
        if (sprite == null) {
            return none();
        }
        return itemSprite(sprite.image(), sprite.glowing());
    }

    public static @NotNull ImageIcon itemSprite(int image, @Nullable ItemSprite.Glowing glowing) {
        JSONObject args = new JSONObject();
        args.put("image", image);
        if (glowing != null) {
            args.put("glowing", glowing.toJsonObject());
        }
        return new ImageIcon("item_sprite", args);
    }

    private static @NotNull ImageIcon tileImage(TerrainFeaturesTilemap.TileImage image, SerializationContext ctx, String profile) {
        JSONObject args = new JSONObject();
        args.put("tile", image.tileVisual);
        return new ImageIcon("tile_image", args);
    }

    public static @NotNull ImageIcon charSprite(@Nullable String spriteAsset, @NotNull String spriteClass) {
        JSONObject args = new JSONObject();
        if (spriteAsset != null) {
            args.put("sprite_asset", spriteAsset);
        } else {
            args.put("sprite_class", spriteClass);
        }
        return new ImageIcon("char_sprite", args);
    }

    public static @NotNull ImageIcon charSprite(@NotNull String spriteClass) {
        return charSprite(null, spriteClass);
    }

    public static @NotNull ImageIcon mobTitleBar(@NotNull Mob mob, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject args = new JSONObject();
        args.put("mob", ctx.serialize(mob, profile));
        JSONArray buffs = new JSONArray();
        for (Buff buff : mob.buffs()) {
            buffs.put(ctx.serialize(buff, profile));
        }
        args.put("buff", buffs);
        return new ImageIcon("mob_titlebar", args);
    }

    public static @NotNull ImageIcon buffIcon(BuffIcon image, SerializationContext ctx, String profile) {
        JSONObject args = new JSONObject();
        args.put("buff", ctx.serialize(image.buff, profile));
        return new ImageIcon("buff_titlebar", args);
    }

    public @NotNull JSONObject toJson() {
        return json;
    }
}
