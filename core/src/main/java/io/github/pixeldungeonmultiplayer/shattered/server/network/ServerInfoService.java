package io.github.pixeldungeonmultiplayer.shattered.server.network;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerInfoService {
    private static Thread listener;
    public static void start() {
        if (SPDSettings.useServerInfoService()) {
            listener = new Thread(new ConnectionLister());
            listener.start();
        }
    }

    public static void stop() {
        if (listener != null) {
            listener.interrupt();
        }
    }
    private static class ConnectionLister implements Runnable{

        @Override
        public void run() {
            try {
                ServerSocket serverSocket = new ServerSocket(SPDSettings.serverPort());
                while (true) {
                    handleConnection(serverSocket.accept());
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void handleConnection(Socket socket){
            JSONObject serverInfo = new JSONObject();
            serverInfo.put("name", SPDSettings.serverName());
            serverInfo.put("players", Server.onlinePlayers());
            serverInfo.put("max_players", Server.clients.length);
            serverInfo.put("challenges", SPDSettings.challenges());
            serverInfo.put("current_floor", Dungeon.depth);
            serverInfo.put("port", Server.localPort);
            serverInfo.put("motd", SPDSettings.motd());
            try {
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(serverInfo.toString().getBytes());
                outputStream.write("\n".getBytes());
                outputStream.flush();
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
