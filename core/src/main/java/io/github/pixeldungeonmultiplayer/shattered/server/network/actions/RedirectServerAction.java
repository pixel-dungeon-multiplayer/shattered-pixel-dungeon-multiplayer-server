package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import io.github.pixeldungeonmultiplayer.shattered.server.network.packets.RedirectPacket;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class RedirectServerAction implements LiveStateNetworkAction {
    public final @NotNull RedirectPacket redirectPacket;

    @Contract(pure = true)
    public RedirectServerAction(@NotNull RedirectPacket redirectPacket) {
        this.redirectPacket = redirectPacket;
    }

    @Override
    @Contract(pure = true)
    public @NotNull String actionName() {
        return "redirect_server";
    }
}
