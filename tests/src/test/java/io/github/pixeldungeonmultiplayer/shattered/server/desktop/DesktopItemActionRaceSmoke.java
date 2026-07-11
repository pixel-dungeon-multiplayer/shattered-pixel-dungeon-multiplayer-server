package io.github.pixeldungeonmultiplayer.shattered.server.desktop;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import io.github.pixeldungeonmultiplayer.shattered.server.network.SendData;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.InventoryRebuildAction;
import io.github.pixeldungeonmultiplayer.shattered.testclient.SimulatedClient;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class DesktopItemActionRaceSmoke {
    private DesktopItemActionRaceSmoke() { }

    @Test
    @Tag("desktop")
    void smoke() throws Exception {
        DesktopSmoke.launch("itemActionRaceSmoke", RaceGame::new);
    }

    private static final class RaceGame extends DesktopSmoke {
        private RaceGame(int port) {
            super(port);
        }

        @Override protected String smokeThreadName() {
            return "SPDMP item-action-race client";
        }

        @Override protected void runTest() throws Exception {
            BlockingBag bag = new BlockingBag();
            Thread actor = null;
            waitForServer();
            try (SimulatedClient client = connectClient()) {
                joinAndWaitForScene(client);
                setupBag(bag);
                waitForClient(client, () -> client.inventory() != null
                        && client.inventory().itemsByPath().containsKey(pathOf(bag)), "bag was not rebuilt");

                RaceItem item = new RaceItem();
                List<Integer> itemPath = new ArrayList<>(pathOf(bag));
                itemPath.add(0);
                AtomicBoolean itemAdded = new AtomicBoolean();
                AtomicBoolean itemUpdated = new AtomicBoolean();
                // Register both hooks before releasing addItemDirect, otherwise the race is not observable.
                client.afterAction("item_add", (ignored, action) -> {
                    if (action.getJSONArray("path").toList().equals(itemPath)) {
                        itemAdded.set(true);
                    }
                });
                client.beforeAction("item_update", (ignored, action) -> {
                    if (action.getJSONArray("path").toList().equals(itemPath)) {
                        require(itemAdded.get(), "item_update arrived before item_add");
                        itemUpdated.set(true);
                    }
                });

                actor = new Thread(() -> item.collect(hero().belongings.backpack), "test-actor");
                actor.start();
                // The bag stops collect between the internal mutation and action creation.
                require(bag.added.await(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS), "item was not internally added");
                bag.allowAdd.countDown();
                waitForClient(client, itemUpdated::get, "item_update was not received");
                require(itemAdded.get(), "item_add was not received");
            } finally {
                bag.allowAdd.countDown();
                if (actor != null) {
                    actor.join(TIMEOUT_MILLIS);
                }
            }
        }

        private void setupBag(BlockingBag bag) throws InterruptedException {
            onGameThread(() -> {
                Hero hero = hero();
                require(hero.belongings.backpack.addItemDirect(bag), "could not add bag");
                bag.owner = hero;
                SendData.packAndSendAction(hero, new InventoryRebuildAction(hero));
                SendData.forceFlush(hero);
            });
        }

        private List<Integer> pathOf(Item item) {
            List<Integer> path = hero().belongings.pathOfItem(item);
            require(path != null, "no item path");
            return path;
        }
    }

    private static final class RaceItem extends Item {
        @Override public LocalizedString name() { return LocalizedString.raw("race-item"); }
    }

    private static final class BlockingBag extends Bag {
        private final CountDownLatch added = new CountDownLatch(1);
        private final CountDownLatch allowAdd = new CountDownLatch(1);

        @Override public boolean addItemDirect(Item item) {
            boolean result = super.addItemDirect(item);
            if (result) {
                added.countDown();
                try {
                    allowAdd.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return result;
        }

        @Override public boolean canHold(Item item) { return item instanceof RaceItem && super.canHold(item); }
        @Override public Icons getBagIcon() { return Icons.BACKPACK; }
        @Override public LocalizedString name() { return LocalizedString.raw("race-bag"); }
    }
}
