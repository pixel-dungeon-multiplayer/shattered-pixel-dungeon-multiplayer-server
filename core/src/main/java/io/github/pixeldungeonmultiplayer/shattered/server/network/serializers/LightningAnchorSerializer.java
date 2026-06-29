package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.dtos.LightningAnchor;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class LightningAnchorSerializer implements Serializer<LightningAnchor> {

    @Override
    public Object serialize(@NotNull LightningAnchor anchor, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject object = new JSONObject();
        object.put("type", anchor.type());
        if (anchor.cell() != null) {
            object.put("cell", anchor.cell());
        }
        if (anchor.targetCharId() != null) {
            object.put("target_char", anchor.targetCharId());
        }
        if (LightningAnchor.TYPE_TARGET_POINT.equals(anchor.type())) {
            object.put("x_factor", anchor.xFactor());
            object.put("y_factor", anchor.yFactor());
            if (anchor.shiftX() != 0f || anchor.shiftY() != 0f) {
                object.put("shift_x", anchor.shiftX());
                object.put("shift_y", anchor.shiftY());
            }
        }
        return object;
    }
}
