package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CellListenerPromptAction implements ImmutableNetworkAction {
    @Nullable
    public final LocalizedString prompt;

    @Contract(pure = true)
    public CellListenerPromptAction(@Nullable LocalizedString prompt) {
        this.prompt = prompt;
    }

    @Contract(pure = true)
    public CellListenerPromptAction(@Nullable CellSelector.Listener listener) {
        this(listener == null ? null : listener.prompt());
    }

    @Override
    public @NotNull String actionName() {
        return "cell_listener_prompt";
    }
}
