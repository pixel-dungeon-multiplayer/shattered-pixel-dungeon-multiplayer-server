package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers;

import com.watabou.noosa.Image;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class ImageSerializer implements Serializer<Image> {

    @Override
    public Object serialize(@NotNull Image obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        if (obj.texture == null || obj.texture.source == null) {
            throw new IllegalArgumentException("Cannot serialize Image without a valid texture source (generic/dynamic textures without source not supported)");
        }

        JSONObject json = new JSONObject();
        json.put("source", ctx.serialize(obj.texture.source, profile));
        if (obj.frame() != null) {
            json.put("frame", ctx.serialize(obj.frame(), profile));
        }
        return json;
    }
}
