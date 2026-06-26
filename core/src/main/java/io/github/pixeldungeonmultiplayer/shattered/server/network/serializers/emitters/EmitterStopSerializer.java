package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.emitters;

import com.shatteredpixel.shatteredpixeldungeon.particles.Emitter;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.Serializer;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class EmitterStopSerializer implements Serializer<Emitter> {

	@Override
	public Object serialize(@NotNull Emitter emitter, @NotNull SerializationContext ctx, @NotNull String profile) {
		JSONObject object = new JSONObject();
		object.put("action_name", "emitter_stop");
		object.put("id", emitter.networkId());
		return object;
	}
}
