package io.github.pixeldungeonmultiplayer.shattered.headlessclient;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ClientItem {
    public final @NotNull JSONObject raw;
    public final int spriteSheet;
    public final int image;
    public final int icon;
    public final Object name;
    public final boolean stackable;
    public final int quantity;
    public final boolean known;
    public final boolean cursed;
    public final boolean identified;
    public final boolean levelKnown;
    public final int level;
    public final int energyValue;
    public final List<String> actions;
    public final JSONObject actionNames;
    public final @Nullable String defaultAction;
    public final Object info;
    public final Object ui;
    public final Object glowing;
    public final Object emitter;

    protected ClientItem(@NotNull JSONObject json) {
        raw = new JSONObject(json.toString());
        spriteSheet = json.optInt("sprite_sheet", -1);
        image = json.optInt("image", -1);
        icon = json.optInt("icon", -1);
        name = json.opt("name");
        stackable = json.optBoolean("stackable", false);
        quantity = json.optInt("quantity", 1);
        known = json.optBoolean("known", false);
        cursed = json.optBoolean("cursed", false);
        identified = json.optBoolean("identified", false);
        levelKnown = json.optBoolean("level_known", false);
        level = json.optInt("level", 0);
        energyValue = json.optInt("energy_value", 0);
        actions = strings(json.optJSONArray("actions"));
        actionNames = json.optJSONObject("action_names");
        defaultAction = json.optString("default_action", null);
        info = json.opt("info");
        ui = json.opt("ui");
        glowing = json.opt("glowing");
        emitter = json.opt("emitter");
    }

    public static @NotNull ClientItem fromJson(@NotNull JSONObject json) {
        if (json.has("items") || json.has("bag_icon") || json.has("size")) {
            return ClientBag.fromJson(json);
        }
        return new ClientItem(json);
    }

    public ClientItem update(@NotNull JSONObject patch) {
        return fromJson(merge(raw, patch));
    }

    private static @NotNull JSONObject merge(@NotNull JSONObject base, @NotNull JSONObject patch) {
        JSONObject merged = new JSONObject(base.toString());
        for (Iterator<String> it = patch.keys(); it.hasNext(); ) {
            String key = it.next();
            Object value = patch.get(key);
            Object previous = merged.opt(key);
            if (previous instanceof JSONObject && value instanceof JSONObject) {
                merged.put(key, merge((JSONObject) previous, (JSONObject) value));
            } else {
                merged.put(key, value);
            }
        }
        return merged;
    }

    private static List<String> strings(JSONArray array) {
        if (array == null) {
            return Collections.emptyList();
        }
        ArrayList<String> values = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            values.add(array.optString(i));
        }
        return Collections.unmodifiableList(values);
    }
}
