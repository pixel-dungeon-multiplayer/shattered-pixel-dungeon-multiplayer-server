package io.github.pixeldungeonmultiplayer.shattered.server.utils;


import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static JSONArray putToJSONArray(Object[] array) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < array.length; i++) {
            jsonArray.put(i, array[i]);
        }
        return jsonArray;
    }
    public static JSONArray putToJSONArray(int[] array) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < array.length; i++) {
            jsonArray.put(i, array[i]);
        }
        return jsonArray;
    }

    public static String truncate(String text, int maxLength, String ellipsis) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        int targetLength = Math.max(0, maxLength - (ellipsis != null ? ellipsis.length() : 0));
        return text.substring(0, targetLength) + (ellipsis != null ? ellipsis : "");
    }

    public static List<Integer> JsonArrayToListInteger(JSONArray arr) {
        List<Integer> res = new ArrayList<Integer>(2);
        try {
            for (int i = 0; i < arr.length(); i++) {
                res.add(arr.getInt(i));
            }
        } catch (Exception e) {
            GLog.n(e.getMessage());
            return null;
        }
        return res;
    }
}
