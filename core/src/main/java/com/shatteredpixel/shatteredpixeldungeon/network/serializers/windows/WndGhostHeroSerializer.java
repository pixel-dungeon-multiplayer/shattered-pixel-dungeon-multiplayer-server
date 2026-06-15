package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows;

import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class WndGhostHeroSerializer extends WindowSerializer<DriedRose.WndGhostHero> {

    @Override
    protected @NotNull String type() {
        return "ghost_hero";
    }

    @Override
    protected @Nullable JSONObject args(@NotNull DriedRose.WndGhostHero obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject args = new JSONObject();
        args.put("weapon", ctx.serialize(obj.weaponItem(), "inventory"));
        args.put("armor", ctx.serialize(obj.armorItem(), "inventory"));
        args.put("rose", ctx.serialize(obj.rose(), "inventory"));
        args.put("title", ctx.serialize(obj.title(), profile));
        args.put("message", ctx.serialize(obj.message(), profile));
        return args;
    }
}
