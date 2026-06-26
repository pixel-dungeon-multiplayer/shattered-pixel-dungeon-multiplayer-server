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

import java.util.ArrayList;
import java.util.List;

public class WndOptions extends Window {

	protected static final int WIDTH_P = 120;
	protected static final int WIDTH_L = 144;

	protected static final int MARGIN 		= 2;
	protected static final int BUTTON_HEIGHT	= 18;

	private WndOptionsParams params;

	public WndOptions(Hero hero, Image icon, String title, String message, String... options) {
		this(hero, icon, LocalizedString.raw(title), LocalizedString.raw(message), LocalizedString.raw(options));
	}
	public WndOptions(Hero hero, Image icon, LocalizedString title, LocalizedString message, LocalizedString... options) {
		super(hero);
		WndOptionsParams params = new WndOptionsParams();
		params.title = title;
		params.message = message;
		params.options = List.of(options);
		if (icon instanceof CharSprite) {
			params.charSprite = (CharSprite) icon;
		} else if (icon instanceof ItemSprite) {
			params.itemSpriteImage = ((ItemSprite) icon).image();
			params.itemSpriteGlowing = ((ItemSprite) icon).glowing();
		}
		sendWnd(params);
	}
	public WndOptions(Hero owner, LocalizedString title, LocalizedString message, LocalizedString... options) {
		super(owner);
		WndOptionsParams params = new WndOptionsParams();
		params.title = title;
		params.message = message;
		params.options = List.of(options);
		sendWnd(params);
	}


	protected void sendWnd(Image icon, @NotNull String title, @Nullable Integer titleColor, @NotNull String message, String... options) {
		this.sendWnd(icon, LocalizedString.raw(title), titleColor, LocalizedString.raw(message), LocalizedString.raw(options));
	}

	protected void sendWnd(Image icon, @NotNull LocalizedString title, @Nullable Integer titleColor, @NotNull LocalizedString message, LocalizedString... options) {
		WndOptionsParams params = new WndOptionsParams();
		params.title = title;
		params.titleColor = titleColor;
		params.message = message;
		if (icon instanceof CharSprite) {
			params.charSprite = (CharSprite) icon;
		} else if (icon instanceof ItemSprite) {
			params.itemSpriteImage = ((ItemSprite) icon).image();
			params.itemSpriteGlowing = ((ItemSprite) icon).glowing();
		}
		params.options = List.of(options);
		sendWnd(params);
	}

	protected void sendWnd(WndOptionsParams params) {
		this.params = params;
	}

	public WndOptionsParams params() {
		return params;
	}

	public WndOptions( Hero hero, String title, String message, String... options ) {
		super(hero);

		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

		float pos = MARGIN;
		if (title != null) {
			RenderedTextBlock tfTitle = PixelScene.renderTextBlock(title, 9);
			tfTitle.hardlight(TITLE_COLOR);
			tfTitle.setPos(MARGIN, pos);
			tfTitle.maxWidth(width - MARGIN * 2);
			add(tfTitle);

			pos = tfTitle.bottom() + 2*MARGIN;
		}
		
		layoutBody(pos, message, options);
	}

	protected void layoutBody(float pos, String message, String... options){
		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

		RenderedTextBlock tfMesage = PixelScene.renderTextBlock( 6 );
		tfMesage.text(message, width);
		tfMesage.setPos( 0, pos );
		add( tfMesage );

		pos = tfMesage.bottom() + 2*MARGIN;

		for (int i=0; i < options.length; i++) {
			final int index = i;
			RedButton btn = new RedButton( options[i] ) {
				@Override
				protected void onClick() {
					hide();
					onSelect( index );
				}
			};
			if (hasIcon(i)) btn.icon(getIcon(i));
			btn.multiline = true;
			add( btn );

			if (!hasInfo(i)) {
				btn.setRect(0, pos, width, BUTTON_HEIGHT);
			} else {
				btn.setRect(0, pos, width - BUTTON_HEIGHT, BUTTON_HEIGHT);
				IconButton info = new IconButton(Icons.get(Icons.INFO)){
					@Override
					protected void onClick() {
						onInfo( index );
					}
				};
				info.setRect(width-BUTTON_HEIGHT, pos, BUTTON_HEIGHT, BUTTON_HEIGHT);
				add(info);
			}

			btn.enable(enabled(i));

			pos += BUTTON_HEIGHT + MARGIN;
		}

		resize( width, (int)(pos - MARGIN) );
	}
	public static final class WndOptionsParams {
		public @Nullable Integer itemSpriteImage;
		public @Nullable ItemSprite.Glowing itemSpriteGlowing;
		public @Nullable CharSprite charSprite;
		public @NotNull LocalizedString title = LocalizedString.raw("Untitled");
		public @Nullable Integer titleColor = null;
		public @NotNull LocalizedString message = LocalizedString.raw("MissingNo");
		public List<LocalizedString> options = new ArrayList<LocalizedString>(3);

	}

	protected boolean enabled( int index ){
		return true;
	}

	public boolean enabledForNetwork(int index) {
		return enabled(index);
	}
	
	protected void onSelect( int index ) {}

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
