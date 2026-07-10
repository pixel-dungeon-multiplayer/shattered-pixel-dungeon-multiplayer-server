package io.github.pixeldungeonmultiplayer.shattered.testclient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class ClientBag extends ClientItem {
    public final int bagIcon;
    public final int size;
    public final Integer owner;
    public final List<ClientItem> items;

    private ClientBag(JSONObject json) {
        super(json);
        bagIcon = json.optInt("bag_icon", -1);
        size = json.optInt("size", 0);
        owner = json.isNull("owner") ? null : json.optInt("owner");
        items = parseItems(json.optJSONArray("items"));
    }

    public static ClientBag fromJson(JSONObject json) {
        return new ClientBag(json);
    }

    private static List<ClientItem> parseItems(JSONArray array) {
        if (array == null) {
            return new ArrayList<>();
        }
        ArrayList<ClientItem> result = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            if (!array.isNull(i)) {
                result.add(ClientItem.fromJson(array.getJSONObject(i)));
            }
        }
        return result;
    }
}
