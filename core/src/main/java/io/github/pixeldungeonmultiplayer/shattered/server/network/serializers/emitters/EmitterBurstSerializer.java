package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.emitters;

import com.shatteredpixel.shatteredpixeldungeon.particles.Emitter;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.Serializer;
import org.jetbrains.annotations.NotNull;

public class EmitterBurstSerializer extends BaseEmitterSerializer implements Serializer<Emitter> {

	@Override
	public Object serialize(@NotNull Emitter emitter, @NotNull SerializationContext ctx, @NotNull String profile) {
		return baseObject("emitter_burst", emitter, ctx);
	}
}
