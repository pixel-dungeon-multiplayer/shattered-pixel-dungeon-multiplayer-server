package io.github.pixeldungeonmultiplayer.shattered.server.desktop;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import io.github.pixeldungeonmultiplayer.shattered.server.network.SendData;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.InventoryRebuildAction;
import io.github.pixeldungeonmultiplayer.shattered.headlessclient.HeadlessClient;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class DesktopInventoryCapacitySmoke {
    private static final int ITERATIONS = 50;

    private DesktopInventoryCapacitySmoke() { }

    @Test
    @Tag("desktop")
    void smoke() throws Exception {
        DesktopSmoke.launch("inventoryCapacitySmoke", CapacityGame::new);
    }

    private static final class CapacityGame extends DesktopSmoke {
        private final List<Item> inventoryItems = new ArrayList<>();

        private CapacityGame(int port) {
            super(port);
        }

        @Override protected String smokeThreadName() {
            return "SPDMP inventory-capacity client";
        }

        @Override protected void runTest() throws Exception {
            waitForServer();
            try (HeadlessClient client = connectClient()) {
                joinAndWaitForScene(client);
                fillBackpack();
                waitForClient(client, () -> client.inventory() != null
                                && client.inventory().backpack.items.size() == hero().belongings.backpack.capacity(),
                        "full inventory rebuild was not received");

                AtomicInteger removes = new AtomicInteger();
                AtomicInteger adds = new AtomicInteger();
                AtomicInteger readyActions = new AtomicInteger();
                    client.afterAction("item_remove", (ignored, action) -> removes.incrementAndGet());
                    client.afterAction("item_add", (ignored, action) -> adds.incrementAndGet());
                    // Only ready=true confirms that the server finished the current cell action.
                    client.afterAction("hero_ready", (ignored, action) -> {
                        if (action.optBoolean("ready", false)) {
                            readyActions.incrementAndGet();
                        }
                    });

                for (int i = 0; i < ITERATIONS; i++) {
                    int iteration = i;
                    Item incoming = placeOverflowItem();
                    int expectedReady = readyActions.get() + 1;
                    int expectedAdds = adds.get();
                    client.selectCell(hero().pos);
                    // A full-inventory pickup must finish without creating an item_add for the overflow item.
                    waitForClient(client, () -> readyActions.get() >= expectedReady,
                            "full-inventory pickup did not complete at iteration " + iteration);
                    require(adds.get() == expectedAdds, "full-inventory pickup sent item_add at iteration " + iteration);
                    require(!hero().belongings.backpack.contains(incoming),
                            "overflow item was collected at iteration " + iteration);

                    int itemIndex = i % inventoryItems.size();
                    Item discarded = inventoryItems.get(itemIndex);
                    onGameThread(() -> discarded.dropsDownHeap = true);
                    int expectedRemoves = removes.get() + 1;
                    int expectedPickupAdds = adds.get() + 1;
                    client.itemAction(pathOf(discarded), "DROP");
                    waitForClient(client, () -> removes.get() >= expectedRemoves,
                            "drop action was not applied at iteration " + iteration);
                    require(!hero().belongings.backpack.contains(discarded),
                            "dropped item remained in inventory at iteration " + iteration);

                    client.selectCell(hero().pos);
                    waitForClient(client, () -> adds.get() >= expectedPickupAdds,
                            "pickup action was not applied at iteration " + iteration);
                    require(hero().belongings.backpack.items().size() == hero().belongings.backpack.capacity(),
                            "server inventory is not full at iteration " + iteration);
                    require(hero().belongings.backpack.contains(incoming),
                            "overflow item was not collected after freeing a slot at iteration " + iteration);
                    inventoryItems.set(itemIndex, incoming);
                    assertInventoryMatches(serverInventory(), client.inventory());
                }
            }
        }

        private void fillBackpack() throws InterruptedException {
            onGameThread(() -> {
                Hero hero = hero();
                while (hero.belongings.backpack.items().size() < hero.belongings.backpack.capacity()) {
                    Item item = new TestInventoryItem();
                    require(hero.belongings.backpack.addItemDirect(item), "could not fill backpack");
                    inventoryItems.add(item);
                }
                SendData.packAndSendAction(hero, new InventoryRebuildAction(hero));
                SendData.forceFlush(hero);
            });
        }

        private Item placeOverflowItem() throws InterruptedException {
            AtomicReference<Item> result = new AtomicReference<>();
            onGameThread(() -> {
                Item incoming = new TestInventoryItem();
                Dungeon.level.drop(incoming, hero().pos);
                SendData.forceFlush(hero());
                result.set(incoming);
            });
            return result.get();
        }

        private List<Integer> pathOf(Item item) {
            List<Integer> path = hero().belongings.pathOfItem(item);
            require(path != null && !path.isEmpty(), "server item has no path: " + item);
            return path;
        }
    }

    private static final class TestInventoryItem extends Item {
        private static int nextId;
        private final int id = nextId++;
        { stackable = false; }
        @Override public LocalizedString name() { return LocalizedString.raw("test-item-" + id); }
    }
}
