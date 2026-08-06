package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.ActionIndicatorAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.ui.ImageIcon;
import com.watabou.noosa.Image;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class ActionIndicatorActionSerializer extends NetworkActionSerializer<ActionIndicatorAction> {
	@Override
	protected JSONObject serializeInternal(@NotNull ActionIndicatorAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
		JSONObject json = new JSONObject();
		json.put("displayName", ctx.serialize(obj.displayName, profile));
		json.put("actionId", obj.actionId == null ? JSONObject.NULL : obj.actionId);
		json.put("indicatorColor", obj.indicatorColor);
		json.put("primaryVisual", serializeVisual(obj.primaryVisual, ctx, profile));
		json.put("secondaryVisual", serializeVisual(obj.secondaryVisual, ctx, profile));
		return json;
	}

	private Object serializeVisual(ActionIndicatorAction.VisualData visual, SerializationContext ctx, String profile) {
		if (visual == null) return JSONObject.NULL;
		JSONObject json;
		if (visual.kind == ActionIndicatorAction.VisualData.Kind.BITMAP_TEXT) {
			json = new JSONObject();
			json.put("type", "bitmap_text");
			json.put("text", visual.text);
		} else if (visual.kind == ActionIndicatorAction.VisualData.Kind.ITEM_SPRITE) {
			json = ImageIcon.itemSprite(visual.itemImage, visual.glowing).toJson();
		} else {
			json = ImageIcon.fromImage((Image) visual.visual, ctx, profile).toJson();
		}
		json.put("width", visual.width); json.put("height", visual.height);
		json.put("scaleX", visual.scaleX); json.put("scaleY", visual.scaleY);
		json.put("rm", visual.rm); json.put("gm", visual.gm); json.put("bm", visual.bm);
		json.put("ra", visual.ra); json.put("ga", visual.ga); json.put("ba", visual.ba);
		return json;
	}
}
