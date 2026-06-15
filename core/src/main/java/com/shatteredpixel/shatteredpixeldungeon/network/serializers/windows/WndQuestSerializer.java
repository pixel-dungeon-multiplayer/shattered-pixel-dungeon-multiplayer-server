package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows;

import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndQuest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class WndQuestSerializer extends WindowSerializer<WndQuest> {

    @Override
    protected @NotNull String type() {
        return "quest";
    }

    @Override
    protected @Nullable JSONObject args(@NotNull WndQuest obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject args = new JSONObject();
        args.put("sprite_name", obj.spriteName());
        args.put("char_name", ctx.serialize(obj.charName(), profile));
        args.put("text", ctx.serialize(obj.text(), profile));
        return args;
    }
}
