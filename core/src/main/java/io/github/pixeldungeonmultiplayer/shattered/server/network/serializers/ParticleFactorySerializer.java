package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.ParticleFactoryRegistry;
import com.shatteredpixel.shatteredpixeldungeon.particles.Emitter;
import com.watabou.noosa.particles.SerializableParticleFactory;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class ParticleFactorySerializer implements Serializer<SerializableParticleFactory> {

    @Override
    public Object serialize(@NotNull SerializableParticleFactory factory, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject object = baseObject(factory);
        return object;
    }

    public static JSONObject baseObject(SerializableParticleFactory factory) {
        String name = ParticleFactoryRegistry.resolve(factory);
        if (name == null) {
            return null;
        }
        JSONObject object = new JSONObject();
        object.put("path", factory.getClass().getName());
        object.put("factory_type", name);
        object.put("light_mode", lightMode(factory));
        return object;
    }

    private static boolean lightMode(SerializableParticleFactory factory) {
        if (factory instanceof Emitter.Factory) {
            return ((Emitter.Factory) factory).lightMode();
        }
        if (factory instanceof Emitter.Factory) {
            return ((Emitter.Factory) factory).lightMode();
        }
        return false;
    }
}
