package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers;

import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.watabou.noosa.particles.SerializableParticleFactory;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class SplashFactorySerializer implements Serializer<Splash.SplashFactory> {

    @Override
    public Object serialize(Splash.@NotNull SplashFactory factory, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject object = (JSONObject) ctx.serializeAs(factory, SerializableParticleFactory.class, profile);
        if (object == null) {
            return null;
        }
        object.put("color", factory.color);
        object.put("dir", factory.dir);
        object.put("cone", factory.cone);
        return object;
    }
}
