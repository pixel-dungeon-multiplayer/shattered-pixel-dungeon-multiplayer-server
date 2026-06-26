package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import org.jetbrains.annotations.NotNull;

public abstract class MusicAction implements ImmutableNetworkAction {
    //Indicates which action related to music client should parse
    public abstract String musicActionType();

    @Override
    public @NotNull String actionName() {
        return "music_"+musicActionType();
    }

    public static class PlayAction extends MusicAction {
        public final String assetName;
        public final boolean looping;
        @Override
        public String musicActionType() {
            return "play";
        }

        public PlayAction(String assetName, boolean looping) {
            this.assetName = assetName;
            this.looping = looping;
        }

        public PlayAction(String assetName) {
            this.assetName = assetName;
            this.looping = false;
        }
    }
    public static class PlayTracksAction extends MusicAction {
        public final String[] tracks;
        public final float[] chances;
        public final boolean shuffle;
        @Override
        public String musicActionType() {
            return "play_tracks";
        }

        public PlayTracksAction(String[] tracks, float[] chances, boolean shuffle) {
            this.tracks = tracks;
            this.chances = chances;
            this.shuffle = shuffle;
        }
    }
    public static class EndAction extends MusicAction {
        @Override
        public String musicActionType() {
            return "end";
        }
    }
    public static class FadeOutAction extends MusicAction {

        public final MusicAction callback;
        public final float duration;
        @Override
        public String musicActionType() {
            return "fade_out";
        }


        public FadeOutAction(float duration) {
            this.duration = duration;
            this.callback = null;
        }

        public FadeOutAction(float duration, MusicAction callBack) {
            this.callback = callBack;
            this.duration = duration;
        }
    }
}
