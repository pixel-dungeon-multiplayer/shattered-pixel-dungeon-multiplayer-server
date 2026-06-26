package io.github.pixeldungeonmultiplayer.shattered.server.network;

import io.github.pixeldungeonmultiplayer.shattered.server.utils.Log;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;

public class QueryClientThread extends Thread {
    private static final int JOIN_TIMEOUT = 30_000;

    private final Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    public QueryClientThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            socket.setSoTimeout(JOIN_TIMEOUT);
            reader = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(),
                    Charset.forName(ClientThread.CHARSET).newDecoder()
            ));
            writer = new BufferedWriter(new OutputStreamWriter(
                    socket.getOutputStream(),
                    Charset.forName(ClientThread.CHARSET).newEncoder()
            ), 16384);

            sendHello();
            readQueryPackets();
        } catch (SocketTimeoutException e) {
            closeSocket();
        } catch (IOException | JSONException e) {
            Log.w("QueryClientThread", "Query connection failed. " + e);
            closeSocket();
        }
    }

    private void readQueryPackets() throws IOException, JSONException {
        String json;
        while ((json = reader.readLine()) != null) {
            JSONObject packet = new JSONObject(json);
            String packetType = packet.optString(Protocol.FIELD_PACKET_TYPE, "");
            if (Protocol.PACKET_STATUS_REQUEST.equals(packetType)) {
                sendStatus();
            } else if (Protocol.PACKET_JOIN.equals(packetType)) {
                if (!isCompatibleJoin(packet)) {
                    Server.rejectClient(socket, "unsupported_protocol", "Unsupported client protocol");
                    closeSocket();
                    return;
                }
                Server.joinClient(socket, packet.optString("hero_class", "random"), packet.optString("uuid", ""), protocolVersion(packet));
                return;
            } else {
                Log.w("QueryClientThread", "Unexpected query packet: " + packetType);
            }
        }
        closeSocket();
    }

    private boolean isCompatibleJoin(JSONObject packet) {
        int protocolVersion = protocolVersion(packet);
        return Protocol.NAME.equals(packet.optString(Protocol.FIELD_PROTOCOL, ""))
                && protocolVersion >= Protocol.MIN_VERSION
                && protocolVersion <= Protocol.VERSION;
    }

    private int protocolVersion(JSONObject packet) {
        return packet.optInt(Protocol.FIELD_VERSION, -1);
    }

    private void sendHello() throws IOException, JSONException {
        JSONObject hello = new JSONObject();
        hello.put(Protocol.FIELD_PACKET_TYPE, Protocol.PACKET_HANDSHAKE);
        hello.put(Protocol.FIELD_PROTOCOL, Protocol.NAME);
        hello.put(Protocol.FIELD_VERSION, Protocol.VERSION);
        hello.put(Protocol.FIELD_SERVER_ID, SPDSettings.serverUUID());
        writePacket(hello);
    }

    private void sendStatus() throws IOException, JSONException {
        JSONObject status = new JSONObject();
        status.put(Protocol.FIELD_PACKET_TYPE, Protocol.PACKET_SERVER_STATUS);
        status.put("server_info", Server.serverInfo());
        writePacket(status);
    }

    private void writePacket(JSONObject packet) throws IOException {
        writer.write(packet.toString());
        writer.write('\n');
        writer.flush();
    }

    private void closeSocket() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}
