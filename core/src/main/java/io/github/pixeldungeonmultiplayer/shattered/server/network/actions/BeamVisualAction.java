package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import com.watabou.noosa.Image;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.dtos.BeamAnchor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class BeamVisualAction implements ImmutableNetworkAction {
    public final @NotNull Image image;
    public final @NotNull BeamAnchor from;
    public final @NotNull BeamAnchor to;
    public final float duration;

    @Contract(pure = true)
    public BeamVisualAction(@NotNull Image image, @NotNull BeamAnchor from, @NotNull BeamAnchor to, float duration) {
        this.image = image;
        this.from = from;
        this.to = to;
        this.duration = duration;
    }

    @Override
    @Contract(pure = true)
    public @NotNull String actionName() {
        return "beam_visual";
    }
}
