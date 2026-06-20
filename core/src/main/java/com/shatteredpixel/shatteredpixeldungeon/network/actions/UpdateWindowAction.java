package com.shatteredpixel.shatteredpixeldungeon.network.actions;

import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class UpdateWindowAction implements LiveStateNetworkAction {

    public final @NotNull Window window;

    @Contract(pure = true)
    public UpdateWindowAction(@NotNull Window window) {
        this.window = window;
    }

    @Override
    public @NotNull String actionName() {
        return "update_window";
    }
}
