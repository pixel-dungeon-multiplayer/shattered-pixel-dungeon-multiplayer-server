package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.dtos.TalentState;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class HeroSubClassSerializer implements Serializer<HeroSubClass> {

    @Override
    public @NotNull JSONObject serialize(@NotNull HeroSubClass obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject json = new JSONObject();
        json.put("id", obj.name());
        json.put("title", ctx.serialize(obj.title(), "default"));
        json.put("short_description", ctx.serialize(obj.shortDesc(), "default"));
        json.put("description", ctx.serialize(obj.desc(), "default"));
        json.put("icon", obj.icon());

        ArrayList<LinkedHashMap<Talent, Integer>> subclassTalents = new ArrayList<>();
        Talent.initSubclassTalents(obj, subclassTalents);
        JSONArray talents = new JSONArray();
        if (subclassTalents.size() > 2) {
            for (Talent talent : subclassTalents.get(2).keySet()) {
                talents.put(ctx.serialize(new TalentState(talent, 0), "default"));
            }
        }
        json.put("talents", talents);

        return json;
    }
}
