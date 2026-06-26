package io.github.pixeldungeonmultiplayer.shattered.server.network.packets;

import org.json.JSONObject;

import java.net.InetSocketAddress;

public class RedirectPacket  {
    InetSocketAddress destination;
    String uuid;
    String password;
    //if true the destination isn't game server, it's the server info service
    boolean serverInfo;
    public JSONObject toJSON() {
        JSONObject object = new JSONObject();
        object.put("host", destination.getAddress().getHostAddress());
        object.put("port", destination.getPort());
        if (uuid != null) {
            object.put("uuid", uuid);
        }
        if (password != null) {
            object.put("password", password);
        }
        return object;
    }

    public RedirectPacket(InetSocketAddress destination, String uuid, String password) {
        this.destination = destination;
        this.uuid = uuid;
        this.password = password;
    }
    public RedirectPacket(InetSocketAddress destination, String uuid, String password, boolean serverInfo) {
        this(destination, uuid, password);
        this.destination = destination;
        this.uuid = uuid;
        this.password = password;
        this.serverInfo = serverInfo;
    }
}
