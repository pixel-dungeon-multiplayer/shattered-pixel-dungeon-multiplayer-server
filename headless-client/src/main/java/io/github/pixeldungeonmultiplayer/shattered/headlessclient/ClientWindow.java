package io.github.pixeldungeonmultiplayer.shattered.headlessclient;

import org.json.JSONObject;

public final class ClientWindow {
    public final int id;
    public final String type;
    public final JSONObject args;
    public final JSONObject raw;

    private ClientWindow(JSONObject json) {
        raw = json;
        id = json.getInt("id");
        type = json.getString("type");
        args = json.optJSONObject("args");
    }

    public static ClientWindow fromJson(JSONObject json) {
        return new ClientWindow(json);
    }

    public boolean isDialog() {
        return "dialog".equals(type);
    }
}
