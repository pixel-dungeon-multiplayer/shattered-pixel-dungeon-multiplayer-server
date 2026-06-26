package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers;

import com.watabou.utils.Rect;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class RectSerializer implements Serializer<Rect> {

    @Override
    public Object serialize(@NotNull Rect rect, @NotNull SerializationContext ctx, @NotNull String profile) {
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
