package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows;

import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.Serializer;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public abstract class WindowSerializer<T extends Window> implements Serializer<T> {

    @Override
    public final Object serialize(@NotNull T obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject envelope = new JSONObject();
        envelope.put("id", obj.getId());
        envelope.put("type", type());

        JSONObject args = args(obj, ctx, profile);
        if (args != null && args.length() > 0) {
            envelope.put("args", args);
        }
        return envelope;
    }

    protected abstract @NotNull String type();

    protected abstract @Nullable JSONObject args(@NotNull T obj, @NotNull SerializationContext ctx, @NotNull String profile);
}
