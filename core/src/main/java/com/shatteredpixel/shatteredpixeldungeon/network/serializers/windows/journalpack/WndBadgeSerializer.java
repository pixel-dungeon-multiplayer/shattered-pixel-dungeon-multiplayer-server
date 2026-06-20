package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.journalpack;

import com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.WindowSerializer;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBadge;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class WndBadgeSerializer extends WindowSerializer<WndBadge> {

    @Override
    protected @NotNull String type() {
        return "badge";
    }

    @Override
    protected @Nullable JSONObject args(@NotNull WndBadge obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject args = new JSONObject();
        args.put("id", obj.badge().name());
        args.put("image", obj.badge().image);
        args.put("title", ctx.serialize(obj.badge().title(), profile));
        args.put("description", ctx.serialize(obj.desc(), profile));
        args.put("unlocked", obj.unlocked());
        return args;
    }
}



