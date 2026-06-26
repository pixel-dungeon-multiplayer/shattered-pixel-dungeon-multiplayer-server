package io.github.pixeldungeonmultiplayer.shattered.server.network;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.ChatMessageAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.ImmutableNetworkAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.LiveStateNetworkAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NetworkPacket {

    private final List<LiveStateNetworkAction> actions;

    public NetworkPacket() {
        actions = new ArrayList<>();
    }

    public synchronized void clearData() {
        actions.clear();
    }

    @NotNull
    public static NetworkPacket fromChatMessages(@NotNull List<@NotNull ChatMessageAction> messages) {
        final NetworkPacket networkPacket = new NetworkPacket();
        for (ImmutableNetworkAction action : messages) {
            networkPacket.addAction(action);
        }
        return networkPacket;
    }

    @Deprecated
    public void addAction(@NotNull JSONObject actionObj) {
        addAction(serializedActionFrom(actionObj));
    }

    public void addAction(@NotNull ImmutableNetworkAction action) {
        addLateLiveStateAction(action);
    }

    public synchronized void addLateLiveStateAction(@NotNull LiveStateNetworkAction action) {
        Objects.requireNonNull(action);
        actions.add(action);
    }

    public synchronized void packAndAdd(@NotNull LiveStateNetworkAction action, @Nullable Hero observer) {
        JSONObject serialized = serializeAction(action, observer);
        if (serialized.length() > 0) {
            addAction(serializedActionFrom(serialized));
        }
    }

    public void packAndAdd(@NotNull ImmutableNetworkAction action) {
        addAction(action);
    }

    private static SerializedAction serializedActionFrom(@NotNull JSONObject actionObj) {
        if (!actionObj.has("action_name")) {
            throw new IllegalArgumentException("Serialized action must have action_name");
        }
        String actionName = actionObj.getString("action_name");
        actionObj.remove("action_name");
        return new SerializedAction(actionName, actionObj);
    }

    public synchronized JSONObject serialize(@Nullable Hero observer) {
        if (actions.isEmpty()) {
            return new JSONObject();
        }
        JSONObject packet = new JSONObject();
        packet.put(Protocol.FIELD_PACKET_TYPE, Protocol.PACKET_ACTIONS_BATCH);

        JSONArray actionsArr = new JSONArray();
        for (LiveStateNetworkAction action : actions) {
            JSONObject serialized = serializeAction(action, observer);
            if (serialized.length() > 0) {
                actionsArr.put(serialized);
            }
        }
        packet.put("actions", actionsArr);
        return packet;
    }

    private JSONObject serializeAction(@NotNull LiveStateNetworkAction action, @Nullable Hero observer) {
        SerializationContext ctx = new SerializationContext(Server.SERIALIZERS, observer);
        Object serialized = ctx.serialize(action);
        if (serialized instanceof JSONObject && ((JSONObject) serialized).length() > 0) {
            return (JSONObject) serialized;
        }
        return new JSONObject();
    }

    public synchronized void compress() {
        List<LiveStateNetworkAction> compressed = NetworkPacketCompressor.compress(actions);
        actions.clear();
        actions.addAll(compressed);
    }

    public static final class SerializedAction implements ImmutableNetworkAction {
        private final String actionName;
        private final JSONObject actionObj;

        @Contract(pure = true)
        private SerializedAction(@NotNull String actionName, @NotNull JSONObject actionObj) {
            Objects.requireNonNull(actionName);
            Objects.requireNonNull(actionObj);
            this.actionName = actionName;
            this.actionObj = actionObj;
        }

        @Override
        public @NotNull String actionName() {
            return actionName;
        }

        public JSONObject actionObj() {
            return actionObj;
        }
    }
}
