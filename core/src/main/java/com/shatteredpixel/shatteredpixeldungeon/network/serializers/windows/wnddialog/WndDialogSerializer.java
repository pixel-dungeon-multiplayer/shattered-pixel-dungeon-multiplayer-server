package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wnddialog;

import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.WindowSerializer;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public abstract class WndDialogSerializer<T extends Window> extends WindowSerializer<T> {

    @Override
    protected final @NotNull String type() {
        return "dialog";
    }

    @Override
    protected final @NotNull JSONObject args(@NotNull T obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        return getContract(obj, ctx, profile).toJson(ctx, profile);
    }

    protected abstract @NotNull WndDialogContract getContract(@NotNull T obj, @NotNull SerializationContext ctx, @NotNull String profile);
}
