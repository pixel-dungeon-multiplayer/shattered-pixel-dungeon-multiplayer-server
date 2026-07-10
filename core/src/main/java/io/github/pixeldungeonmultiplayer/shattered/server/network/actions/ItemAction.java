package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ItemAction {

    @Contract(value = "-> fail", pure = true)
    private ItemAction() {
        throw new RuntimeException();
    }

    public static class Add implements LiveStateNetworkAction {
        public final @NotNull Item item;
        public final @NotNull List<Integer> path;

        @Contract(pure = true)
        public Add(@NotNull Item item, @NotNull List<Integer> path) {
            this.item = item;
            this.path = path;
        }

        @Override
        @Contract(pure = true)
        public @NotNull String actionName() {
            return "item_add";
        }
    }

    public static class Remove implements LiveStateNetworkAction {
        public final @NotNull List<Integer> path;

        @Contract(pure = true)
        public Remove(@NotNull List<Integer> path) {
            this.path = path;
        }

        @Override
        @Contract(pure = true)
        public @NotNull String actionName() {
            return "item_remove";
        }
    }

    public static class Update implements LiveStateNetworkAction {
        public final @NotNull Item item;

        @Contract(pure = true)
        public Update(@NotNull Item item) {
            this.item = item;
        }

        @Override
        @Contract(pure = true)
        public @NotNull String actionName() {
            return "item_update";
        }
    }

    public static class UpdateCount extends Update {
        @Contract(pure = true)
        public UpdateCount(@NotNull Item item, int count, @Nullable List<Integer> path) {
            super(item);
        }
    }

    public static class Replace implements LiveStateNetworkAction {
        public final @NotNull Item item;

        @Contract(pure = true)
        public Replace(@NotNull Item item) {
            this.item = item;
        }

        @Override
        @Contract(pure = true)
        public @NotNull String actionName() {
            return "item_replace";
        }
    }
}
