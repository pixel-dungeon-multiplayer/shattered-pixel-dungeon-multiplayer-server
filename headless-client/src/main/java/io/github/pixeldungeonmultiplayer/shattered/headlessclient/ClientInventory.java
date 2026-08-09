package io.github.pixeldungeonmultiplayer.shattered.headlessclient;

import org.checkerframework.dataflow.qual.SideEffectFree;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public final class ClientInventory {
    public final @NotNull ClientBag backpack;
    public final @UnmodifiableView @NotNull List<@NotNull SpecialSlot> specialSlots;

    private ClientInventory(@NotNull ClientBag backpack, @NotNull List<@NotNull SpecialSlot> specialSlots) {
        this.backpack = Objects.requireNonNull(backpack);
        this.specialSlots = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(specialSlots)));
    }

    @Contract("_ -> new")
    public static @NotNull ClientInventory fromJson(@NotNull JSONObject json) {
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
        return itemAtPath(path);
    }

    public @NotNull Map<List<Integer>, @NotNull ClientItem> itemsByPath() {
        Map<List<Integer>, ClientItem> itemsByPath = new LinkedHashMap<>();
        indexBag(itemsByPath, Collections.emptyList(), backpack);
        for (SpecialSlot slot : specialSlots) {
            if (slot.item != null) {
                List<Integer> path = Collections.singletonList(slot.path());
                itemsByPath.put(path, slot.item);
                if (slot.item instanceof ClientBag) {
                    indexBag(itemsByPath, path, (ClientBag) slot.item);
                }
            }
        }
        return Collections.unmodifiableMap(itemsByPath);
    }

    public void addItem(@NotNull List<@NotNull Integer> path, ClientItem item) {
        List<ClientItem> items = parentBag(path).items;
        int index = lastIndex(path);
        if (index < 0 || index > items.size()) {
            throw new IndexOutOfBoundsException("Cannot add item at path " + path);
        }
        items.add(index, item);
    }

    public void updateItem(@NotNull List<@NotNull Integer> path, JSONObject patch) {
        replaceExisting(path, requireExisting(path).update(patch));
    }

    public void replaceItem(List<Integer> path, ClientItem item) {
        replaceExisting(path, item);
    }

    public void removeItem(List<Integer> path) {
        if (isSpecialSlotRoot(path)) {
            specialSlot(path).item = null;
        } else {
            parentBag(path).items.remove(lastIndex(path));
        }
    }

    private ClientItem itemAtPath(List<Integer> path) {
        if (path.isEmpty()) {
            return backpack;
        }
        if (path.get(0) < 0) {
            ClientItem item = specialSlot(path).item;
            if (path.size() == 1) {
                return item;
            }
            if (!(item instanceof ClientBag)) {
                throw new IllegalArgumentException("Special slot item is not a bag: " + path.get(0));
            }
            return itemInBag((ClientBag) item, path, 1);
        }
        return itemInBag(backpack, path, 0);
    }

    private @NotNull ClientItem requireExisting(@NotNull List<@NotNull Integer> path) {
        ClientItem item = itemAtPath(path);
        if (item == null) {
            throw new IllegalArgumentException("Cannot update missing item at path " + path);
        }
        return item;
    }

    private ClientItem itemInBag(@NotNull ClientBag bag, @NotNull List<@NotNull Integer> path, int offset) {
        List<ClientItem> items = bag.items;
        ClientItem item = null;
        for (int i = offset; i < path.size(); i++) {
            item = items.get(path.get(i));
            if (i < path.size() - 1) {
                if (!(item instanceof ClientBag)) {
                    throw new IllegalArgumentException("Path segment is not a bag: " + path.subList(0, i + 1));
                }
                items = ((ClientBag) item).items;
            }
        }
        return item;
    }

    private @NotNull ClientBag parentBag(@NotNull List<@NotNull Integer> path) {
        if (path.isEmpty()) {
            throw new IllegalArgumentException("Item path must not be empty");
        }
        if (isSpecialSlotRoot(path)) {
            throw new IllegalArgumentException("Special slot root has no list parent: " + path);
        }
        if (path.size() == 1) {
            return backpack;
        }
        ClientItem parent = itemAtPath(path.subList(0, path.size() - 1));
        if (!(parent instanceof ClientBag)) {
            throw new IllegalArgumentException("Item parent is not a bag: " + path);
        }
        return (ClientBag) parent;
    }

    private void replaceExisting(@NotNull List<Integer> path, @NotNull ClientItem item) {
        if (isSpecialSlotRoot(path)) {
            specialSlot(path).item = item;
        } else {
            parentBag(path).items.set(lastIndex(path), item);
        }
    }

    private boolean isSpecialSlotRoot(@NotNull List<@NotNull Integer> path) {
        return path.size() == 1 && path.get(0) < 0;
    }

    private @NotNull SpecialSlot specialSlot(@NotNull List<Integer> path) {
        if (path.isEmpty() || path.get(0) >= 0) {
            throw new IllegalArgumentException("Path is not a special slot path: " + path);
        }
        int slotIndex = -path.get(0) - 1;
        if (slotIndex < 0 || slotIndex >= specialSlots.size()) {
            throw new IndexOutOfBoundsException("Unknown special slot path: " + path);
        }
        return specialSlots.get(slotIndex);
    }

    private int lastIndex(@NotNull List<Integer> path) {
        if (path.isEmpty()) {
            throw new IllegalArgumentException("Item path must not be empty");
        }
        return path.get(path.size() - 1);
    }

    private void indexBag(@NotNull Map<List<Integer>, ClientItem> itemsByPath, @NotNull List<Integer> prefix, @NotNull ClientBag bag) {
        for (int i = 0; i < bag.items.size(); i++) {
            ClientItem item = bag.items.get(i);
            ArrayList<Integer> path = new ArrayList<>(prefix);
            path.add(i);
            itemsByPath.put(Collections.unmodifiableList(path), item);
            if (item instanceof ClientBag) {
                indexBag(itemsByPath, path, (ClientBag) item);
            }
        }
    }

    public static final class SpecialSlot {
        public final int id;
        public ClientItem item;

        @Contract(pure = true)
        private SpecialSlot(int id, ClientItem item) {
            this.id = id;
            this.item = item;
        }

        @Contract(pure = true)
        @SideEffectFree
        public int path() {
            return -id - 1;
        }
    }
}
