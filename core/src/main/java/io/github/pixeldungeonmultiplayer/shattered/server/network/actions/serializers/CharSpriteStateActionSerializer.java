package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.CharSpriteStateAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.Locale;

public class CharSpriteStateActionSerializer extends NetworkActionSerializer<CharSpriteStateAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull CharSpriteStateAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = new JSONObject();
        actionObj.put("actor_id", obj.actorId);
        actionObj.put("state", obj.state.name().toLowerCase(Locale.ROOT));
        return actionObj;
    }
}
