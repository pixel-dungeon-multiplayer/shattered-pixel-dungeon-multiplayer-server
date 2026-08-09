package io.github.pixeldungeonmultiplayer.shattered.headlessclient;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class ClientBag extends ClientItem {
    public final int bagIcon;
    public final int size;
    public final @Nullable Integer owner;
    public final @NotNull List<@NotNull ClientItem> items;

    private ClientBag(@NotNull JSONObject json) {
        super(json);
        bagIcon = json.optInt("bag_icon", -1);
        size = json.optInt("size", 0);
        owner = json.isNull("owner") ? null : json.optInt("owner");
        items = parseItems(json.optJSONArray("items"));
    }

    @Contract(value = "_->new", pure = true)
    public static @NotNull ClientBag fromJson(@NotNull JSONObject json) {
        return new ClientBag(json);
    }

    @Contract(value = "_->new", pure = true)
    private static @NotNull List<@NotNull ClientItem> parseItems(@Nullable JSONArray array) {
        if (array == null) {
            return new ArrayList<>();
        }
        ArrayList<@NotNull ClientItem> result = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            if (!array.isNull(i)) {
                result.add(ClientItem.fromJson(array.getJSONObject(i)));
            }
        }
        return result;
    }
}
