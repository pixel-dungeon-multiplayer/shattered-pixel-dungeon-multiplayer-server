package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SampleAction {

    @Contract(value = "-> fail", pure = true)
    private SampleAction() {throw new RuntimeException();}

    public static class PlayAction implements ImmutableNetworkAction {
        @NotNull public final String id;
        public final float leftVolume;
        public final float rightVolume;
        public final float rate;
        public final float pitch;
        @Nullable public final Float delay;

        @Contract(pure = true)
        public PlayAction(@NotNull String id, float leftVolume, float rightVolume, float rate, float pitch, @Nullable Float delay) {
            this.id = id;
            this.leftVolume = leftVolume;
            this.rightVolume = rightVolume;
            this.rate = rate;
            this.pitch = pitch;
            this.delay = delay;
        }

        @Override
        @Contract(pure = true)
        public @NotNull String actionName() {
            return "play_sample";
        }
    }

    public static class LoadAction implements ImmutableNetworkAction {
        public final @NotNull String @NotNull[] samples;

        @Contract(pure = true)
        public LoadAction(@NotNull String @NotNull[] samples) {
            this.samples = samples;
        }

        @Override
        @Contract(pure = true)
        public @NotNull String actionName() {
            return "load_sample";
        }
    }

    public static class UnloadAction implements ImmutableNetworkAction {
        public final @NotNull String sample;

        @Contract(pure = true)
        public UnloadAction(@NotNull String sample) {
            this.sample = sample;
        }

        @Override
        @Contract(pure = true)
        public @NotNull String actionName() {
            return "unload_sample";
        }
    }

    public static class ReloadAction implements ImmutableNetworkAction {
        public final @NotNull String @NotNull[] samples;

        @Contract(pure = true)
        public ReloadAction(@NotNull String @NotNull[] samples) {
            this.samples = samples;
        }

        @Override
        @Contract(pure = true)
        public @NotNull String actionName() {
            return "reload_sample";
        }
    }
}
