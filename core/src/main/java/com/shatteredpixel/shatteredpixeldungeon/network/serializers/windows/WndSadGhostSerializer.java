package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndSadGhost;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class WndSadGhostSerializer extends WindowSerializer<WndSadGhost> {

    @Override
    protected @NotNull String type() {
        return "sad_ghost";
    }

    @Override
    protected @Nullable JSONObject args(@NotNull WndSadGhost obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject args = new JSONObject();
        args.put("type", obj.questType());
        args.put("weapon", ctx.serialize(Ghost.Quest.weapon, "inventory"));
        args.put("armor", ctx.serialize(Ghost.Quest.armor, "inventory"));
        return args;
    }
}
