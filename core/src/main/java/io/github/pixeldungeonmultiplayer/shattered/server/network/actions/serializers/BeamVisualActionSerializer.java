package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import com.watabou.noosa.Image;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.BeamVisualAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class BeamVisualActionSerializer extends NetworkActionSerializer<BeamVisualAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull BeamVisualAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = new JSONObject();
        actionObj.put("image", ctx.serializeAs(obj.image, Image.class, profile));
        actionObj.put("from", ctx.serialize(obj.from, profile));
        actionObj.put("to", ctx.serialize(obj.to, profile));
        actionObj.put("duration", obj.duration);

        // Optimization: do not send default color modifiers for untinted images.
        if (obj.image.rm != 1f || obj.image.gm != 1f || obj.image.bm != 1f || obj.image.am != 1f
                || obj.image.ra != 0f || obj.image.ga != 0f || obj.image.ba != 0f || obj.image.aa != 0f) {
            JSONObject color = new JSONObject();
            color.put("rm", obj.image.rm);
            color.put("gm", obj.image.gm);
            color.put("bm", obj.image.bm);
            color.put("am", obj.image.am);
            color.put("ra", obj.image.ra);
            color.put("ga", obj.image.ga);
            color.put("ba", obj.image.ba);
            color.put("aa", obj.image.aa);
            actionObj.put("color", color);
        }

        return actionObj;
    }
}
