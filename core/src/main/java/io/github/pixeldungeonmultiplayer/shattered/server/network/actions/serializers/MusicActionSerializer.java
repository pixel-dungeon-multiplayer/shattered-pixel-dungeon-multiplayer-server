package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.MusicAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class MusicActionSerializer extends NetworkActionSerializer<MusicAction> {

    @Override
    protected @Nullable JSONObject serializeInternal(@NotNull MusicAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject object = new JSONObject();
        if(obj instanceof MusicAction.PlayAction){
            MusicAction.PlayAction action = (MusicAction.PlayAction) obj;
            object.put("asset", action.assetName);
            object.put("looping", action.looping);

        }
        if(obj instanceof MusicAction.PlayTracksAction){
            MusicAction.PlayTracksAction action = (MusicAction.PlayTracksAction) obj;
            object.put("chances", action.chances);
            object.put("tracks", action.tracks);
            object.put("shuffle", action.shuffle);
        }
        if(obj instanceof MusicAction.FadeOutAction){
            MusicAction.FadeOutAction action = (MusicAction.FadeOutAction) obj;
            if (action.callback != null) {
                object.put("callback", ctx.serialize(action.callback));
            }
            object.put("duration", action.duration);
        }
        //EndAction has no extra fields needed for serialization
        return object;
    }
}
