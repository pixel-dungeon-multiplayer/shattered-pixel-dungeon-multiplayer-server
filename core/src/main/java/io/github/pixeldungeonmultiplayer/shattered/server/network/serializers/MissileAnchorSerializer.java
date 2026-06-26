package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class MissileAnchorSerializer implements Serializer<MissileSprite.Anchor> {

    @Override
    public Object serialize(@NotNull MissileSprite.Anchor anchor, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject object = new JSONObject();
        object.put("type", anchor.type);
        if (anchor.cell != null) {
            object.put("cell", anchor.cell);
        }
        if (anchor.charId != null) {
            object.put("char_id", anchor.charId);
        }
        return object;
    }
}
