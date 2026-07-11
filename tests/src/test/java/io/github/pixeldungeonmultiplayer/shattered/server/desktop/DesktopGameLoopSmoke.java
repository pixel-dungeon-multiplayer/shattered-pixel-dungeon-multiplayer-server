package io.github.pixeldungeonmultiplayer.shattered.server.desktop;

import io.github.pixeldungeonmultiplayer.shattered.server.network.Protocol;
import io.github.pixeldungeonmultiplayer.shattered.headlessclient.ClientState;
import io.github.pixeldungeonmultiplayer.shattered.headlessclient.HeadlessClient;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public final class DesktopGameLoopSmoke {
    private DesktopGameLoopSmoke() { }

    @Test
    @Tag("desktop")
    void smoke() throws Exception {
        DesktopSmoke.launch("gameLoopSmoke", 2, GameLoopSmoke::new);
    }

    private static final class GameLoopSmoke extends DesktopSmoke {
        private GameLoopSmoke(int port) {
            super(port);
        }

        @Override protected String smokeThreadName() {
            return "SPDMP game-loop smoke client";
        }

        @Override protected void runTest() throws Exception {
            waitForServer();
            runHandshakeSmoke();
            runJoinInventorySmoke();
        }

        private void runHandshakeSmoke() throws IOException {
            try (HeadlessClient client = connectClient()) {
                client.parseNext();
                require(client.state() == ClientState.HELLO_RECEIVED, "client did not receive hello");
                require(Protocol.NAME.equals(client.protocolName()), "unexpected protocol");
                require(Protocol.VERSION == client.protocolVersion(), "unexpected protocol version");
                require("desktop-gameLoopSmoke-server".equals(client.serverId()), "unexpected server id");
            }
        }

        private void runJoinInventorySmoke() throws IOException {
            AtomicBoolean checkedInventory = new AtomicBoolean();
            try (HeadlessClient client = connectClient()) {
                client.parseNext();
                // Inventory is valid only after the server has completed the initial level transition.
                client.afterAction("interlevel_scene", (ignored, action) -> {
                    if ("fade_out".equals(action.optString("state", ""))) {
                        assertInventoryMatches(serverInventory(), client.inventory());
                        checkedInventory.set(true);
                    }
                });
                client.join("warrior", "");
                waitForClient(client, checkedInventory::get, "did not receive interlevel fade_out before timeout");
            }
        }
    }
}
