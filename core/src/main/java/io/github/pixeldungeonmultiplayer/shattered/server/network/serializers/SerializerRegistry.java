package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers;

import java.util.HashMap;
import java.util.Map;

public class SerializerRegistry {
    private final Map<Class<?>, Map<String, Serializer<?>>> serializers = new HashMap<>();

    public <T> void register(Class<T> clazz, Serializer<T> serializer) {
        register(clazz, "default", serializer);
    }

    public <T> void register(Class<T> clazz, String profile, Serializer<T> serializer) {
        serializers.computeIfAbsent(clazz, k -> new HashMap<>()).put(profile, serializer);
    }

    public Serializer<?> get(Class<?> clazz, String profile) {
        Class<?> current = clazz;
        while (current != null) {
            Map<String, Serializer<?>> profileMap = serializers.get(current);
            if (profileMap != null) {
                // 1. Check for precise profile on this specific class
                if (profileMap.containsKey(profile)) {
                    return profileMap.get(profile);
                }
                // 2. Fallback to default profile on THIS SPECIFIC class before looking at parents
                if (!"default".equals(profile) && profileMap.containsKey("default")) {
                    return profileMap.get("default");
                }
            }

            for (Class<?> iface : current.getInterfaces()) {
                Serializer<?> ifaceSerializer = getFromInterface(iface, profile);
                if (ifaceSerializer != null) {
                    return ifaceSerializer;
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }

    private Serializer<?> getFromInterface(Class<?> iface, String profile) {
        Map<String, Serializer<?>> profileMap = serializers.get(iface);
        if (profileMap != null) {
            if (profileMap.containsKey(profile)) {
                return profileMap.get(profile);
            }
            if (!"default".equals(profile) && profileMap.containsKey("default")) {
                return profileMap.get("default");
            }
        }

        for (Class<?> superIface : iface.getInterfaces()) {
            Serializer<?> superIfaceSerializer = getFromInterface(superIface, profile);
            if (superIfaceSerializer != null) {
                return superIfaceSerializer;
            }
        }
        return null;
    }
}
