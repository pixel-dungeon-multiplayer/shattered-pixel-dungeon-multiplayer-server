package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import org.jetbrains.annotations.NotNull;

public class EnchantingVisualAction implements LiveStateNetworkAction {
    public final int targetId;
    public final Item item;

    public EnchantingVisualAction(int targetId, @NotNull Item item) {
        this.targetId = targetId;
        this.item = item;
    }

    @Override
    public @NotNull String actionName() {
        return "enchanting_visual";
    }
}
