package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class CharSpriteStateAction implements ImmutableNetworkAction {
    public final int actorId;
    public final CharSprite.State state;
    public final boolean remove;

    @Contract(pure = true)
    public CharSpriteStateAction(int actorId, @NotNull CharSprite.State state, boolean remove) {
        this.actorId = actorId;
        this.state = state;
        this.remove = remove;
    }

    @Override
    @Contract(pure = true)
    public @NotNull String actionName() {
        return remove ? "char_sprite_state_remove" : "char_sprite_state_add";
    }
}
