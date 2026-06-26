package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public class UpdateFovAction implements LiveStateNetworkAction {
    @Nullable
    private final Hero hero;
    private final boolean @Nullable [] visible;

    @Contract(pure = true)
    public UpdateFovAction(@NotNull Hero hero) {
        this.hero = hero;
        this.visible = null;
    }

    @Contract(pure = true)
    public UpdateFovAction(boolean @NotNull [] visible) {
        this.hero = null;
        this.visible = Arrays.copyOf(visible, visible.length);
    }

    @Contract(pure = true)
    public boolean @NotNull [] visible() {
        return hero != null ? Objects.requireNonNull(hero.fieldOfView) : Objects.requireNonNull(visible);
    }

    @Override
    @Contract(pure = true)
    public @NotNull String actionName() {
        return "update_fov";
    }
}
