package io.github.pixeldungeonmultiplayer.shattered.server.headless;

import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.shatteredpixel.shatteredpixeldungeon.desktop.DesktopPlatformSupport;

/** Platform operations which have no meaning on a headless server. */
final class HeadlessPlatformSupport extends DesktopPlatformSupport {
    @Override public void updateDisplaySize() { }
    @Override public void updateSystemUI() { }
    @Override public boolean connectedToUnmeteredNetwork() { return true; }
    @Override public boolean supportsVibration() { return false; }
    @Override public boolean supportsFullScreen() { return false; }
    @Override public boolean supportsPlugins() { return false; }
    @Override public void setupFontGenerators(int pageSize, boolean systemFont) { }
    @Override protected FreeTypeFontGenerator getGeneratorForString(String input) { return null; }

    @Override
    public String[] splitforTextBlock(String text, boolean multiline) {
        return new String[]{text};
    }
}
