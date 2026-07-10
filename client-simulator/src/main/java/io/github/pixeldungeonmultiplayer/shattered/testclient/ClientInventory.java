package io.github.pixeldungeonmultiplayer.shattered.testclient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ClientInventory {
    public final ClientBag backpack;
    public final List<SpecialSlot> specialSlots;
    private final Map<List<Integer>, ClientItem> itemsByPath;

    private ClientInventory(ClientBag backpack, List<SpecialSlot> specialSlots) {
        this.backpack = backpack;
        this.specialSlots = Collections.unmodifiableList(new ArrayList<>(specialSlots));
        this.itemsByPath = new LinkedHashMap<>();
        indexBag(Collections.emptyList(), backpack);
        for (SpecialSlot slot : specialSlots) {
            if (slot.item != null) {
                itemsByPath.put(Collections.singletonList(slot.id), slot.item);
            }
        }
    }

    public static ClientInventory fromJson(JSONObject json) {
        ClientBag backpack = ClientBag.fromJson(json.getJSONObject("backpack"));
        ArrayList<SpecialSlot> slots = new ArrayList<>();
        JSONArray specialSlots = json.optJSONArray("special_slots");
        if (specialSlots != null) {
            for (int i = 0; i < specialSlots.length(); i++) {
                JSONObject slot = specialSlots.getJSONObject(i);
                ClientItem item = slot.isNull("item") ? null : ClientItem.fromJson(slot.getJSONObject("item"));
                slots.add(new SpecialSlot(slot.getInt("id"), item));
            }
        }
        return new ClientInventory(backpack, slots);
    }

    public ClientItem itemAt(List<Integer> path) {
        return itemsByPath.get(pathKey(path));
    }

    public Map<List<Integer>, ClientItem> itemsByPath() {
        return Collections.unmodifiableMap(itemsByPath);
    }

    public void putItem(List<Integer> path, ClientItem item) {
        itemsByPath.put(pathKey(path), item);
    }

    public void removeItem(List<Integer> path) {
        itemsByPath.remove(pathKey(path));
    }

    private void indexBag(List<Integer> prefix, ClientBag bag) {
        for (int i = 0; i < bag.items.size(); i++) {
            ClientItem item = bag.items.get(i);
            ArrayList<Integer> path = new ArrayList<>(prefix);
            path.add(i);
            itemsByPath.put(Collections.unmodifiableList(path), item);
            if (item instanceof ClientBag) {
                indexBag(path, (ClientBag) item);
            }
        }
    }

    private static List<Integer> pathKey(List<Integer> path) {
        return Collections.unmodifiableList(new ArrayList<>(path));
    }

    public static final class SpecialSlot {
        public final int id;
        public final ClientItem item;

        private SpecialSlot(int id, ClientItem item) {
            this.id = id;
            this.item = item;
        }
    }
}
