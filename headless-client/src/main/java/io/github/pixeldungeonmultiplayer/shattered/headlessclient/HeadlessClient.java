package io.github.pixeldungeonmultiplayer.shattered.headlessclient;


import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import io.github.pixeldungeonmultiplayer.shattered.server.network.ClientThread;
import io.github.pixeldungeonmultiplayer.shattered.server.network.Protocol;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.RequiresNonNull;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.*;

public final class HeadlessClient implements Closeable {

    private @NotNull ClientState state = ClientState.NEW;
    private @MonotonicNonNull Socket socket;
    private @MonotonicNonNull BufferedReader reader;
    private @MonotonicNonNull BufferedWriter writer;
    private @MonotonicNonNull JSONObject helloPacket;
    private @MonotonicNonNull ClientInventory inventory;
    private final @NotNull Map<@NotNull Integer, @NotNull ClientWindow> windows = new HashMap<>();
    private final @NotNull List<JSONObject> unhandledActions = new ArrayList<>();
    private final @NotNull Map<@NotNull String, @NotNull List<@NotNull ActionHook>> beforeActionHooks = new HashMap<>();
    private final @NotNull Map<@NotNull String, @NotNull List<@NotNull ActionHook>> afterActionHooks = new HashMap<>();
    private final @NotNull Map<@NotNull String, @NotNull List<@NotNull ActionHook>> insteadActionHooks = new HashMap<>();
    private final @NotNull List<@NotNull PacketHook> beforePacketHooks = new ArrayList<>();

    @Contract(pure = true)
    public ClientState state() {
        return state;
    }

    @EnsuresNonNull("this.helloPacket")
    @Contract(pure = true)
    public @NotNull JSONObject helloPacket() {
        requireState(ClientState.HELLO_RECEIVED);
        Objects.requireNonNull(helloPacket);
        return helloPacket;
    }

    @Contract(pure = true)
    public ClientInventory inventory() {
        return Objects.requireNonNull(inventory);
    }

    @Contract(pure = true)
    public @UnmodifiableView @NotNull Map<@NotNull Integer, @NotNull ClientWindow> windows() {
        return Collections.unmodifiableMap(windows);
    }

    @Contract(pure = true)
    public List<JSONObject> unhandledActions() {
        return Collections.unmodifiableList(unhandledActions);
    }

