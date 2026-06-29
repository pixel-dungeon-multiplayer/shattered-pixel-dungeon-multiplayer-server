package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.dtos.FloatingTextAnchor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class ShowFloatingTextAction implements ImmutableNetworkAction {
    public final @NotNull FloatingTextAnchor anchor;
    public final @NotNull LocalizedString text;
    public final int color;
    public final int icon;
    public final boolean left;

    @Contract(pure = true)
    public ShowFloatingTextAction(@NotNull FloatingTextAnchor anchor, @NotNull LocalizedString text, int color, int icon, boolean left) {
        this.anchor = anchor;
        this.text = text;
        this.color = color;
        this.icon = icon;
        this.left = left;
    }

    @Override
    @Contract(pure = true)
    public @NotNull String actionName() {
        return "show_floating_text";
    }
}
