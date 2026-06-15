package com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows;

import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndChooseSubclass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class WndChooseSubclassSerializer extends WindowSerializer<WndChooseSubclass> {

    @Override
    protected @NotNull String type() {
        return "choose_subclass";
    }

    @Override
    protected @Nullable JSONObject args(@NotNull WndChooseSubclass obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject args = new JSONObject();
        args.put("option1", obj.getOwnerHero().heroClass.subClasses()[0].name());
        args.put("option2", obj.getOwnerHero().heroClass.subClasses()[1].name());
        return args;
    }
}
