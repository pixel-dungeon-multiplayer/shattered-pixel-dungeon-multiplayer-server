package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows;

import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndQuest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

public class WndQuestSerializer extends WindowSerializer<WndQuest> {

    @Override
    protected @NotNull String type() {
        return "wnd_option";
    }

    @Override
    protected @Nullable JSONObject args(@NotNull WndQuest obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject args = new JSONObject();

        JSONObject title = new JSONObject();
        title.put("text", ctx.serialize(obj.charName(), profile));
        title.put("color", JSONObject.NULL);
        args.put("title", title);

        args.put("message", ctx.serialize(obj.text(), profile));
        args.put("options", new JSONArray());

        JSONObject titleIcon = new JSONObject();
        JSONObject titleIconArgs = new JSONObject();
        titleIcon.put("type", "char_sprite");
        titleIconArgs.put("sprite_class", obj.spriteName());
        titleIcon.put("args", titleIconArgs);
        args.put("title_icon", titleIcon);

        JSONObject layout = new JSONObject();
        layout.put("expand_in_landscape", true);
        layout.put("highlighting", true);
        args.put("layout", layout);

        return args;
    }
}
