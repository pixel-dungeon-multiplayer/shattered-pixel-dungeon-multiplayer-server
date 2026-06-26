package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import org.jetbrains.annotations.NotNull;

public class PlantUpdateAction implements LiveStateNetworkAction {
    public final int pos;
    public final Plant plant;

    public PlantUpdateAction(int pos, @NotNull Plant plant) {
        this.pos = pos;
        this.plant = plant;
    }

    @Override
    public @NotNull String actionName() {
        return "plant_update";
    }
}
