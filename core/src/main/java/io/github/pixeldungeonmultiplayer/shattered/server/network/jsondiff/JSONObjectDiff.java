package io.github.pixeldungeonmultiplayer.shattered.server.network.jsondiff;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Iterator;

public class JSONObjectDiff {

    public static @Nullable JSONObject diff(@NotNull JSONObject oldJson, @NotNull JSONObject newJson) {
        JSONObject patch = new JSONObject();
        
        // Find deleted and modified keys from oldJson
        Iterator<String> keys = oldJson.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (!newJson.has(key)) {
                // Key removed
                patch.put(key, JSONObject.NULL);
            } else {
                Object oldVal = oldJson.get(key);
                Object newVal = newJson.get(key);
                Object diffVal = diffValues(oldVal, newVal);
                if (diffVal != null) {
                    patch.put(key, diffVal);
                }
            }
        }
        
        // Find new keys not present in oldJson
        keys = newJson.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (!oldJson.has(key)) {
                patch.put(key, newJson.get(key));
            }
        }
        
        return patch.length() > 0 ? patch : null;
    }

    private static Object diffValues(Object oldVal, Object newVal) {
        if (oldVal == null || newVal == null) {
            if (oldVal == newVal) return null;
            return newVal == null ? JSONObject.NULL : newVal;
        }
        if (JsonComparator.similar(oldVal, newVal)) {
            return null;
        }
        
        if (oldVal instanceof JSONObject && newVal instanceof JSONObject) {
            return diff((JSONObject) oldVal, (JSONObject) newVal);
        }
        
        if (oldVal instanceof JSONArray && newVal instanceof JSONArray) {
            return diffArrays((JSONArray) oldVal, (JSONArray) newVal);
        }
        
        return newVal;
    }

    private static Object diffArrays(JSONArray oldArr, JSONArray newArr) {
        // Check if items have IDs for matching
        boolean hasIds = true;
        for (int i = 0; i < oldArr.length(); i++) {
            Object item = oldArr.opt(i);
            if (item instanceof JSONObject) {
                if (!((JSONObject) item).has("id")) {
                    hasIds = false;
                    break;
                }
            } else {
                hasIds = false;
                break;
            }
        }
        if (hasIds) {
            for (int i = 0; i < newArr.length(); i++) {
                Object item = newArr.opt(i);
                if (item instanceof JSONObject) {
                    if (!((JSONObject) item).has("id")) {
                        hasIds = false;
                        break;
                    }
                } else {
                    hasIds = false;
                    break;
                }
            }
        }

        // If items have IDs, build an updates/removals diff
        if (hasIds && (oldArr.length() > 0 || newArr.length() > 0)) {
            JSONObject arrayPatch = new JSONObject();
            JSONObject updates = new JSONObject();
            JSONArray removals = new JSONArray();

            java.util.Map<String, JSONObject> oldMap = new java.util.HashMap<>();
            for (int i = 0; i < oldArr.length(); i++) {
                JSONObject item = oldArr.getJSONObject(i);
                oldMap.put(item.getString("id"), item);
            }

            java.util.Set<String> newIds = new java.util.HashSet<>();
            for (int i = 0; i < newArr.length(); i++) {
                JSONObject newItem = newArr.getJSONObject(i);
                String id = newItem.getString("id");
                newIds.add(id);

                if (!oldMap.containsKey(id)) {
                    updates.put(id, newItem);
                } else {
                    JSONObject oldItem = oldMap.get(id);
                    JSONObject itemDiff = diff(oldItem, newItem);
                    if (itemDiff != null) {
                        updates.put(id, itemDiff);
                    }
                }
            }

            for (String id : oldMap.keySet()) {
                if (!newIds.contains(id)) {
                    removals.put(id);
                }
            }

            if (updates.length() > 0 || removals.length() > 0) {
                arrayPatch.put("$updates", updates);
                arrayPatch.put("$removals", removals);
                return arrayPatch;
            }
            return null;
        }

        if (!JsonComparator.similar(oldArr, newArr)) {
            return newArr;
        }
        return null;
    }
}
