package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import org.jetbrains.annotations.NotNull;

public class InventoryRebuildAction implements LiveStateNetworkAction {
    public final Hero hero;

    public InventoryRebuildAction(@NotNull Hero hero) {
        this.hero = hero;
    }

    @Override
    public @NotNull String actionName() {
        return "inventory_rebuild";
    }
}
