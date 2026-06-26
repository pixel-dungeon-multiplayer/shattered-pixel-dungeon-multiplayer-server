package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import org.jetbrains.annotations.NotNull;

public class CharUpdateAction implements LiveStateNetworkAction {
    public final Char character;

    public CharUpdateAction(@NotNull Char character) {
        this.character = character;
    }

    @Override
    public @NotNull String actionName() {
        return "char_update";
    }
}
