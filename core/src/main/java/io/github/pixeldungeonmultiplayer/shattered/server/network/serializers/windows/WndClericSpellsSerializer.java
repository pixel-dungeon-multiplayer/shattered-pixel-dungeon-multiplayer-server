package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.ClericSpell;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.ui.ImageIcon;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndClericSpells;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

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

        JSONArray buttons = new JSONArray();
        Hero cleric = obj.getOwnerHero();
        for (WndClericSpells.SpellButton button : obj.spellButtons) {
            JSONObject btnObj = new JSONObject();
            btnObj.put("info", obj.infoMode());
            btnObj.put("spell_name", ctx.serialize(button.spell.name(), profile));
            btnObj.put("spell_short_desc", ctx.serialize(button.spell.shortDesc(cleric), profile));
            btnObj.put("spell_desc", ctx.serialize(button.spell.desc(cleric), profile));
            btnObj.put("alpha", obj.tome().canCast(cleric, button.spell) ? 1.0 : 0.3);
            btnObj.put("icon", button.spell.icon());
            btnObj.put("hover_text", ctx.serialize(button.hoverText(), profile));

            int tier = 1;
            for (int i = 1; i <= Talent.MAX_TALENT_TIERS; i++) {
                if (ClericSpell.getSpellList(cleric, i).contains(button.spell)) {
                    tier = i;
                    break;
                }
            }
            btnObj.put("tier", tier);
            buttons.put(btnObj);
        }
        args.put("buttons", buttons);

        return args;
    }
}
