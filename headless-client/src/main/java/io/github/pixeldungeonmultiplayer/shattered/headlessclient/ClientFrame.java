package io.github.pixeldungeonmultiplayer.shattered.headlessclient;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public final class ClientFrame {
    public final float left;
    public final float top;
    public final float right;
    public final float bottom;
    public final float x;
    public final float y;
    public final float width;
    public final float height;

    private ClientFrame(@NotNull JSONObject json) {
        left = (float) json.optDouble("left", 0);
        top = (float) json.optDouble("top", 0);
        right = (float) json.optDouble("right", 0);
        bottom = (float) json.optDouble("bottom", 0);
        x = (float) json.optDouble("x", left);
        y = (float) json.optDouble("y", top);
        width = (float) json.optDouble("width", right - left);
        height = (float) json.optDouble("height", bottom - top);
    }

    @Contract("_ -> new")
    public static @NotNull ClientFrame fromJson(@NotNull JSONObject json) {
        return new ClientFrame(json);
    }
}
