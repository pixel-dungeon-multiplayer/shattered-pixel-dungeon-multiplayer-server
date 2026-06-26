package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CharEmoAction implements ImmutableNetworkAction {
    public final int actorId;
    @Nullable
    public final String emotion;

    @Contract(pure = true)
    public CharEmoAction(int actorId, @Nullable String emotion) {
        this.actorId = actorId;
        this.emotion = emotion;
    }

    @Override
    @Contract(pure = true)
    public @NotNull String actionName() {
        return "char_emo";
    }
}
