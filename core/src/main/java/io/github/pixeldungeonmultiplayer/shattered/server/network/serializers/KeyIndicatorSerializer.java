package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.dtos.KeyIndicatorDTO;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

public class KeyIndicatorSerializer implements Serializer<KeyIndicatorDTO> {

    @Override
    public Object serialize(@NotNull KeyIndicatorDTO dto, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONArray arr = new JSONArray();
        if (dto.keys != null) {
            for (int key : dto.keys) {
                arr.put(key);
            }
        }
        return arr;
    }
}
