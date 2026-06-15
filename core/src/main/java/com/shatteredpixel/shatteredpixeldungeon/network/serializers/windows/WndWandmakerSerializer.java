package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Wandmaker;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndWandmaker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class WndWandmakerSerializer extends WindowSerializer<WndWandmaker> {

    @Override
    protected @NotNull String type() {
        return "wandmaker";
    }

    @Override
    protected @Nullable JSONObject args(@NotNull WndWandmaker obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject args = new JSONObject();
        args.put("wand1", ctx.serialize(Wandmaker.Quest.wand1, "inventory"));
        args.put("wand2", ctx.serialize(Wandmaker.Quest.wand2, "inventory"));
        args.put("quest_item", ctx.serialize(obj.questItem(), "inventory"));
        args.put("quest_item_class", obj.questItem().getClass().getName());
        return args;
    }
}
