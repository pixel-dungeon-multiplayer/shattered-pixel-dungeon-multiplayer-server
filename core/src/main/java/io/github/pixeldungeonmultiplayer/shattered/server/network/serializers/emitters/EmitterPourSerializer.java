package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.emitters;

import com.shatteredpixel.shatteredpixeldungeon.particles.Emitter;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.Serializer;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class EmitterPourSerializer extends BaseEmitterSerializer implements Serializer<Emitter> {

	@Override
	public Object serialize(@NotNull Emitter emitter, @NotNull SerializationContext ctx, @NotNull String profile) {
		JSONObject object = baseObject("emitter_pour", emitter, ctx);
		if (object != null) {
			object.put("id", emitter.networkId());
		}
		return object;
	}
}
