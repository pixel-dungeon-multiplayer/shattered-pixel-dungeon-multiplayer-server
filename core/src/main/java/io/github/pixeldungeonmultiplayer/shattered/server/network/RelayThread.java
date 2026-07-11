package io.github.pixeldungeonmultiplayer.shattered.server.network;

import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import io.github.pixeldungeonmultiplayer.shattered.server.utils.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;

import static io.github.pixeldungeonmultiplayer.shattered.server.network.ClientThread.CHARSET;

public class RelayThread extends Thread {
    private static final int RELAY_PROTOCOL_VERSION = 2;
    private static final int UPDATE_DELAY = 3000;
    protected OutputStreamWriter writeStream;
    protected BufferedWriter writer;
    protected InputStreamReader readStream;
    private BufferedReader reader;
    protected Socket clientSocket;
    private Callback callback = null;
    static int restartCount = 0;

    public RelayThread(){
        this.callback = new Callback() {
            @Override
            public void onDisconnect() {
            }
        };
    }
    public RelayThread(Callback callback){
        this.callback = callback;
    }

    @Override
    public void interrupt() {
        super.interrupt();
        if (clientSocket != null) {
            try {
                clientSocket.close();
            } catch (IOException ignored) {}
            clientSocket = null;
        }
    }
    private static int getRelayPort(){
        if (!SPDSettings.useCustomRelay()){
            return SPDSettings.defaultRelayServerPort;
        }
        int port = SPDSettings.customRelayPort();
        return (port != 0)? port: SPDSettings.defaultRelayServerPort;
    }

    private static String getRelayAddress(){
        if (!SPDSettings.useCustomRelay()){
            return SPDSettings.defaultRelayServerAddress;
        }
        String address = SPDSettings.customRelayAddress();
        return (!"".equals(address))? address : SPDSettings.defaultRelayServerAddress;
    }

    public void run() {
        Socket socket = null;
        String relayServerAddress = getRelayAddress();
        int relayPort = getRelayPort();
        Log.i("Relay", "Connecting to %s:%d...", relayServerAddress, relayPort);
        try {
            socket = new Socket(relayServerAddress, relayPort);
            socket.setSoTimeout(UPDATE_DELAY);
        } catch (IOException e) {
            Log.e("Relay", "Connection to %s:%d failed: %s", relayServerAddress, relayPort, e.getMessage());
            e.printStackTrace();
            this.callback.onDisconnect();
            return;
        }
        this.clientSocket = socket;
        try {
            writeStream = new OutputStreamWriter(
                    clientSocket.getOutputStream(),
                    Charset.forName(CHARSET).newEncoder()
            );
            readStream = new InputStreamReader(
                    clientSocket.getInputStream(),
                    Charset.forName(CHARSET).newDecoder()
            );
            reader = new BufferedReader(readStream);
            writer = new BufferedWriter(writeStream, 16384);

            Log.i("Relay", "Connected successfully, registering server...");
            sendServerUpdate(null);
            long serverId = 0;
            while (true) {
                String json;
                try {
                    json = reader.readLine();
                } catch (SocketTimeoutException e) {
                    sendServerUpdate(serverId == 0 ? null : serverId);
                    continue;
                }
                if (json == null){
                    if (restartCount > 3) {
                        GLog.h("relay thread stopped");
                    }
                    Log.w("Relay", "Connection closed by remote server.");
                    socket.close();
                    this.callback.onDisconnect();
                    if (restartCount < 10) {
                        if (restartCount > 3) {
                            Log.w("Relay", "Restarting relay");
                        }
                        new RelayThread().start();
                        restartCount++;
                    } else {
                        Log.e("Relay", "Starting relay failed");
                    }
                    return;
                }
                JSONObject action = new JSONObject(json);
                String actionName = action.optString("action", "");
                if ("server_registered".equals(actionName)) {
                    serverId = action.optLong("server_id", serverId);
                    Log.i("Relay", "Server registered successfully. Server ID: %d", serverId);
                } else if ("ping".equals(actionName)) {
                    JSONObject pong = new JSONObject();
                    pong.put("action", "pong");
                    pong.put("server_id", serverId);
                    writer.write(pong.toString());
                    writer.write('\n');
                    writer.flush();
                } else if ("client_requested".equals(actionName)) {
                    String connectId = action.optString("connect_id", "unknown");
                    long reqServerId = action.optLong("server_id", 0);
                    Log.i("Relay", "Incoming client request. Connect ID: %s, Server ID: %d", connectId, reqServerId);
                    Socket client = new Socket(relayServerAddress, relayPort);
                    JSONObject accept = new JSONObject();
                    accept.put("action", "accept_client");
                    accept.put("server_id", reqServerId);
                    accept.put("connect_id", connectId);
                    BufferedWriter acceptWriter = new BufferedWriter(new OutputStreamWriter(
                            client.getOutputStream(),
                            Charset.forName(CHARSET).newEncoder()
                    ), 16384);
                    acceptWriter.write(accept.toString());
                    acceptWriter.write('\n');
                    acceptWriter.flush();
                    Log.i("Relay", "Client tunnel accepted, starting client handler thread...");
                    Server.startClientThread(client);
                } else if ("error".equals(actionName)) {
                    String errMsg = action.optString("message", action.optString("code", "unknown"));
                    Log.e("Relay", "Relay error: %s", errMsg);
                    GLog.h("Relay error: {0}", errMsg);
                }
            }
        } catch (IOException | JSONException e) {
            Log.e("Relay", "Relay thread error: %s", e.getMessage());
            e.printStackTrace();
            try {
                Thread.sleep((1000 * new java.util.Random().nextInt(10)));
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
            GLog.h("relay thread stopped");
            this.callback.onDisconnect();
            restartCount = 0;
            new RelayThread().start();
            GLog.h("Relay thread restarted");
        }
    }

    private void sendServerUpdate(Long serverId) throws IOException, JSONException {
        JSONObject update = new JSONObject();
        update.put("action", "update_server");
        if (serverId != null && serverId != 0) {
            update.put("server_id", serverId);
        }
        update.put("name", SPDSettings.serverName());
        update.put("server_info", Server.serverInfo());
        update.put("server_version", com.watabou.noosa.Game.version);
        update.put("server_version_code", com.watabou.noosa.Game.versionCode);
        update.put("server_protocol_version", RELAY_PROTOCOL_VERSION);
        writer.write(update.toString());
        writer.write('\n');
        writer.flush();
    }

    public interface Callback {
         void onDisconnect();
    }
}
