package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.BlobUpdateAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.LiveStateNetworkAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BlobUpdateActionSerializer extends NetworkActionSerializer<BlobUpdateAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull BlobUpdateAction action, @NotNull SerializationContext ctx, @NotNull String profile) {
        final Blob blob = action.blob;

        JSONObject object = new JSONObject();
        if (blob.cur == null) {
            return null;
        }

        try {
            object.put("id", blob.id());
            object.put("tile_desc", blob.tileDesc() == null? JSONObject.NULL: blob.tileDesc().toJsonObject());
            object.put("always_visible", blob.alwaysVisible);
            LiveStateNetworkAction emitter = blob.emitter != null? blob.emitter.networkStartAction() : null;
            object.put("emitter", emitter == null? JSONObject.NULL: ctx.serialize(emitter, profile));

            JSONArray positions = new JSONArray();
            for (int i = 0; i < blob.cur.length; i++) {
                if (blob.cur[i] > 0) {
                    positions.put(i);
                }
            }
            object.put("positions", positions);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return object;
    }
}
