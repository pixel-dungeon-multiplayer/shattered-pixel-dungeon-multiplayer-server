package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.utils.Utils;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.SampleAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

public final class SampleActionSerializers {

    @Contract(value = "-> fail", pure = true)
    private SampleActionSerializers() {throw new RuntimeException();}

    public static class Play extends NetworkActionSerializer<SampleAction.PlayAction> {
        @Override
        protected @Nullable JSONObject serializeInternal(@NotNull SampleAction.PlayAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
            JSONObject object = new JSONObject();
            object.put("sample", obj.id);
            object.put("left_volume", obj.leftVolume);
            object.put("right_volume", obj.rightVolume);
            object.put("rate", obj.rate);
            if (obj.delay != null) {
                object.put("delay", (float) obj.delay);
            }
            object.put("pitch", obj.pitch);
            return object;
        }
    }

    public static class Load extends NetworkActionSerializer<SampleAction.LoadAction> {
        @Override
        protected @Nullable JSONObject serializeInternal(@NotNull SampleAction.LoadAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
            JSONObject object = new JSONObject();
            try {
                object.put("samples", Utils.putToJSONArray(obj.samples));
            } catch (JSONException ignored) {}
            return object;
        }
    }

    public static class Unload extends NetworkActionSerializer<SampleAction.UnloadAction> {
        @Override
        protected @Nullable JSONObject serializeInternal(@NotNull SampleAction.UnloadAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
            JSONObject object = new JSONObject();
            object.put("sample", obj.sample);
            return object;
        }
    }

    public static class Reload extends NetworkActionSerializer<SampleAction.ReloadAction> {
        @Override
        protected @Nullable JSONObject serializeInternal(@NotNull SampleAction.ReloadAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
            JSONObject object = new JSONObject();
            try {
                object.put("samples", Utils.putToJSONArray(obj.samples));
            } catch (JSONException ignored) {}
            return object;
        }
    }
}
