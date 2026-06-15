package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows;

import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoCell;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class WndInfoCellSerializer extends WindowSerializer<WndInfoCell> {

    @Override
    protected @NotNull String type() {
        return "info_cell";
    }

    @Override
    protected @Nullable JSONObject args(@NotNull WndInfoCell obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject args = new JSONObject();
        args.put("desc", ctx.serialize(obj.desc(), profile));
        args.put("title_bar", obj.titlebar().toJson());
        return args;
    }
}
