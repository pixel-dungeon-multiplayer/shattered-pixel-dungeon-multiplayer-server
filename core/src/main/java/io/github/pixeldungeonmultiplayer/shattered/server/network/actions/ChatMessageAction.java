package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class ChatMessageAction implements ImmutableNetworkAction {
    @NotNull
    public final LocalizedString text;

    @Contract(pure = true)
    public ChatMessageAction(@NotNull LocalizedString text) {
        this.text = text;
    }

    @Contract(pure = true)
    public ChatMessageAction(@NotNull String text) {
        this.text = LocalizedString.raw(text);
    }

    @Override
    @Contract(pure = true)
    public @NotNull String actionName() {
        return "messages";
    }
}
