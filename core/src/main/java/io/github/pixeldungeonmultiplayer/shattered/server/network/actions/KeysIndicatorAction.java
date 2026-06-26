package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import com.shatteredpixel.shatteredpixeldungeon.ui.KeyDisplay;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KeysIndicatorAction implements ImmutableNetworkAction {
    public final List<Integer> keysCount;

    public KeysIndicatorAction() {
        this(keyCounts());
    }

    @Contract(pure = true)
    public KeysIndicatorAction(@NotNull List<Integer> keysCount) {
        this.keysCount = Collections.unmodifiableList(new ArrayList<>(keysCount));
    }

    private static List<Integer> keyCounts() {
        ArrayList<Integer> counts = new ArrayList<>();
        if (KeyDisplay.keys != null) {
            for (int key : KeyDisplay.keys) {
                counts.add(key);
            }
        }
        return counts;
    }

    @Override
    public @NotNull String actionName() {
        return "keys_indicator";
    }
}
