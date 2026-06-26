package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers;

import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class LocalizedStringSerializer implements Serializer<LocalizedString> {
    @Override
    public JSONObject serialize(@NotNull LocalizedString obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        return obj.toJsonObject();
    }
}
