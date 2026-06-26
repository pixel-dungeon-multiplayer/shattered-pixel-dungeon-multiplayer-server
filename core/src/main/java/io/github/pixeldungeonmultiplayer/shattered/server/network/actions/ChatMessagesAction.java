package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatMessagesAction implements LiveStateNetworkAction {
    private final ArrayList<LocalizedString> messages = new ArrayList<>();

    public ChatMessagesAction(@NotNull String message) {
        this(LocalizedString.raw(message));
    }

    public ChatMessagesAction(@NotNull LocalizedString message) {
        messages.add(message);
    }

    public ChatMessagesAction(@NotNull List<@NotNull LocalizedString> messages) {
        this.messages.addAll(messages);
    }

    public void addMessage(@NotNull LocalizedString message) {
        messages.add(message);
    }

    public @NotNull @UnmodifiableView List<LocalizedString> messages() {
        return Collections.unmodifiableList(messages);
    }

    @Override
    public @NotNull String actionName() {
        return "messages";
    }
}
