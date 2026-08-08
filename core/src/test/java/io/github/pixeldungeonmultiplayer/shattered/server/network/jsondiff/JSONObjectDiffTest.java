package io.github.pixeldungeonmultiplayer.shattered.server.network.jsondiff;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JSONObjectDiffTest {

    @Test
    void returnsNullForEquivalentObjects() {
        JSONObject oldJson = new JSONObject()
                .put("value", 1)
                .put("nested", new JSONObject().put("enabled", true));
        JSONObject newJson = new JSONObject()
                .put("nested", new JSONObject().put("enabled", true))
                .put("value", 1);

        assertNull(JSONObjectDiff.diff(oldJson, newJson));
    }

    @Test
    void reportsAddedRemovedAndChangedProperties() {
        JSONObject oldJson = new JSONObject()
                .put("removed", "old")
                .put("changed", 1)
                .put("unchanged", true);
        JSONObject newJson = new JSONObject()
                .put("added", "new")
                .put("changed", 2)
                .put("unchanged", true);

        JSONObject patch = JSONObjectDiff.diff(oldJson, newJson);

        assertEquals(3, patch.length());
        assertTrue(patch.isNull("removed"));
        assertEquals(2, patch.getInt("changed"));
        assertEquals("new", patch.getString("added"));
        assertFalse(patch.has("unchanged"));
    }

    @Test
    void createsMinimalPatchForNestedObjects() {
        JSONObject oldJson = new JSONObject().put("settings", new JSONObject()
                .put("name", "old")
                .put("port", 8080));
        JSONObject newJson = new JSONObject().put("settings", new JSONObject()
                .put("name", "new")
                .put("port", 8080));

        JSONObject patch = JSONObjectDiff.diff(oldJson, newJson);

        JSONObject settingsPatch = patch.getJSONObject("settings");
        assertEquals(1, settingsPatch.length());
        assertEquals("new", settingsPatch.getString("name"));
    }

    @Test
    void replacesOrdinaryArrayWhenOrderChanges() {
        JSONObject oldJson = new JSONObject().put("values", new JSONArray().put(1).put(2));
        JSONObject newJson = new JSONObject().put("values", new JSONArray().put(2).put(1));

        JSONObject patch = JSONObjectDiff.diff(oldJson, newJson);

        assertTrue(JsonComparator.similar(newJson.getJSONArray("values"), patch.getJSONArray("values")));
    }

    @Test
    void replacesMixedArrayWhenNotEveryItemHasAnId() {
        JSONObject oldJson = new JSONObject().put("items", new JSONArray()
                .put(new JSONObject().put("id", "identified").put("value", 1))
                .put(new JSONObject().put("value", 2)));
        JSONObject newJson = new JSONObject().put("items", new JSONArray()
                .put(new JSONObject().put("id", "identified").put("value", 3))
                .put(new JSONObject().put("value", 2)));

        JSONObject patch = JSONObjectDiff.diff(oldJson, newJson);

        assertTrue(JsonComparator.similar(newJson.getJSONArray("items"), patch.getJSONArray("items")));
    }

    @Test
    void diffsIdentifiedArraysByItemId() {
        JSONObject oldJson = new JSONObject().put("players", new JSONArray()
                .put(new JSONObject().put("id", "removed").put("level", 1))
                .put(new JSONObject().put("id", "changed").put("level", 2))
                .put(new JSONObject().put("id", "same").put("level", 3)));
        JSONObject newJson = new JSONObject().put("players", new JSONArray()
                .put(new JSONObject().put("id", "changed").put("level", 5))
                .put(new JSONObject().put("id", "same").put("level", 3))
                .put(new JSONObject().put("id", "added").put("level", 4)));

        JSONObject arrayPatch = JSONObjectDiff.diff(oldJson, newJson).getJSONObject("players");
        JSONObject updates = arrayPatch.getJSONObject("$updates");

        assertEquals(2, updates.length());
        assertEquals(5, updates.getJSONObject("changed").getInt("level"));
        assertFalse(updates.getJSONObject("changed").has("id"));
        assertEquals("added", updates.getJSONObject("added").getString("id"));
        assertEquals("removed", arrayPatch.getJSONArray("$removals").getString(0));
    }

    @Test
    void ignoresReorderingOfIdentifiedArrayItems() {
        JSONObject first = new JSONObject().put("id", "first").put("value", 1);
        JSONObject second = new JSONObject().put("id", "second").put("value", 2);
        JSONObject oldJson = new JSONObject().put("items", new JSONArray().put(first).put(second));
        JSONObject newJson = new JSONObject().put("items", new JSONArray().put(second).put(first));

        assertNull(JSONObjectDiff.diff(oldJson, newJson));
    }

    @Test
    void handlesTransitionsToAndFromEmptyArrays() {
        JSONObject added = JSONObjectDiff.diff(
                new JSONObject().put("items", new JSONArray()),
                new JSONObject().put("items", new JSONArray().put("value"))
        );
        JSONObject removed = JSONObjectDiff.diff(
                new JSONObject().put("items", new JSONArray().put("value")),
                new JSONObject().put("items", new JSONArray())
        );

        assertEquals("value", added.getJSONArray("items").getString(0));
        assertEquals(0, removed.getJSONArray("items").length());
    }
}
