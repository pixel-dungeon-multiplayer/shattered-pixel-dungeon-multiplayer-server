/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WndTitledMessage extends Window {

	protected static final int WIDTH_MIN    = 120;
	protected static final int WIDTH_MAX    = 220;
	protected static final int GAP	= 2;

	private @Nullable Image icon;
	private @Nullable LocalizedString title;
	private @Nullable LocalizedString message;
	private @Nullable Component titlebar;

	public WndTitledMessage(Hero hero, Image icon, LocalizedString title, LocalizedString message ) {
		super(hero);
		this.icon = icon;
		this.title = title;
		this.message = message;
		//this( new IconTitle( icon, title ), message, hero );

	}

	public WndTitledMessage(@NotNull Hero hero, @Nullable Component titlebar, @Nullable LocalizedString message ) {
		super(hero);
		this.titlebar = titlebar;
		this.message = message;
		//todo WndCheck Component titlebar cannot be losslessly converted to wnd_option title/title_icon yet.
	}

	public @Nullable Image titleIcon() {
		return icon;
	}

	public @Nullable LocalizedString title() {
		return title;
	}

	public @Nullable LocalizedString message() {
		return message;
	}

	public @Nullable Component titlebar() {
		return titlebar;
	}

	protected boolean useHighlighting(){
		return true;
	}

	public boolean highlightingForNetwork() {
		return useHighlighting();
	}

	protected float targetHeight() {
		return PixelScene.MIN_HEIGHT_L - 10;
	}
}
