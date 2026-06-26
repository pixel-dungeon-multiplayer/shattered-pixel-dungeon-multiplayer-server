package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class ArmorAbilitySerializer implements Serializer<ArmorAbility> {

    @Override
    public @NotNull JSONObject serialize(@NotNull ArmorAbility obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject json = new JSONObject();
        json.put("id", obj.getClass().getName());
        json.put("name", ctx.serialize(obj.name(), "default"));
        json.put("short_description", ctx.serialize(obj.shortDesc(), "default"));
        json.put("description", ctx.serialize(obj.desc(), "default"));
        json.put("icon", obj.icon());
        return json;
    }
}
