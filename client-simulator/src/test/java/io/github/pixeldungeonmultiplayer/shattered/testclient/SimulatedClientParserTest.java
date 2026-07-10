package io.github.pixeldungeonmultiplayer.shattered.testclient;

import io.github.pixeldungeonmultiplayer.shattered.server.network.Protocol;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SimulatedClientParserTest {

    private static final String SERVER_UUID = "test-server-id";

    @Test
    void parsesInventoryAndWindowActionsIntoClientState() throws Exception {
        InMemorySocketPair sockets = InMemorySocketPair.create();
        SimulatedClient client = new SimulatedClient().connect(sockets.client());
        client.parse(handshakePacket());

        JSONObject packet = new JSONObject();
        packet.put(Protocol.FIELD_PACKET_TYPE, Protocol.PACKET_ACTIONS_BATCH);
        packet.put("actions", new JSONArray()
                .put(inventoryRebuildAction())
                .put(itemUpdateAction())
                .put(updateWindowAction()));

        client.parse(packet);

        assertEquals(2, client.inventory().backpack.items.size());
        assertEquals("Potion", client.inventory().itemAt(Arrays.asList(1)).name);
        assertEquals(10, client.inventory().itemAt(Arrays.asList(1)).quantity);
        assertEquals("dialog", client.windows().get(42).type);
        assertEquals("hello", client.windows().get(42).args.getString("message"));
    }

    @Test
    void appliesListInventoryItemActions() throws Exception {
        InMemorySocketPair sockets = InMemorySocketPair.create();
        SimulatedClient client = new SimulatedClient().connect(sockets.client());
        client.parse(handshakePacket());

        JSONObject packet = new JSONObject();
        packet.put(Protocol.FIELD_PACKET_TYPE, Protocol.PACKET_ACTIONS_BATCH);
        packet.put("actions", new JSONArray()
                .put(inventoryRebuildAction())
                .put(itemAddAction(1, item("Scroll", 1)))
                .put(itemAddAction(3, item("Bomb", 2)))
                .put(itemUpdatePatchAction(2, new JSONObject().put("quantity", 10)))
                .put(itemReplaceAction(0, item("Axe", 1)))
                .put(itemRemoveAction(1)));

        client.parse(packet);

        assertEquals(3, client.inventory().backpack.items.size());
        assertEquals("Axe", client.inventory().itemAt(Arrays.asList(0)).name);
        assertEquals("Potion", client.inventory().itemAt(Arrays.asList(1)).name);
        assertEquals(10, client.inventory().itemAt(Arrays.asList(1)).quantity);
        assertEquals("Bomb", client.inventory().itemAt(Arrays.asList(2)).name);
        assertEquals(2, client.inventory().itemAt(Arrays.asList(2)).quantity);
    }

    @Test
    void addressesSpecialSlotsAndNestedSpecialSlotBags() throws Exception {
        InMemorySocketPair sockets = InMemorySocketPair.create();
        SimulatedClient client = new SimulatedClient().connect(sockets.client());
        client.parse(handshakePacket());

        JSONObject packet = new JSONObject();
        packet.put(Protocol.FIELD_PACKET_TYPE, Protocol.PACKET_ACTIONS_BATCH);
        packet.put("actions", new JSONArray()
                .put(inventoryRebuildWithSpecialSlotBagAction())
                .put(itemUpdatePatchAction(new JSONArray().put(-1), new JSONObject().put("quantity", 2)))
                .put(itemAddAction(new JSONArray().put(-1).put(1), item("Ring", 1)))
                .put(itemRemoveAction(new JSONArray().put(-1).put(0))));

        client.parse(packet);

        assertEquals("Weapon Bag", client.inventory().itemAt(Arrays.asList(-1)).name);
        assertEquals(2, client.inventory().itemAt(Arrays.asList(-1)).quantity);
        assertEquals("Ring", client.inventory().itemAt(Arrays.asList(-1, 0)).name);
        assertEquals("Sword", client.inventory().itemAt(Arrays.asList(0)).name);
    }

    @Test
    void runsBeforeAfterAndInsteadActionHooks() throws Exception {
        InMemorySocketPair sockets = InMemorySocketPair.create();
        SimulatedClient client = new SimulatedClient().connect(sockets.client());
        client.parse(handshakePacket());

        AtomicInteger before = new AtomicInteger();
        AtomicInteger after = new AtomicInteger();
        AtomicInteger instead = new AtomicInteger();

        client.beforeAction("update_window", (c, action) -> before.incrementAndGet());
        client.insteadAction("update_window", (c, action) -> instead.incrementAndGet());
        client.afterAction("update_window", (c, action) -> after.incrementAndGet());

        JSONObject packet = new JSONObject();
        packet.put(Protocol.FIELD_PACKET_TYPE, Protocol.PACKET_ACTIONS_BATCH);
        packet.put("actions", new JSONArray().put(updateWindowAction()));

        client.parse(packet);

        assertEquals(1, before.get());
        assertEquals(1, instead.get());
        assertEquals(1, after.get());
        assertNull(client.windows().get(42));
    }

    private static JSONObject handshakePacket() {
        JSONObject packet = new JSONObject();
        packet.put(Protocol.FIELD_PACKET_TYPE, Protocol.PACKET_HANDSHAKE);
        packet.put(Protocol.FIELD_PROTOCOL, Protocol.NAME);
        packet.put(Protocol.FIELD_VERSION, Protocol.VERSION);
        packet.put(Protocol.FIELD_SERVER_ID, SERVER_UUID);
        return packet;
    }

    private static JSONObject inventoryRebuildAction() {
        JSONObject backpack = item("Backpack", 1);
        backpack.put("bag_icon", 7);
        backpack.put("size", 20);
        backpack.put("owner", JSONObject.NULL);
        backpack.put("items", new JSONArray()
                .put(item("Sword", 1))
                .put(item("Potion", 3)));

        JSONObject action = new JSONObject();
        action.put("action_name", "inventory_rebuild");
        action.put("backpack", backpack);
        action.put("special_slots", new JSONArray());
        return action;
    }

    private static JSONObject inventoryRebuildWithSpecialSlotBagAction() {
        JSONObject backpack = item("Backpack", 1);
        backpack.put("bag_icon", 7);
        backpack.put("size", 20);
        backpack.put("owner", JSONObject.NULL);
        backpack.put("items", new JSONArray().put(item("Sword", 1)));

        JSONObject weaponBag = item("Weapon Bag", 1);
        weaponBag.put("bag_icon", 8);
        weaponBag.put("size", 5);
        weaponBag.put("owner", JSONObject.NULL);
        weaponBag.put("items", new JSONArray().put(item("Dagger", 1)));

        JSONObject action = new JSONObject();
        action.put("action_name", "inventory_rebuild");
        action.put("backpack", backpack);
        action.put("special_slots", new JSONArray()
                .put(new JSONObject().put("id", 0).put("item", weaponBag)));
        return action;
    }

    private static JSONObject itemUpdateAction() {
        JSONObject action = new JSONObject();
        action.put("action_name", "item_update");
        action.put("path", new JSONArray().put(1));
        action.put("item", item("Potion", 10));
        return action;
    }

    private static JSONObject itemAddAction(int index, JSONObject item) {
        return itemAddAction(new JSONArray().put(index), item);
    }

    private static JSONObject itemAddAction(JSONArray path, JSONObject item) {
        JSONObject action = new JSONObject();
        action.put("action_name", "item_add");
        action.put("path", path);
        action.put("item", item);
        return action;
    }

    private static JSONObject itemUpdatePatchAction(int index, JSONObject patch) {
        return itemUpdatePatchAction(new JSONArray().put(index), patch);
    }

    private static JSONObject itemUpdatePatchAction(JSONArray path, JSONObject patch) {
        JSONObject action = new JSONObject();
        action.put("action_name", "item_update");
        action.put("path", path);
        action.put("item", patch);
        return action;
    }

    private static JSONObject itemReplaceAction(int index, JSONObject item) {
        JSONObject action = new JSONObject();
        action.put("action_name", "item_replace");
        action.put("path", new JSONArray().put(index));
        action.put("item", item);
        return action;
    }

    private static JSONObject itemRemoveAction(int index) {
        return itemRemoveAction(new JSONArray().put(index));
    }

    private static JSONObject itemRemoveAction(JSONArray path) {
        JSONObject action = new JSONObject();
        action.put("action_name", "item_remove");
        action.put("path", path);
        return action;
    }

    private static JSONObject updateWindowAction() {
        JSONObject action = new JSONObject();
        action.put("action_name", "update_window");
        action.put("id", 42);
        action.put("type", "dialog");
        action.put("args", new JSONObject().put("message", "hello"));
        return action;
    }

    private static JSONObject item(String name, int quantity) {
        JSONObject item = new JSONObject();
        item.put("sprite_sheet", 0);
        item.put("image", 1);
        item.put("icon", -1);
        item.put("name", name);
        item.put("stackable", quantity > 1);
        item.put("quantity", quantity);
        item.put("known", true);
        item.put("cursed", false);
        item.put("identified", true);
        item.put("level_known", true);
        item.put("level", 0);
        item.put("energy_value", 0);
        item.put("glowing", JSONObject.NULL);
        item.put("emitter", JSONObject.NULL);
        return item;
    }
}
