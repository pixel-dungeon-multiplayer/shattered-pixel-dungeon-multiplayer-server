package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class TexturePackAction implements ImmutableNetworkAction {
    public final String data;

    @Contract(pure = true)
    public TexturePackAction(@NotNull String data) {
        this.data = data;
    }

    @Override
    @Contract(pure = true)
    public @NotNull String actionName() {
        return "texturepack";
    }
}
