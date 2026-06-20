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

import com.nikita22007.multiplayer.utils.text.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.utils.Callback;

public class WndInfoTalent extends Window {

	private static final float GAP	= 2;

	private static final int WIDTH_MIN = 120;
	private static final int WIDTH_MAX = 220;
	private final Talent talent;
	private final int points;
	private final TalentButtonCallback buttonCallback;
	private final boolean metaDesc;
	public final IconTitle titlebar;
	public final RenderedTextBlock txtInfo;
	public RedButton button;

	public WndInfoTalent(Talent talent, int points, TalentButtonCallback buttonCallback){
		super();
		this.talent = talent;
		this.points = points;
		this.buttonCallback = buttonCallback;

		int width = WIDTH_MIN;

		titlebar = new IconTitle();

		titlebar.icon( new TalentIcon( talent ) );
		LocalizedString title = Messages.titleCase(talent.title());
		if (points > 0){
			title = LocalizedString.concat(title, " +", points);
		}
		titlebar.label( title, Window.TITLE_COLOR );
		titlebar.setRect( 0, 0, width, 0 );
		add( titlebar );

		metaDesc = (buttonCallback != null && buttonCallback.metamorphDesc()) ||
				(getOwnerHero() != null && getOwnerHero().metamorphedTalents.containsValue(talent));

		txtInfo = PixelScene.renderTextBlock(talent.desc(metaDesc), 6);
		txtInfo.maxWidth(width);
		txtInfo.setPos(titlebar.left(), titlebar.bottom() + 2*GAP);
		add( txtInfo );

		while (PixelScene.landscape()
				&& txtInfo.height() > 120
				&& width < WIDTH_MAX){
			width += 20;
			txtInfo.maxWidth(width);
		}
		titlebar.setRect( 0, 0, width, 0 );
		resize( width, (int)(txtInfo.bottom() + GAP) );

		if (buttonCallback != null) {
			button = new RedButton( buttonCallback.prompt() ) {
				@Override
				protected void onClick() {
					super.onClick();
					hide();
					buttonCallback.call();
				}
			};
			button.icon(Icons.get(Icons.TALENT));
			button.setRect(0, txtInfo.bottom() + 2*GAP, width, 18);
			add(button);
			resize( width, (int)button.bottom()+1 );
		}

	}

	public Talent talent() {
		return talent;
	}

	public int points() {
		return points;
	}

	public TalentButtonCallback buttonCallback() {
		return buttonCallback;
	}

	public boolean metaDesc() {
		return metaDesc;
	}

	@Override
	protected void onSelect(int button) {
		if (button == 0 && this.button != null) {
			this.button.onClickNetwork();
		}
	}

	public static abstract class TalentButtonCallback implements Callback {

		public abstract LocalizedString prompt();

		public boolean metamorphDesc(){
			return false;
		}

	}

}
