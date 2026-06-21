package com.shatteredpixel.shatteredpixeldungeon.network.serializers;

import com.nikita22007.multiplayer.utils.text.LocalizedString;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class LocalizedStringSerializer implements Serializer<LocalizedString> {
    @Override
    public JSONObject serialize(@NotNull LocalizedString obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        return obj.toJsonObject();
    }
}
