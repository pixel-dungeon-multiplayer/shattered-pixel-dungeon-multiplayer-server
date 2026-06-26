package io.github.pixeldungeonmultiplayer.shattered.server.network.jsondiff;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

public final class JsonComparator {

    public static boolean similar(Object a, Object b) {
        if (a == b) return true;
        if (a == null || b == null) return false;

        if (a == JSONObject.NULL || b == JSONObject.NULL) {
            return false;
        }

        if (a instanceof JSONObject && b instanceof JSONObject) {
            return similarObjects((JSONObject) a, (JSONObject) b);
        }

        if (a instanceof JSONArray && b instanceof JSONArray) {
            return similarArrays((JSONArray) a, (JSONArray) b);
        }

        return a.equals(b);
    }

    private static boolean similarObjects(JSONObject a, JSONObject b) {
        if (a.length() != b.length()) return false;

        Iterator<String> keys = a.keys();
        while (keys.hasNext()) {
            String key = keys.next();

            if (!b.has(key)) return false;
            if (!similar(a.opt(key), b.opt(key))) return false;
        }

        return true;
    }

    private static boolean similarArrays(JSONArray a, JSONArray b) {
        if (a.length() != b.length()) return false;

        for (int i = 0; i < a.length(); i++) {
            if (!similar(a.opt(i), b.opt(i))) return false;
        }

        return true;
    }
}