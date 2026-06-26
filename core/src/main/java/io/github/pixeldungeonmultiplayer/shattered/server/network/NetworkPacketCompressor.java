package io.github.pixeldungeonmultiplayer.shattered.server.network;

import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.ChatMessageAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.ChatMessagesAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.LiveStateNetworkAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.JournalSnapshotAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.ResizeLevelAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.SetLevelStatesAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.SetLevelTilesAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.UpdateCellsAction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * <p> Compresses accumulated packet actions without changing non-cell event order. </p>
 *
 * <p>Level cell diffs are allowed to move across other actions, but their own order is preserved:
 * later cell updates override earlier cell updates, and a later level snapshot overrides all
 * earlier pending cell updates for the same data kind. </p>
 *
 * <ul>A cell update is absorbed into the latest matching snapshot array when possible:
 * <li>update_cells.tiles updates the latest set_level_tiles.tiles snapshot;</li>
 * <li>update_cells.states updates the latest set_level_states.states snapshot; </li>
 * <li>if only one snapshot array exists, the uncovered part of update_cells remains pending;</li>
 * <li>if both tiles and states are covered by snapshots, update_cells is not emitted.</li>
 * </ul>
 * The returned list is always new; retained live actions may still be compacted in place.
 */
class NetworkPacketCompressor {

    @Contract("_->new")
    static @NotNull List<LiveStateNetworkAction> compress(@NotNull @UnmodifiableView List<LiveStateNetworkAction> actions) {
        LevelActionsCompressor levelActions = new LevelActionsCompressor();
        for (LiveStateNetworkAction action : actions) {
            levelActions.add(action);
        }
        return levelActions.toActions();
    }

    private static class LevelActionsCompressor {
        private final ArrayList<LiveStateNetworkAction> actions = new ArrayList<>();
        private final LinkedHashMap<Integer, Integer> pendingTiles = new LinkedHashMap<>();
        private final LinkedHashMap<Integer, Integer> pendingStates = new LinkedHashMap<>();
        private int @Nullable [] currentTiles;
        private int @Nullable [] currentStates;
        @Nullable
        private ChatMessagesAction currentMessagesAction;
        @Nullable
        private JournalSnapshotAction pendingJournalSnapshot;

        void add(@NotNull LiveStateNetworkAction action) {
            if (action instanceof ResizeLevelAction) {
                currentTiles = null;
                currentStates = null;
                clearPendingUpdates();
                actions.add(action);
            } else if (action instanceof SetLevelTilesAction) {
                currentTiles = ((SetLevelTilesAction) action).tiles;
                pendingTiles.clear();
                actions.add(action);
            } else if (action instanceof SetLevelStatesAction) {
                currentStates = ((SetLevelStatesAction) action).states;
                pendingStates.clear();
                actions.add(action);
            } else if (action instanceof UpdateCellsAction) {
                addCellsUpdate((UpdateCellsAction) action);
            } else if (action instanceof ChatMessageAction) {
                addMessage(((ChatMessageAction) action).text);
            } else if (action instanceof ChatMessagesAction) {
                addMessages((ChatMessagesAction) action);
            } else if (action instanceof JournalSnapshotAction) {
                pendingJournalSnapshot = (JournalSnapshotAction) action;
            } else {
                actions.add(action);
            }
        }

        private void addMessages(@NotNull ChatMessagesAction action) {
            for (LocalizedString text : action.messages()) {
                addMessage(text);
            }
        }

        private void addMessage(@NotNull LocalizedString text) {
            if (currentMessagesAction == null) {
                currentMessagesAction = new ChatMessagesAction(text);
                actions.add(currentMessagesAction);
                return;
            }
            currentMessagesAction.addMessage(text);
        }

        private void clearPendingUpdates() {
            pendingTiles.clear();
            pendingStates.clear();
        }

        private void addCellsUpdate(@NotNull UpdateCellsAction action) {
            for (int i = 0; i < action.positions.length; i++) {
                int pos = action.positions[i];
                if (pos < 0) {
                    continue;
                }
                if (action.tiles != null && i < action.tiles.length) {
                    int tile = action.tiles[i];
                    if (!applyCellUpdate(currentTiles, pos, tile)) {
                        pendingTiles.put(pos, tile);
                    }
                }
                if (action.states != null && i < action.states.length) {
                    int state = action.states[i];
                    if (!applyCellUpdate(currentStates, pos, state)) {
                        pendingStates.put(pos, state);
                    }
                }
            }
        }

        private boolean applyCellUpdate(int @Nullable [] values, int pos, int value) {
            if (values == null || pos >= values.length) {
                return false;
            }
            values[pos] = value;
            return true;
        }

        @Contract("->new")
        @NotNull List<LiveStateNetworkAction> toActions() {
            addPendingUpdates();
            if (pendingJournalSnapshot != null) {
                actions.add(pendingJournalSnapshot);
            }
            return new ArrayList<>(actions);
        }

        private void addPendingUpdates() {
            if (pendingTiles.isEmpty() && pendingStates.isEmpty()) {
                return;
            }
            if (!pendingTiles.isEmpty() && pendingTiles.keySet().equals(pendingStates.keySet())) {
                actions.add(createCellsUpdate(pendingTiles, pendingStates));
                pendingTiles.clear();
                pendingStates.clear();
                return;
            }
            if (!pendingTiles.isEmpty()) {
                actions.add(createCellsUpdate(pendingTiles, null));
                pendingTiles.clear();
            }
            if (!pendingStates.isEmpty()) {
                actions.add(createCellsUpdate(null, pendingStates));
                pendingStates.clear();
            }
        }

        private UpdateCellsAction createCellsUpdate(@Nullable LinkedHashMap<Integer, Integer> tileUpdates,
                                                    @Nullable LinkedHashMap<Integer, Integer> stateUpdates) {
            LinkedHashSet<Integer> positions = new LinkedHashSet<>();
            if (tileUpdates != null) positions.addAll(tileUpdates.keySet());
            if (stateUpdates != null) positions.addAll(stateUpdates.keySet());

            int[] positionsArray = new int[positions.size()];
            int[] tilesArray = tileUpdates == null ? null : new int[positions.size()];
            int[] statesArray = stateUpdates == null ? null : new int[positions.size()];

            int i = 0;
            for (Integer pos : positions) {
                positionsArray[i] = pos;
                if (tilesArray != null) tilesArray[i] = tileUpdates.get(pos);
                if (statesArray != null) statesArray[i] = stateUpdates.get(pos);
                i++;
            }

            return new UpdateCellsAction(positionsArray, tilesArray, statesArray);
        }
    }
}
