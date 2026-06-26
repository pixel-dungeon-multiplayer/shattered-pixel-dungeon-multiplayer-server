package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class HeapUpdateAction implements LiveStateNetworkAction {
    public final @NotNull Heap heap;

    @Contract(pure = true)
    public HeapUpdateAction(@NotNull Heap heap) {
        this.heap = heap;
    }

    @Override
    @Contract(pure = true)
    public @NotNull String actionName() {
        return "heap_update";
    }
}
