package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers;

import com.watabou.gltextures.TextureSource;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

public abstract class TextureSourceSerializer<T extends TextureSource> implements Serializer<T> {

    protected abstract String getType();
    protected abstract JSONObject serializeData(@NotNull T obj, @NotNull SerializationContext ctx, @NotNull String profile);

    @Override
    public final Object serialize(@NotNull T obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject json = serializeData(obj, ctx, profile);
        json.put("type", getType());
        return json;
    }

    public static class FileSerializer extends TextureSourceSerializer<TextureSource.File> {
        @Override
        protected String getType() {
            return "file";
        }

        @Override
        protected JSONObject serializeData(@NotNull TextureSource.File obj, @NotNull SerializationContext ctx, @NotNull String profile) {
            JSONObject json = new JSONObject();
            json.put("path", obj.path);
            return json;
        }
    }

    public static class SolidSerializer extends TextureSourceSerializer<TextureSource.Solid> {
        @Override
        protected String getType() {
            return "solid";
        }

        @Override
        protected JSONObject serializeData(@NotNull TextureSource.Solid obj, @NotNull SerializationContext ctx, @NotNull String profile) {
            JSONObject json = new JSONObject();
            json.put("color", obj.color);
            return json;
        }
    }

    public static class GradientSerializer extends TextureSourceSerializer<TextureSource.Gradient> {
        @Override
        protected String getType() {
            return "gradient";
        }

        @Override
        protected JSONObject serializeData(@NotNull TextureSource.Gradient obj, @NotNull SerializationContext ctx, @NotNull String profile) {
            JSONObject json = new JSONObject();
            JSONArray colors = new JSONArray();
            for (int color : obj.colors) {
                colors.put(color);
            }
            json.put("colors", colors);
            return json;
        }
    }
}
