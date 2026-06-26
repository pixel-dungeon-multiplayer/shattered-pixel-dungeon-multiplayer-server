package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.FlareVisualAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class FlareVisualActionSerializer extends NetworkActionSerializer<FlareVisualAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull FlareVisualAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = new JSONObject();
        if (obj.positionX != null && obj.positionY != null) {
            actionObj.put("position_x", obj.positionX);
            actionObj.put("position_y", obj.positionY);
        } else {
            actionObj.put("pos", obj.pos);
        }
        actionObj.put("color", obj.color);
        actionObj.put("duration", obj.duration);
        actionObj.put("light_mode", obj.lightMode);
        actionObj.put("rays", obj.rays);
        actionObj.put("radius", obj.radius);
        actionObj.put("angle", obj.angle);
        actionObj.put("angular_speed", obj.angularSpeed);
        return actionObj;
    }
}
