package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.CustomTilemapActions;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import static io.github.pixeldungeonmultiplayer.shattered.server.network.actions.CustomTilemapActions.findIndex;

public final class CustomTilemapActionSerializers {

    @Contract(value = "-> fail", pure = true)
    private CustomTilemapActionSerializers() {
        throw new RuntimeException();
    }

    public static class Add extends NetworkActionSerializer<CustomTilemapActions.Add> {
        @Override
        protected @Nullable JSONObject serializeInternal(@NotNull CustomTilemapActions.Add obj, @NotNull SerializationContext ctx, @NotNull String profile) {
            JSONObject object = new JSONObject();
            object.put("isWall", obj.isWall);
            object.put("index", obj.index);
            object.put("tilemap", ctx.serialize(obj.tilemap, profile));
            return object;
        }
    }

    public static class Remove extends NetworkActionSerializer<CustomTilemapActions.Remove> {
        @Override
        protected @Nullable JSONObject serializeInternal(@NotNull CustomTilemapActions.Remove obj, @NotNull SerializationContext ctx, @NotNull String profile) {
            if (obj.index < 0) {
                return null;
            }
            JSONObject object = new JSONObject();
            object.put("isWall", obj.isWall);
            object.put("index", obj.index);
            return object;
        }
    }

    public static class Update extends NetworkActionSerializer<CustomTilemapActions.Update> {
        @Override
        protected @Nullable JSONObject serializeInternal(@NotNull CustomTilemapActions.Update obj, @NotNull SerializationContext ctx, @NotNull String profile) {
            CustomTilemapActions.TilemapPos pos = findIndex(obj.tilemap);
            if (pos.index < 0) {
                return null;
            }
            JSONObject object = new JSONObject();
            object.put("isWall", pos.isWall);
            object.put("index", pos.index);
            object.put("tilemap", ctx.serialize(obj.tilemap, profile));
            return object;
        }
    }

    public static class PrisonTrapFade extends NetworkActionSerializer<CustomTilemapActions.Special.PrisonTrapFade> {
        @Override
        protected @Nullable JSONObject serializeInternal(@NotNull CustomTilemapActions.Special.PrisonTrapFade obj, @NotNull SerializationContext ctx, @NotNull String profile) {
            CustomTilemapActions.TilemapPos pos = findIndex(obj.tilemap);
            if (pos.index < 0) {
                return null;
            }
            JSONObject object = new JSONObject();
            object.put("isWall", pos.isWall);
            object.put("index", pos.index);
            object.put("custom_action", obj.CustomActionName());
            return object;
        }
    }
}
