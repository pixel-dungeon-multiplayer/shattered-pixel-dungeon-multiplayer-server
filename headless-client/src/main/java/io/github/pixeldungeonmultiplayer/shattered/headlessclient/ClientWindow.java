package io.github.pixeldungeonmultiplayer.shattered.headlessclient;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public final class ClientWindow {
    public final int id;
    public final @NotNull String type;
    public final @Nullable JSONObject args;
    public final @NotNull JSONObject raw;

    @Contract(pure = true)
    private ClientWindow(@NotNull JSONObject json) {
        raw = json;
        id = json.getInt("id");
        type = json.getString("type");
        args = json.optJSONObject("args");
    }

    @Contract(value = "_->new", pure = true)
    public static @NotNull ClientWindow fromJson(@NotNull JSONObject json) {
        return new ClientWindow(json);
    }

    @Contract(pure = true)
    public boolean isDialog() {
        return "dialog".equals(type);
    }
}
