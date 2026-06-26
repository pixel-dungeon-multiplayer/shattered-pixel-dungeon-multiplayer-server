package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows;

import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class WndTextInputSerializer extends WindowSerializer<WndTextInput> {

    @Override
    protected @NotNull String type() {
        return "text_input";
    }

    @Override
    protected @Nullable JSONObject args(@NotNull WndTextInput obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject args = new JSONObject();
        args.put("title", obj.title() == null ? JSONObject.NULL : ctx.serialize(obj.title(), profile));
        args.put("message", obj.body() == null ? JSONObject.NULL : ctx.serialize(obj.body(), profile));
        args.put("initial_value", obj.initialValue() == null ? "" : obj.initialValue().toString());
        args.put("max_length", obj.maxLength());
        args.put("is_multi_line", obj.multiLine());
        args.put("positive_text", ctx.serialize(obj.positiveText(), profile));
        args.put("negative_text", obj.negativeText() == null ? JSONObject.NULL : ctx.serialize(obj.negativeText(), profile));
        return args;
    }
}



