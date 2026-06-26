package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShowStatusAction implements ImmutableNetworkAction {
    @Nullable
    public final Float x;
    @Nullable
    public final Float y;
    @Nullable
    public final Integer key;
    @NotNull
    public final String text;
    public final int color;
    public final boolean ignorePosition;

    public ShowStatusAction(@Nullable Float x, @Nullable Float y, @Nullable Integer key, @NotNull String text, int color, boolean ignorePosition) {
        this.x = x;
        this.y = y;
        this.key = key;
        this.text = text;
        this.color = color;
        this.ignorePosition = ignorePosition;
    }

    @Override
    public @NotNull String actionName() {
        return "show_status";
    }
}
