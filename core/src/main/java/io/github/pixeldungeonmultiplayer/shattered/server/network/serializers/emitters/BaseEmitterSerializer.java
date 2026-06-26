package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.emitters;

import com.shatteredpixel.shatteredpixeldungeon.particles.Emitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.json.JSONObject;

public abstract class BaseEmitterSerializer {

	public static JSONObject baseObject(String actionType, Emitter emitter, SerializationContext ctx) {
		JSONObject object = new JSONObject();
		object.put("action_name", actionType);
		object.put("anchor", ctx.serialize(emitter.anchor()));
		Object factory = ctx.serialize(emitter.networkFactory());
		if (factory == null || factory == JSONObject.NULL) {
			return null;
		}
		object.put("factory", factory);
		object.put("interval", emitter.networkInterval());
		object.put("quantity", emitter.networkQuantity());
		object.put("fill_target", emitter.networkFillTarget());
		if (emitter instanceof BlobEmitter) {
			object.put("bound", ctx.serialize(((BlobEmitter) emitter).bound));
		}
		return object;
	}
}
