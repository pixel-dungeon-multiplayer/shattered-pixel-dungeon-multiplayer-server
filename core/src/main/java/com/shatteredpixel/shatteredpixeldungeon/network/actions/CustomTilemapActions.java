package com.shatteredpixel.shatteredpixeldungeon.network.actions;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CustomTilemapActions {

    // Adds new tilemap on "Index" position (like List.add)
    public static class Add implements LiveStateNetworkAction {
        public final boolean isWall;
        public final int index;
        public final CustomTilemap tilemap;

        @Contract(pure = true)
        public Add(boolean isWall, int index, CustomTilemap tilemap) {
            this.isWall = isWall;
            this.index = index;
            this.tilemap = tilemap;
            if (index < 0) throw new IllegalArgumentException("Index cannot be negative");
        }

        @Override
        public @NotNull String actionName() {
            return "custom_tilemap_add";
        }
    }

    public static class Remove implements LiveStateNetworkAction {
        public final boolean isWall;
        public final int index;

        @Contract(pure = true)
        public Remove(boolean isWall, int index) {
            this.isWall = isWall;
            this.index = index;
        }

        public Remove(CustomTilemap tilemap) {
            TilemapPos pos = findIndex(tilemap);
            this.isWall = pos.isWall;
            this.index = pos.index;
        }

        @Override
        public @NotNull String actionName() {
            return "custom_tilemap_remove";
        }
    }

    public static class Update implements LiveStateNetworkAction {
        public final CustomTilemap tilemap;

        @Contract(pure = true)
        public Update(CustomTilemap tilemap) {
            this.tilemap = tilemap;
        }

        @Override
        public @NotNull String actionName() {
            return "update_custom_tilemap";
        }
    }

    public static abstract class Special implements LiveStateNetworkAction {
        public final CustomTilemap tilemap;

        @Contract(pure = true)
        public Special(CustomTilemap tilemap) {
            this.tilemap = tilemap;
        }

        public abstract String CustomActionName();

        @Override
        public @NotNull String actionName() {
            return "custom_tilemap_special";
        }

        public static final class PrisonTrapFade extends Special {

            public PrisonTrapFade(CustomTilemap tilemap) {
                super(tilemap);
            }

            @Override
            @Contract(pure = true)
            public @NotNull String CustomActionName() {
                return "fade";
            }
        }
    }

    public static final class TilemapPos {
        public final boolean isWall;
        public final int index;

        @Contract(pure = true)
        public TilemapPos(boolean isWall, int index) {
            this.isWall = isWall;
            this.index = index;
        }
    }

    @Contract(pure = true)
    public static @NotNull TilemapPos findIndex(@Nullable CustomTilemap tilemap) {
        TilemapPos pos;
        int idx = Dungeon.level.customWalls.indexOf(tilemap);
        if (idx != -1) {
             pos = new TilemapPos(true, idx);
        } else {
            idx = Dungeon.level.customTiles.indexOf(tilemap);
            pos = new TilemapPos(false, idx);
        }
        return pos;
    }
}
