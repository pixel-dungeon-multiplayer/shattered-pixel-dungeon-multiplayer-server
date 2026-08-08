package io.github.pixeldungeonmultiplayer.shattered.server.network.jsondiff;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonComparatorTest {

    @Test
    void comparesObjectsWithoutDependingOnKeyOrder() {
        JSONObject first = new JSONObject()
                .put("name", "server")
                .put("settings", new JSONObject().put("public", true));
        JSONObject second = new JSONObject()
                .put("settings", new JSONObject().put("public", true))
                .put("name", "server");

        assertTrue(JsonComparator.similar(first, second));
    }

    @Test
    void detectsNestedObjectChanges() {
        JSONObject first = new JSONObject().put("settings", new JSONObject().put("public", true));
        JSONObject second = new JSONObject().put("settings", new JSONObject().put("public", false));

        assertFalse(JsonComparator.similar(first, second));
    }

    @Test
    void comparesArraysInOrder() {
        JSONArray first = new JSONArray().put(1).put(2);
        JSONArray same = new JSONArray().put(1).put(2);
        JSONArray reordered = new JSONArray().put(2).put(1);

        assertTrue(JsonComparator.similar(first, same));
        assertFalse(JsonComparator.similar(first, reordered));
    }

    @Test
    void distinguishesDifferentNumericTypes() {
        assertFalse(JsonComparator.similar(1, 1L));
        assertFalse(JsonComparator.similar(1, 1.0));
    }

    @Test
    void handlesJavaNullAndJsonNull() {
        assertTrue(JsonComparator.similar(null, null));
        assertTrue(JsonComparator.similar(JSONObject.NULL, JSONObject.NULL));
        assertFalse(JsonComparator.similar(null, JSONObject.NULL));
        assertFalse(JsonComparator.similar(JSONObject.NULL, "null"));
    }
}
