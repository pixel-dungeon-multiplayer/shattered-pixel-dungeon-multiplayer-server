package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class BlobUpdateAction implements LiveStateNetworkAction {
    public final @NotNull Blob blob;

    @Contract(pure = true)
    public BlobUpdateAction(@NotNull Blob blob) {
        this.blob = blob;
    }

    @Override
    @Contract(pure = true)
    public @NotNull String actionName() {
        return "blob_update";
    }
}
