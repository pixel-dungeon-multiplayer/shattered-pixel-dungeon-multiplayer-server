package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import com.shatteredpixel.shatteredpixeldungeon.effects.Lightning;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.LightningVisualAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

public class LightningVisualActionSerializer extends NetworkActionSerializer<LightningVisualAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull LightningVisualAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = new JSONObject();
        JSONArray arcs = new JSONArray();
        for (Lightning.Arc arc : obj.arcs) {
            JSONObject arcObj = new JSONObject();
            arcObj.put("from", ctx.serialize(arc.fromAnchor(), profile));
            arcObj.put("to", ctx.serialize(arc.toAnchor(), profile));
            arcs.put(arcObj);
        }
        actionObj.put("arcs", arcs);
        actionObj.put("duration", obj.duration);
        return actionObj;
    }
}
