package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterLevelSceneServer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InterlevelSceneAction implements ImmutableNetworkAction {
    public enum FadeTime {
        SLOW_FADE, NORM_FADE, FAST_FADE
    }

    @Nullable
    public final String state;
    @Nullable
    public final InterLevelSceneServer.Mode mode;
    @Nullable
    public final LocalizedString customMessage;
    @Nullable
    public final Float scrollSpeed;
    @Nullable
    public final String loadingTexture;
    @Nullable
    public final FadeTime fadeTime;
    public final boolean resetLevel;

    @Contract(pure = true)
    public InterlevelSceneAction(@NotNull String state) {
        this(state, (LocalizedString) null);
    }

    @Contract(pure = true)
    public InterlevelSceneAction(@NotNull String state, @Nullable String customMessage) {
        this(state, customMessage == null ? null : LocalizedString.raw(customMessage));
    }

    @Contract(pure = true)
    public InterlevelSceneAction(@NotNull String state, @Nullable LocalizedString customMessage) {
        this.state = state;
        this.mode = null;
        this.customMessage = customMessage;
        this.scrollSpeed = null;
        this.loadingTexture = null;
        this.fadeTime = null;
        this.resetLevel = false;
    }

    @Contract(pure = true)
    public InterlevelSceneAction(@NotNull InterLevelSceneServer.Mode mode, @Nullable String loadingTexture, @Nullable FadeTime fadeTime) {
        this(mode, loadingTexture, fadeTime, null, null, false);
    }

    @Contract(pure = true)
    public InterlevelSceneAction(@NotNull InterLevelSceneServer.Mode mode, @Nullable String loadingTexture, @Nullable FadeTime fadeTime, @Nullable Float scrollSpeed, @Nullable LocalizedString customMessage) {
        this(mode, loadingTexture, fadeTime, scrollSpeed, customMessage, false);
    }

    @Contract(pure = true)
    public InterlevelSceneAction(@NotNull InterLevelSceneServer.Mode mode, @Nullable String loadingTexture, @Nullable FadeTime fadeTime, @Nullable Float scrollSpeed, @Nullable LocalizedString customMessage, boolean resetLevel) {
        this.state = null;
        this.mode = mode;
        this.customMessage = customMessage;
        this.scrollSpeed = scrollSpeed;
        this.loadingTexture = loadingTexture;
        this.fadeTime = fadeTime;
        this.resetLevel = resetLevel;
    }

    @Override
    @Contract(pure = true)
    public @NotNull String actionName() {
        return "interlevel_scene";
    }
}
