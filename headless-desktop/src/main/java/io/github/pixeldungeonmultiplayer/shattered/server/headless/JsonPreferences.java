package io.github.pixeldungeonmultiplayer.shattered.server.headless;

import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** A human-editable JSON implementation of libGDX preferences. */
final class JsonPreferences implements Preferences {

    private final FileHandle file;
    private final Map<String, Object> values = new LinkedHashMap<>();

    JsonPreferences(FileHandle file) {
        this.file = file;
        if (file.exists()) {
            try {
                JSONObject json = new JSONObject(file.readString("UTF-8"));
                for (String key : json.keySet()) {
                    Object value = json.get(key);
                    if (value instanceof Boolean || value instanceof Number || value instanceof String) {
                        values.put(key, value);
                    } else {
                        throw new IllegalArgumentException("Unsupported value for key " + key);
                    }
                }
            } catch (Exception e) {
                throw new GdxRuntimeException("Could not read headless server config: " + file.file().getAbsolutePath(), e);
            }
        }
    }

    @Override public synchronized Preferences putBoolean(String key, boolean value) { values.put(key, value); return this; }
    @Override public synchronized Preferences putInteger(String key, int value) { values.put(key, value); return this; }
    @Override public synchronized Preferences putLong(String key, long value) { values.put(key, value); return this; }
    @Override public synchronized Preferences putFloat(String key, float value) { values.put(key, value); return this; }
    @Override public synchronized Preferences putString(String key, String value) { values.put(key, value); return this; }

    @Override
    public synchronized Preferences put(Map<String, ?> entries) {
        for (Map.Entry<String, ?> entry : entries.entrySet()) {
            Object value = entry.getValue();
            if (!(value instanceof Boolean || value instanceof Number || value instanceof String)) {
                throw new IllegalArgumentException("Unsupported value for key " + entry.getKey());
            }
            values.put(entry.getKey(), value);
        }
        return this;
    }

    @Override public synchronized boolean getBoolean(String key) { return getBoolean(key, false); }
    @Override public synchronized int getInteger(String key) { return getInteger(key, 0); }
    @Override public synchronized long getLong(String key) { return getLong(key, 0L); }
    @Override public synchronized float getFloat(String key) { return getFloat(key, 0f); }
    @Override public synchronized String getString(String key) { return getString(key, ""); }

    @Override public synchronized boolean getBoolean(String key, boolean defValue) {
        Object value = values.get(key);
        return value instanceof Boolean ? (Boolean) value : defValue;
    }

    @Override public synchronized int getInteger(String key, int defValue) {
        Object value = values.get(key);
        return value instanceof Number ? ((Number) value).intValue() : defValue;
    }

    @Override public synchronized long getLong(String key, long defValue) {
        Object value = values.get(key);
        return value instanceof Number ? ((Number) value).longValue() : defValue;
    }

    @Override public synchronized float getFloat(String key, float defValue) {
        Object value = values.get(key);
        return value instanceof Number ? ((Number) value).floatValue() : defValue;
    }

    @Override public synchronized String getString(String key, String defValue) {
        Object value = values.get(key);
        return value instanceof String ? (String) value : defValue;
    }

    @Override
    public synchronized Map<String, ?> get() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }

    @Override public synchronized boolean contains(String key) { return values.containsKey(key); }
    @Override public synchronized void clear() { values.clear(); }
    @Override public synchronized void remove(String key) { values.remove(key); }

    @Override
    public synchronized void flush() {
        try {
            if (file.parent() != null) file.parent().mkdirs();
            file.writeString(new JSONObject(values).toString(2) + System.lineSeparator(), false, "UTF-8");
        } catch (Exception e) {
            throw new GdxRuntimeException("Could not write headless server config: " + file.file().getAbsolutePath(), e);
        }
    }
}
