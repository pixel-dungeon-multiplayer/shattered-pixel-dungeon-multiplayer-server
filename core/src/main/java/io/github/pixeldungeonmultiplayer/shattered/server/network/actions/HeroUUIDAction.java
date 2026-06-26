package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class HeroUUIDAction implements ImmutableNetworkAction {
    public final String uuid;

    @Contract(pure = true)
    public HeroUUIDAction(String uuid) {
        this.uuid = uuid;
    }

    @Override
    @Contract(pure = true)
    public @NotNull String actionName() {
        return "hero_uuid";
    }
}