    @Contract("_ -> this")
    public @NotNull HeadlessClient connect(Socket socket) throws IOException {
        requireState(ClientState.NEW);
        this.socket = socket;
        Charset charset = Charset.forName(ClientThread.CHARSET);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), charset.newDecoder()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), charset.newEncoder()));
        state = ClientState.CONNECTED;
        return this;
    }

    @EnsuresNonNull("this.reader")
    @Contract("->this")
    public @NotNull HeadlessClient parseNext() throws IOException {
        requireConnected();
        String line = reader.readLine();
        if (line == null) {
            state = ClientState.DISCONNECTED;
            throw new IOException("Server disconnected");
        }

        parse(new JSONObject(line));
        return this;
    }

    public void parse(JSONObject packet) {
        for (PacketHook hook : beforePacketHooks) {
            hook.handle(this, packet);
        }
        try {
            String packetType = packet.optString(Protocol.FIELD_PACKET_TYPE, "");
            if (Protocol.PACKET_HANDSHAKE.equals(packetType)) {
                parseHandshake(packet);
            } else if (Protocol.PACKET_ACTIONS_BATCH.equals(packetType)) {
                parseActionsBatch(packet);
            } else if (Protocol.PACKET_DISCONNECT.equals(packetType)) {
                state = ClientState.DISCONNECTED;
            } else {
                throw new IllegalStateException("Unexpected packet type: " + packetType);
            }
        } catch (RuntimeException e) {
            state = ClientState.FAILED;
            throw e;
        }
    }

    private void parseActionsBatch(@NotNull JSONObject packet) {
        requireState(ClientState.HELLO_RECEIVED);
        JSONArray actions = packet.optJSONArray("actions");
        if (actions == null) {
            return;
        }
        for (int i = 0; i < actions.length(); i++) {
            parseAction(actions.getJSONObject(i));
        }
    }

    private void parseAction(@NotNull JSONObject action) {
        String actionName = action.optString("action_name", "");
        runHooks(beforeActionHooks, actionName, action);
        if (!runHooks(insteadActionHooks, actionName, action)) {
            parseActionDefault(actionName, action);
        }
        runHooks(afterActionHooks, actionName, action);
    }

    private void parseActionDefault(@NotNull String actionName, @NotNull JSONObject action) {
        switch (actionName) {
            case "inventory_rebuild":
                inventory = ClientInventory.fromJson(action);
                break;
            case "item_add":
                if (inventory != null) {
                    inventory.addItem(intList(action.getJSONArray("path")), ClientItem.fromJson(action.getJSONObject("item")));
                }
                break;
            case "item_update":
                if (inventory != null) {
                    inventory.updateItem(intList(action.getJSONArray("path")), action.getJSONObject("item"));
                }
                break;
            case "item_replace":
                if (inventory != null) {
                    inventory.replaceItem(intList(action.getJSONArray("path")), ClientItem.fromJson(action.getJSONObject("item")));
                }
                break;
            case "item_remove":
                if (inventory != null) {
                    inventory.removeItem(intList(action.getJSONArray("path")));
                }
                break;
            case "update_window": {
                ClientWindow window = ClientWindow.fromJson(action);
                windows.put(window.id, window);
                break;
            }
            case "hide_window":
                windows.remove(action.getInt("id"));
                break;
            default:
                unhandledActions.add(action);
                break;
        }
    }

    @RequiresNonNull("this.writer")
    public void send(@NotNull JSONObject packet) throws IOException {
        if (state == ClientState.NEW || state == ClientState.DISCONNECTED || state == ClientState.FAILED) {
            throw new IllegalStateException("Client is not connected: " + state);
        }
        writer.write(packet.toString());
        writer.write('\n');
        writer.flush();
    }

    public void join(String heroClass, String uuid) throws IOException {
        requireConnected();
        JSONObject packet = new JSONObject();
        packet.put(Protocol.FIELD_PACKET_TYPE, Protocol.PACKET_JOIN);
        packet.put(Protocol.FIELD_PROTOCOL, Protocol.NAME);
        packet.put(Protocol.FIELD_VERSION, protocolVersion());
        packet.put("hero_class", heroClass);
        packet.put("uuid", uuid);
        send(packet);
    }

    public void itemAction(List<Integer> slot, String actionName) throws IOException {
        requireConnected();
        JSONObject action = new JSONObject();
        action.put("slot", new JSONArray(slot));
        action.put("action_name", actionName);

        JSONObject packet = new JSONObject();
        packet.put(Protocol.FIELD_PACKET_TYPE, Protocol.PACKET_CLIENT_COMMAND);
        packet.put("action", action);
        send(packet);
    }

    public void selectWindow(int id, int button) throws IOException {
        requireConnected();
        JSONObject selection = new JSONObject();
        selection.put("id", id);
        selection.put("button", button);

        JSONObject packet = new JSONObject();
        packet.put(Protocol.FIELD_PACKET_TYPE, Protocol.PACKET_CLIENT_COMMAND);
        packet.put("window", selection);
        send(packet);
    }

    public void selectCell(int cell) throws IOException {
        requireConnected();
        JSONObject packet = new JSONObject();
        packet.put(Protocol.FIELD_PACKET_TYPE, Protocol.PACKET_CLIENT_COMMAND);
        packet.put("cell_listener", cell);
        send(packet);
    }

    @Contract("_, _ -> this")
    public HeadlessClient beforeAction(String actionName, ActionHook hook) {
        addHook(beforeActionHooks, actionName, hook);
        return this;
    }

    @Contract("_, _ -> this")
    public HeadlessClient afterAction(String actionName, ActionHook hook) {
        addHook(afterActionHooks, actionName, hook);
        return this;
    }

    @Contract("_, _ -> this")
    public HeadlessClient insteadAction(String actionName, ActionHook hook) {
        addHook(insteadActionHooks, actionName, hook);
        return this;
    }

    @Contract("_ -> this")
    public HeadlessClient beforePacket(PacketHook hook) {
        beforePacketHooks.add(hook);
        return this;
    }

    public int protocolVersion() {
        return helloPacket().getInt(Protocol.FIELD_VERSION);
    }

    public String protocolName() {
        return helloPacket().getString(Protocol.FIELD_PROTOCOL);
    }

    public String serverId() {
        return helloPacket().getString(Protocol.FIELD_SERVER_ID);
    }

    @Override
    public void close() throws IOException {
        if (socket != null) {
            socket.close();
        }
        state = ClientState.DISCONNECTED;
    }

    private void parseHandshake(@NotNull JSONObject packet) {
        requireState(ClientState.CONNECTED);
        if (!Protocol.NAME.equals(packet.optString(Protocol.FIELD_PROTOCOL, ""))) {
            throw new IllegalStateException("Unexpected protocol: "
                    + packet.optString(Protocol.FIELD_PROTOCOL, ""));
        }
        int protocolVersion = packet.optInt(Protocol.FIELD_VERSION, -1);
        if (protocolVersion < Protocol.MIN_VERSION || protocolVersion > Protocol.VERSION) {
            throw new IllegalStateException("Unsupported protocol version: " + protocolVersion);
        }
        if (packet.optString(Protocol.FIELD_SERVER_ID, "").isEmpty()) {
            throw new IllegalStateException("Missing server_id");
        }
        helloPacket = packet;
        state = ClientState.HELLO_RECEIVED;
    }

    @EnsuresNonNull({"reader","writer"})
    @Contract(pure = true)
    private void requireConnected() {
        if (state == ClientState.NEW || state == ClientState.DISCONNECTED || state == ClientState.FAILED) {
            throw new IllegalStateException("Client is not connected: " + state);
        }
        Objects.requireNonNull(reader, "Reader must be present");
        Objects.requireNonNull(writer, "Writer must be present");
    }

    private static @NotNull List<@NotNull Integer> intList(@NotNull JSONArray array) {
        ArrayList<@NotNull Integer> result = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            result.add(array.getInt(i));
        }
        return result;
    }

    @Contract(pure = true)
    private void requireState(ClientState expected) {
        if (state != expected) {
            throw new IllegalStateException("Expected state " + expected + ", got " + state);
        }
    }

    private void addHook(@NotNull Map<String, List<ActionHook>> hooks, @NotNull String actionName, @NotNull ActionHook hook) {
        hooks.computeIfAbsent(actionName, ignored -> new ArrayList<>()).add(hook);
    }

    private boolean runHooks(@NotNull Map<String, List<ActionHook>> hooks, @NotNull String actionName, JSONObject action) {
        boolean ran = false;
        List<ActionHook> exactHooks = hooks.get(actionName);
        if (exactHooks != null) {
            for (ActionHook hook : exactHooks) {
                hook.handle(this, action);
                ran = true;
            }
        }
        List<ActionHook> wildcardHooks = hooks.get("*");
        if (wildcardHooks != null) {
            for (ActionHook hook : wildcardHooks) {
                hook.handle(this, action);
                ran = true;
            }
        }
        return ran;
    }

    public interface ActionHook {
        void handle(HeadlessClient client, JSONObject action);
    }

    public interface PacketHook {
        void handle(HeadlessClient client, JSONObject packet);
    }
}
