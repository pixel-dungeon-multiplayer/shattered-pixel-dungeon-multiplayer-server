package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows;

import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
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

        JSONArray buttons = new JSONArray();
        for (WndClericSpells.SpellButton button : obj.spellBtns) {
            JSONObject btnObj = new JSONObject();
            btnObj.put("info", button.info);
            btnObj.put("alpha", obj.tome().canCast(obj.getOwnerHero(), button.spell) ? 1.0 : 0.3);
            btnObj.put("tier", button.tier);
            btnObj.put("icon", button.spell.icon());
            btnObj.put("spell_id", button.spellID);
            btnObj.put("spell_short_desc", ctx.serialize(button.spell.shortDesc(obj.getOwnerHero()), profile));
            btnObj.put("spell_name", ctx.serialize(button.spell.name(), profile));
            buttons.put(btnObj);
        }
        args.put("buttons", buttons);
        return args;
    }
}
