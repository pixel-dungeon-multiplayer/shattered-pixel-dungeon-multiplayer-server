package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers;

import com.watabou.utils.RectF;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class RectFSerializer implements Serializer<RectF> {

    @Override
    public Object serialize(@NotNull RectF rect, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject json = new JSONObject();
        json.put("left", rect.left);
        json.put("top", rect.top);
        json.put("right", rect.right);
        json.put("bottom", rect.bottom);
        json.put("x", rect.left);
        json.put("y", rect.top);
        json.put("width", rect.width());
        json.put("height", rect.height());
        return json;
    }
}
