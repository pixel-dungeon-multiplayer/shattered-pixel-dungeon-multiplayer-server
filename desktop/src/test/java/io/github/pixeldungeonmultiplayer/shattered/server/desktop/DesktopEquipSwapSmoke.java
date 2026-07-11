package io.github.pixeldungeonmultiplayer.shattered.server.desktop;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CapeOfThorns;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.ChaliceOfBlood;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfAccuracy;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEvasion;
import io.github.pixeldungeonmultiplayer.shattered.server.network.SendData;
import io.github.pixeldungeonmultiplayer.shattered.testclient.ClientWindow;
import io.github.pixeldungeonmultiplayer.shattered.testclient.SimulatedClient;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class DesktopEquipSwapSmoke {
    private DesktopEquipSwapSmoke() { }

    @Test
    @Tag("desktop")
    void smoke() throws Exception {
        DesktopSmoke.launch("equipSwapSmoke", EquipSwapGame::new);
    }

    private static final class EquipSwapGame extends DesktopSmoke {
        private EquipSwapGame(int port) {
            super(port);
        }

        @Override protected String smokeThreadName() {
            return "SPDMP equip-swap client";
        }

        @Override protected void runTest() throws Exception {
            waitForServer();
            try (SimulatedClient client = connectClient()) {
                joinAndWaitForScene(client);
                List<Item> items = seedInventory();
                waitForClient(client, () -> allPresent(client, items), "seed items were not received");

                equip(client, items.get(0), -3);
                equip(client, items.get(1), -5);
                equip(client, items.get(2), -4);

                AtomicReference<Integer> equipWindowId = new AtomicReference<>();
                client.afterAction("update_window", (ignored, action) -> equipWindowId.set(action.getInt("id")));
                client.itemAction(pathOf(items.get(3)), "EQUIP");
                waitForClient(client, () -> equipWindowId.get() != null, "equip slot window was not received");
                ClientWindow window = client.windows().get(equipWindowId.get());
                require(window != null, "equip slot window was not retained by the client");

                AtomicBoolean windowHidden = new AtomicBoolean();
                client.afterAction("hide_window", (ignored, action) -> {
                    if (action.getInt("id") == window.id) {
                        windowHidden.set(true);
                    }
                });
                client.selectWindow(window.id, 1);
                waitForClient(client, windowHidden::get, "equip slot window was not hidden");

                assertInventoryMatches(serverInventory(), client.inventory());
                require(client.inventory().itemAt(Arrays.asList(-4)) != null,
                        "replacement item is missing from misc slot");
            }
        }

        private List<Item> seedInventory() throws InterruptedException {
            AtomicReference<List<Item>> items = new AtomicReference<>();
            onGameThread(() -> {
                Hero hero = hero();
                List<Item> seeded = Arrays.asList(
                        new ChaliceOfBlood(), new RingOfAccuracy(), new RingOfEvasion(), new CapeOfThorns());
                for (Item item : seeded) {
                    require(item.collect(hero.belongings.backpack), "could not seed " + item.getClass().getSimpleName());
                }
                SendData.forceFlush(hero);
                items.set(seeded);
            });
            return items.get();
        }

        private void equip(SimulatedClient client, Item item, int targetSlot) throws IOException {
            client.itemAction(pathOf(item), "EQUIP");
            waitForClient(client, () -> client.inventory().itemAt(Arrays.asList(targetSlot)) != null,
                    "item was not equipped into slot " + targetSlot);
        }

        private List<Integer> pathOf(Item item) {
            List<Integer> path = hero().belongings.pathOfItem(item);
            require(path != null && !path.isEmpty(), "server item has no inventory path: " + item);
            return path;
        }

        private boolean allPresent(SimulatedClient client, List<Item> items) {
            if (client.inventory() == null) {
                return false;
            }
            for (Item item : items) {
                if (client.inventory().itemAt(pathOf(item)) == null) {
                    return false;
                }
            }
            return true;
        }
    }
}
