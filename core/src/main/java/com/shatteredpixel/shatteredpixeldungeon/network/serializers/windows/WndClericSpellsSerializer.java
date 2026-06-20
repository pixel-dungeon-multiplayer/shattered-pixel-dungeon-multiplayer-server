package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.ClericSpell;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.ui.ImageIcon;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndClericSpells;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class WndClericSpellsSerializer extends WindowSerializer<WndClericSpells> {

    @Override
    protected @NotNull String type() {
        return "cleric_spells";
    }

    @Override
    protected @Nullable JSONObject args(@NotNull WndClericSpells obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject args = new JSONObject();
        args.put("info", obj.infoMode());
        args.put("description", ctx.serialize(obj.desc(), profile));

        JSONObject title = new JSONObject();
        {
            title.put("text", ctx.serialize(obj.title.text, profile));
            title.put("color", obj.title.color == null ? JSONObject.NULL : obj.title.color);
            title.put("title_icon", ImageIcon.fromImage(obj.title.imIcon, ctx, profile).toJson());
        }
        args.put("title", title);

        JSONArray spellTiers = new JSONArray();
        Hero cleric = obj.getOwnerHero();
        for (int i = 1; i <= Talent.MAX_TALENT_TIERS; i++) {
            ArrayList<ClericSpell> spells = ClericSpell.getSpellList(cleric, i);
            if (spells.isEmpty()) {
                continue;
            }

            JSONObject tierObj = new JSONObject();
            tierObj.put("tier", i);

            JSONArray spellsArray = new JSONArray();
            for (ClericSpell spell : spells) {
                JSONObject spellObj = new JSONObject();
                spellObj.put("id", spell.name());
                spellObj.put("spell_id", ClericSpell.getSpellID(spell));
                spellObj.put("name", ctx.serialize(spell.name(), profile));
                spellObj.put("short_desc", ctx.serialize(spell.shortDesc(cleric), profile));
                spellObj.put("desc", ctx.serialize(spell.desc(cleric), profile));
                spellObj.put("alpha", obj.tome().canCast(cleric, spell) ? 1.0 : 0.3);
                spellObj.put("icon", spell.icon());
                spellsArray.put(spellObj);
            }
            tierObj.put("spells", spellsArray);
            spellTiers.put(tierObj);
        }
        args.put("spell_tiers", spellTiers);

        return args;
    }
}
