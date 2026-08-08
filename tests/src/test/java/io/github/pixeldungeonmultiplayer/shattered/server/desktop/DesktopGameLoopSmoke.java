package io.github.pixeldungeonmultiplayer.shattered.server.desktop;

import io.github.pixeldungeonmultiplayer.shattered.server.network.ClientThread;
import io.github.pixeldungeonmultiplayer.shattered.server.network.Protocol;
import io.github.pixeldungeonmultiplayer.shattered.headlessclient.ClientState;
import io.github.pixeldungeonmultiplayer.shattered.headlessclient.HeadlessClient;
import org.json.JSONObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.Charset;
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
            runQueryProtocolSmoke();
            runHandshakeSmoke();
            runJoinInventorySmoke();
        }

        private void runQueryProtocolSmoke() throws IOException {
            try (Socket socket = new Socket("127.0.0.1", port)) {
                socket.setSoTimeout(500);
                Charset charset = Charset.forName(ClientThread.CHARSET);
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        socket.getInputStream(), charset.newDecoder()
                ));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                        socket.getOutputStream(), charset.newEncoder()
                ));

                JSONObject hello = new JSONObject(reader.readLine());
                require(Protocol.PACKET_HANDSHAKE.equals(hello.getString(Protocol.FIELD_PACKET_TYPE)),
                        "query connection did not receive handshake");

                writePacket(writer, new JSONObject().put(Protocol.FIELD_PACKET_TYPE, "unexpected"));
                writePacket(writer, new JSONObject().put(
                        Protocol.FIELD_PACKET_TYPE, Protocol.PACKET_STATUS_REQUEST
                ));

                JSONObject response = new JSONObject(reader.readLine());
                require(Protocol.PACKET_SERVER_STATUS.equals(response.getString(Protocol.FIELD_PACKET_TYPE)),
                        "query connection did not receive server status");
                JSONObject serverInfo = response.getJSONObject("server_info");
                require("desktop-gameLoopSmoke-server".equals(serverInfo.getString("server_id")),
                        "server status contained unexpected server id");
                require(serverInfo.getInt("max_players") == 2,
                        "server status contained unexpected player limit");
            }
        }

        private void writePacket(BufferedWriter writer, JSONObject packet) throws IOException {
            writer.write(packet.toString());
            writer.write('\n');
            writer.flush();
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
