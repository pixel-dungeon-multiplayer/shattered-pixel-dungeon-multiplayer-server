package io.github.pixeldungeonmultiplayer.shattered.server.network;

import com.badlogic.gdx.Preferences;
import com.watabou.utils.GameSettings;
import io.github.pixeldungeonmultiplayer.shattered.headlessclient.ClientState;
import io.github.pixeldungeonmultiplayer.shattered.headlessclient.InMemorySocketPair;
import io.github.pixeldungeonmultiplayer.shattered.headlessclient.HeadlessClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class QueryClientThreadTest {

    private static final String SERVER_UUID = "test-server-id";

    @BeforeEach
    void setUp() {
        InMemoryPreferences preferences = new InMemoryPreferences();
        preferences.putString("server_uuid", SERVER_UUID);
        GameSettings.set(preferences);
    }

    @Test
    void sendsHelloPacketAfterConnection() throws Exception {
        InMemorySocketPair sockets = InMemorySocketPair.create();
        QueryClientThread server = new QueryClientThread(sockets.server());
        server.setDaemon(true);
        server.start();

        HeadlessClient client = new HeadlessClient()
                .connect(sockets.client());
        client.parseNext();

        assertEquals(ClientState.HELLO_RECEIVED, client.state());
        assertEquals(Protocol.PACKET_HANDSHAKE, client.helloPacket().getString(Protocol.FIELD_PACKET_TYPE));
        assertEquals(Protocol.NAME, client.protocolName());
        assertEquals(Protocol.VERSION, client.protocolVersion());
        assertEquals(SERVER_UUID, client.serverId());

        client.close();
        server.join(1000);
        assertFalse(server.isAlive());
    }

    @Test
    void closesConnectionAfterMalformedJson() throws Exception {
        InMemorySocketPair sockets = InMemorySocketPair.create();
        QueryClientThread server = new QueryClientThread(sockets.server());
        server.setDaemon(true);
        server.start();

        Charset charset = Charset.forName(ClientThread.CHARSET);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                sockets.client().getInputStream(), charset.newDecoder()
        ));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                sockets.client().getOutputStream(), charset.newEncoder()
        ));
        reader.readLine();

        writer.write("not-json\n");
        writer.flush();

        assertNull(reader.readLine());
        server.join(1000);
        assertFalse(server.isAlive());
        sockets.client().close();
    }

    private static final class InMemoryPreferences implements Preferences {
        private final Map<String, Object> values = new HashMap<>();

        @Override
        public Preferences putBoolean(String key, boolean val) {
            values.put(key, val);
            return this;
        }

        @Override
        public Preferences putInteger(String key, int val) {
            values.put(key, val);
            return this;
        }

        @Override
        public Preferences putLong(String key, long val) {
            values.put(key, val);
            return this;
        }

        @Override
        public Preferences putFloat(String key, float val) {
            values.put(key, val);
            return this;
        }

        @Override
        public Preferences putString(String key, String val) {
            values.put(key, val);
            return this;
        }

        @Override
        public Preferences put(Map<String, ?> vals) {
            values.putAll(vals);
            return this;
        }

        @Override
        public boolean getBoolean(String key) {
            return getBoolean(key, false);
        }

        @Override
        public int getInteger(String key) {
            return getInteger(key, 0);
        }

        @Override
        public long getLong(String key) {
            return getLong(key, 0L);
        }

        @Override
        public float getFloat(String key) {
            return getFloat(key, 0f);
        }

        @Override
        public String getString(String key) {
            return getString(key, "");
        }

        @Override
        public boolean getBoolean(String key, boolean defValue) {
            Object value = values.get(key);
            return value instanceof Boolean ? (Boolean) value : defValue;
        }

        @Override
        public int getInteger(String key, int defValue) {
            Object value = values.get(key);
            return value instanceof Integer ? (Integer) value : defValue;
        }

        @Override
        public long getLong(String key, long defValue) {
            Object value = values.get(key);
            return value instanceof Long ? (Long) value : defValue;
        }

        @Override
        public float getFloat(String key, float defValue) {
            Object value = values.get(key);
            return value instanceof Float ? (Float) value : defValue;
        }

        @Override
        public String getString(String key, String defValue) {
            Object value = values.get(key);
            return value instanceof String ? (String) value : defValue;
        }

        @Override
        public Map<String, ?> get() {
            return new HashMap<>(values);
        }

        @Override
        public boolean contains(String key) {
            return values.containsKey(key);
        }

        @Override
        public void clear() {
            values.clear();
        }

        @Override
        public void remove(String key) {
            values.remove(key);
        }

        @Override
        public void flush() {
        }
    }
}
