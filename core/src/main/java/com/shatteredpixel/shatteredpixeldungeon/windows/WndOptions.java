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

import com.watabou.utils.DeviceCompat;
import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WndOptions extends Window {

	protected static final int WIDTH_P = 120;
	protected static final int WIDTH_L = 144;

	protected static final int MARGIN 		= 2;
	protected static final int BUTTON_HEIGHT	= 18;

	public IconTitle titlebar;
	public RenderedTextBlock message;
	public RedButton[] optionButtons;

	public WndOptions(Hero hero, Image icon, LocalizedString title, LocalizedString message, LocalizedString... options) {
		this(hero, icon, title, null, message, options);
	}

	public WndOptions(Hero owner, LocalizedString title, LocalizedString message, LocalizedString... options) {
		this(owner, null, title, null, message, options);
	}

	public WndOptions(Hero hero, Image icon, LocalizedString title, Integer titleColor, LocalizedString message, LocalizedString... options) {
		super(hero);

		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

		float pos = MARGIN;
		if (title != null || icon != null) {
			titlebar = new IconTitle();
			if (icon != null) {
				titlebar.icon(icon);
			}
			if (title != null) {
				if (titleColor != null) {
					titlebar.label(title, titleColor);
				} else {
					titlebar.label(title);
				}
			}
			titlebar.setRect(0, 0, width, 0);
			add(titlebar);

			pos = titlebar.bottom() + 2 * MARGIN;
		}

		this.message = PixelScene.renderTextBlock(6);
		this.message.text(message, width);
		this.message.setPos(0, pos);
		add(this.message);

		pos = this.message.bottom() + 2 * MARGIN;

		optionButtons = new RedButton[options.length];
		for (int i = 0; i < options.length; i++) {
			final int index = i;
			RedButton btn = new RedButton(options[i]) {
				@Override
				protected void onClick() {
					hide();
					if (DeviceCompat.isDebug()) {
						throw new RuntimeException("This never should happen");
					}
				}
			};
			if (hasIcon(i)) {
				btn.icon(getIcon(i));
			}
			btn.multiline = true;
			add(btn);
			optionButtons[i] = btn;

			if (!hasInfo(i)) {
				btn.setRect(0, pos, width, BUTTON_HEIGHT);
			} else {
				btn.setRect(0, pos, width - BUTTON_HEIGHT, BUTTON_HEIGHT);
				IconButton info = new IconButton(Icons.get(Icons.INFO)) {
					@Override
					protected void onClick() {
						onInfo(index);
					}
				};
				info.setRect(width - BUTTON_HEIGHT, pos, BUTTON_HEIGHT, BUTTON_HEIGHT);
				add(info);
			}

			btn.enable(enabled(i));

			pos += BUTTON_HEIGHT + MARGIN;
		}

		resize(width, (int) (pos - MARGIN));
	}

	@Override
	public void onSelect(int button) {
		hide();
	}

	@Override
	public void onSelect(int button, @Nullable final JSONObject args) {
		if (args!= null && args.optBoolean("info")) {
			onInfo(button);
			return;
		}
		super.onSelect(button, args);
	}

	protected boolean enabled(int index ){
		return true;
	}

	public boolean enabledForNetwork(int index) {
		return enabled(index);
	}
	
	
	

	protected boolean hasInfo( int index ) {
		return false;
	}

	public boolean hasInfoForNetwork(int index) {
		return hasInfo(index);
	}

	protected void onInfo( int index ) {}

	protected boolean hasIcon( int index ) {
		return false;
	}

	protected Image getIcon( int index ) {
		return null;
	}

	public Image optionIcon(int index) {
		return hasIcon(index) ? getIcon(index) : null;
	}
}
