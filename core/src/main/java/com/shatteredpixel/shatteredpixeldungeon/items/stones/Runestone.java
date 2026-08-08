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

package com.shatteredpixel.shatteredpixeldungeon.items.stones;

import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import org.jetbrains.annotations.Contract;

public abstract class Runestone extends Item {
	
	{
		stackable = true;
		defaultAction = AC_THROW;
	}
	//FIXME
	protected boolean anonymous = false;

	public void anonymize(){
		image = ItemSpriteSheet.STONE_HOLDER;
		anonymous = true;
	}

	@Override
	protected void onThrow(int cell, Hero hero) {
		///inventory stones are thrown like normal items, other stones don't trigger when thrown into pits
		if (this instanceof InventoryStone ||
				hero.buff(MagicImmune.class) != null ||
				(Dungeon.level.pit[cell] && Actor.findChar(cell) == null)){
			if (!anonymous) super.onThrow( cell, hero );
		} else {
			Catalog.countUse(getClass());
			activate(cell, hero);
			if (!anonymous) {
				Catalog.countUse(getClass());
				Talent.onRunestoneUsed(curUser, cell, getClass());
			}
			activate(cell, hero);
			if (Actor.findChar(cell) == null) Dungeon.level.pressCell( cell );
			Invisibility.dispel(hero);
		}
	}
	protected abstract void activate(int cell, Hero user);
	
	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}
	
	@Contract(pure = true)
    @Override
	public int value() {
		return 15 * quantity();
	}

	@Contract(pure = true)
    @Override
	public int energyVal() {
		return 3 * quantity();
	}

	public static class PlaceHolder extends Runestone {
		
		{
			image = ItemSpriteSheet.STONE_HOLDER;
		}
		
		@Override
		protected void activate(int cell, Hero user) {
			//does nothing
		}
		
		@Override
		public boolean isSimilar(Item item) {
			return item instanceof Runestone;
		}
		
		@Override
		public LocalizedString info() {
			return LocalizedString.EMPTY;
		}
	}
}
