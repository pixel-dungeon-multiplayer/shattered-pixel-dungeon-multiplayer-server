package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

public class WndMetamorphReplaceSerializer extends WindowSerializer<ScrollOfMetamorphosis.WndMetamorphReplace> {

    @Override
    protected @NotNull String type() {
        return "metamorph_replace";
    }

    @Override
    protected @Nullable JSONObject args(@NotNull ScrollOfMetamorphosis.WndMetamorphReplace obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject args = new JSONObject();
        args.put("message", ctx.serialize(Messages.get(ScrollOfMetamorphosis.class, "replace_desc"), profile));
        args.put("replacing", obj.replacing.name());
        args.put("tier", obj.tier);
        JSONArray options = new JSONArray();
        for (Talent talent : obj.replaceOptions().keySet()) {
            JSONObject option = new JSONObject();
            option.put("id", talent.name());
            option.put("title", ctx.serialize(talent.title(), profile));
            option.put("points", obj.replaceOptions().get(talent));
            options.put(option);
        }
        args.put("options", options);
        return args;
    }
}



