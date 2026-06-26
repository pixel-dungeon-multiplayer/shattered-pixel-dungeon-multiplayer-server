package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.heropack;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.WindowSerializer;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndHeroInfo;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.dtos.TalentState;
import com.watabou.utils.DeviceCompat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class WndHeroInfoSerializer extends WindowSerializer<WndHeroInfo> {

    @Override
    protected @NotNull String type() {
        return "hero_info";
    }

    @Override
    protected @Nullable JSONObject args(@NotNull WndHeroInfo obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject args = new JSONObject();
        HeroClass heroClass = obj.heroClass();
        args.put("selected_tab", obj.selectedTabIndex());
        args.put("hero_class", ctx.serialize(heroClass, "default"));

        // Tab availability based on badges/achievements
        args.put("subclass_unlocked", Badges.isUnlocked(Badges.Badge.BOSS_SLAIN_2) || DeviceCompat.isDebug());
        args.put("ability_unlocked", Badges.isUnlocked(Badges.Badge.BOSS_SLAIN_4) || DeviceCompat.isDebug());

        // Talents
        JSONArray tiers = new JSONArray();
        ArrayList<LinkedHashMap<Talent, Integer>> classTalents = new ArrayList<>();
        Talent.initClassTalents(heroClass, classTalents);
        classTalents.get(2).clear(); // remove T3 talents, as in original

        for (int i = 0; i < classTalents.size(); i++) {
            JSONObject tierObj = new JSONObject();
            tierObj.put("tier", i + 1);

            JSONArray talents = new JSONArray();
            LinkedHashMap<Talent, Integer> tierTalents = classTalents.get(i);
            for (Talent talent : tierTalents.keySet()) {
                talents.put(ctx.serialize(new TalentState(talent, obj.getOwnerHero().pointsInTalent(talent), obj.getOwnerHero()), profile));
            }
            tierObj.put("talents", talents);
            tiers.put(tierObj);
        }
        args.put("talent_tiers", tiers);

        return args;
    }
}
