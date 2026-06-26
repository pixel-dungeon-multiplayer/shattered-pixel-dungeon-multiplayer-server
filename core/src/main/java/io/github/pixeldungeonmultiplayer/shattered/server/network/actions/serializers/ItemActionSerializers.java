package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.ItemAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

public final class ItemActionSerializers {

    @Contract(value = "-> fail", pure = true)
    private ItemActionSerializers() {
        throw new RuntimeException();
    }

    private static JSONArray serializePath(@NotNull java.util.List<Integer> path) {
        JSONArray pathArr = new JSONArray();
        for (int p : path) pathArr.put(p);
        return pathArr;
    }

    public static class Add extends NetworkActionSerializer<ItemAction.Add> {
        @Override
        protected @Nullable JSONObject serializeInternal(@NotNull ItemAction.Add obj, @NotNull SerializationContext ctx, @NotNull String profile) {
            Hero hero = (Hero) ctx.observer;
            if (hero == null) {
                return null;
            }
            java.util.List<Integer> path = hero.belongings.pathOfItem(obj.item);
            if (path == null || path.isEmpty()) {
                return null;
            }
            JSONObject object = new JSONObject();
            object.put("path", serializePath(path));
            object.put("item", ctx.serialize(obj.item, "inventory"));
            return object;
        }
    }

    public static class Remove extends NetworkActionSerializer<ItemAction.Remove> {
        @Override
        protected @Nullable JSONObject serializeInternal(@NotNull ItemAction.Remove obj, @NotNull SerializationContext ctx, @NotNull String profile) {
            JSONObject object = new JSONObject();
            object.put("path", serializePath(obj.path));
            return object;
        }
    }

    public static class Update extends NetworkActionSerializer<ItemAction.Update> {
        @Override
        protected @Nullable JSONObject serializeInternal(@NotNull ItemAction.Update obj, @NotNull SerializationContext ctx, @NotNull String profile) {
            Hero hero = (Hero) ctx.observer;
            if (hero == null) {
                return null;
            }
            java.util.List<Integer> path = hero.belongings.pathOfItem(obj.item);
            if (path == null || path.isEmpty()) {
                return null;
            }
            JSONObject object = new JSONObject();
            object.put("path", serializePath(path));
            object.put("item", ctx.serialize(obj.item, "inventory"));
            return object;
        }
    }

    public static class Replace extends NetworkActionSerializer<ItemAction.Replace> {
        @Override
        protected @Nullable JSONObject serializeInternal(@NotNull ItemAction.Replace obj, @NotNull SerializationContext ctx, @NotNull String profile) {
            Hero hero = (Hero) ctx.observer;
            if (hero == null) {
                return null;
            }
            java.util.List<Integer> path = hero.belongings.pathOfItem(obj.item);
            if (path == null || path.isEmpty()) {
                return null;
            }
            JSONObject object = new JSONObject();
            object.put("path", serializePath(path));
            object.put("item", ctx.serialize(obj.item, "inventory"));
            return object;
        }
    }
}
