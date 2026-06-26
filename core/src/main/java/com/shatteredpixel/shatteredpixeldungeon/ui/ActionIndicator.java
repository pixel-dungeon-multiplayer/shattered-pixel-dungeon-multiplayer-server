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

package com.shatteredpixel.shatteredpixeldungeon.ui;

import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.watabou.noosa.Visual;

public class ActionIndicator {

	private final Hero owner;

	public Action action;

	public ActionIndicator(Hero hero) {
		this.owner = hero;
	}

	protected void onClick() {
		if (action != null && owner.isReady()) {
			action.doAction(owner);
		}
	}


	public void setAction(Action action){
			this.action = action;
			refresh();
	}

	public void clearAction(){
		clearAction(null);
	}

	public void clearAction(Action action) {
		if (action == null || this.action == action) {
			this.action = null;
		}
	}

	public void refresh(){
		//todo send this
	}

	public interface Action {

		LocalizedString actionName();

		default int actionIcon(){
			return HeroIcon.NONE;
		}

		//usually just a static icon, unless overridden
		default Visual primaryVisual(){
			return new HeroIcon(this);
		}
		default Visual primaryVisual(Hero hero){
			return primaryVisual();
		}

		//a smaller visual on the bottom-right, usually a tiny icon or bitmap text
		default Visual secondaryVisual(){
			return null; //no second visual by default
		}
		default Visual secondaryVisual(Hero hero){
			return secondaryVisual(); //no second visual by default
		}

		default int indicatorColor(){
			return 0;
		};
		default int indicatorColor(Hero hero){
			return indicatorColor();
		};

		void doAction(Hero hero);

	}

}
