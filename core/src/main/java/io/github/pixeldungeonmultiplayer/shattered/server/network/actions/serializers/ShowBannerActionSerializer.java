package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.ShowBannerAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.Locale;

public class ShowBannerActionSerializer extends NetworkActionSerializer<ShowBannerAction> {
    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull ShowBannerAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject actionObj = new JSONObject();
        actionObj.put("banner", obj.banner.toString().toLowerCase(Locale.ROOT));
        actionObj.put("color", obj.color);
        actionObj.put("fade_time", obj.fadeTime);
        actionObj.put("show_time", obj.showTime);
        return actionObj;
    }
}
