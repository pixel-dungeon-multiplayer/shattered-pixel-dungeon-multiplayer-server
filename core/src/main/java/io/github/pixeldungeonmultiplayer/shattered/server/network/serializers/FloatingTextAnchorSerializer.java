package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.dtos.FloatingTextAnchor;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class FloatingTextAnchorSerializer implements Serializer<FloatingTextAnchor> {

    @Override
    public Object serialize(@NotNull FloatingTextAnchor anchor, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject object = new JSONObject();
        object.put("type", anchor.type());
        if (anchor.cell() != null) {
            object.put("cell", anchor.cell());
        }
        if (anchor.targetCharId() != null) {
            object.put("target_char", anchor.targetCharId());
        }
        return object;
    }
}
