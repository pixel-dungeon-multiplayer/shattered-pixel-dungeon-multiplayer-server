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

package com.shatteredpixel.shatteredpixeldungeon.items.spells;

import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.MetalShard;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.journal.Bestiary;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import io.github.pixeldungeonmultiplayer.shattered.server.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Reflection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ReclaimTrap extends TargetedSpell {
	
	{
		image = ItemSpriteSheet.RECLAIM_TRAP;

		talentChance = 1/(float)Recipe.OUT_QUANTITY;
	}

	//This class has a variety of code for compat with pre-v3.0.0 saves
	//Stored traps used to be a property of the item itself, but in 3.0.0 this was changed to be
	//a buff attached to the hero, which is much more resistant to exploits

	private Class<?extends Trap> storedTrap = null;
	
	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		//prevents exploits, pre-v3.0.0
		if (storedTrap != null){
			actions.remove(AC_DROP);
			actions.remove(AC_THROW);
		}
		return actions;
	}

	@Override
	protected void affectTarget(Ballistica bolt, Hero hero) {
		Class<?extends Trap> storedTrap = null;
		//pre-v3.0.0
		if (this.storedTrap != null){
			storedTrap = this.storedTrap;
			this.storedTrap = null;
		} else {
			if (hero.buff(ReclaimedTrap.class) != null){
				storedTrap = hero.buff(ReclaimedTrap.class).trap;
				hero.buff(ReclaimedTrap.class).detach();
			}
		}
		if (storedTrap == null) {
			Trap t = Dungeon.level.traps.get(bolt.collisionPos);
			if (t != null && t.isActive() && t.visible) {
				t.disarm(); //even disarms traps that normally wouldn't be
				
				Sample.INSTANCE.play(Assets.Sounds.LIGHTNING);
				ScrollOfRecharging.charge(hero);
				Buff.affect(hero, ReclaimedTrap.class).trap = t.getClass();
				Bestiary.setSeen(t.getClass());
				Bestiary.setSeen(t.getClass());

			} else {
				GLog.w(Messages.get(this, "no_trap"));
			}

			//spell is not consumed, so doesn't count as a full use
			Invisibility.dispel();
			curUser.spendAndNext( timeToCast() );

		} else {
			
			Trap t = Reflection.newInstance(storedTrap);
			
			t.pos = bolt.collisionPos;
			t.reclaimed = true;
			Bestiary.countEncounter(t.getClass());
			t.activate();

			onSpellused();

		}
	}
	
	@Override
	public LocalizedString desc(Hero hero) {
		LocalizedString desc = super.desc();
		if (storedTrap != null){
			desc = LocalizedString.concat(desc, LocalizedString.concat("\n\n", Messages.get(this, "desc_trap", Messages.get(storedTrap, "name"))));
		} else if (hero != null && hero.belongings.contains(this) && hero.buff(ReclaimedTrap.class) != null){
			desc = LocalizedString.concat(desc, LocalizedString.concat("\n\n", Messages.get(this, "desc_trap", Messages.get(hero.buff(ReclaimedTrap.class).trap, "name"))));
		}
		return desc;
	}
	
	private static final ItemSprite.Glowing[] COLORS = new ItemSprite.Glowing[]{
			new ItemSprite.Glowing( 0xFF0000 ),
			new ItemSprite.Glowing( 0xFF8000 ),
			new ItemSprite.Glowing( 0xFFFF00 ),
			new ItemSprite.Glowing( 0x00FF00 ),
			new ItemSprite.Glowing( 0x00FFFF ),
			new ItemSprite.Glowing( 0x8000FF ),
			new ItemSprite.Glowing( 0xFFFFFF ),
			new ItemSprite.Glowing( 0x808080 ),
			new ItemSprite.Glowing( 0x000000 )
	};
	
	@Override
	public ItemSprite.Glowing glowing() {
		Hero hero = findOwner();
		if (storedTrap != null){
			return COLORS[Reflection.newInstance(storedTrap).color];
		} else if (hero != null && hero.belongings.contains(this) && hero.buff(ReclaimedTrap.class) != null){
			return COLORS[Reflection.newInstance(hero.buff(ReclaimedTrap.class).trap).color];
		}
		return null;
	}
	
	@Contract(pure = true)
    @Override
	public int value() {
		return (int)(60 * (quantity() /(float)Recipe.OUT_QUANTITY));
	}

	@Contract(pure = true)
    @Override
	public int energyVal() {
		return (int)(12 * (quantity() /(float)Recipe.OUT_QUANTITY));
	}
	
	private static final String STORED_TRAP = "stored_trap";
	
	@Override
	public void storeInBundle(@NotNull Bundle bundle) {
		super.storeInBundle(bundle);
		if (storedTrap != null) bundle.put(STORED_TRAP, storedTrap);
	}
	
	@Override
	public void restoreFromBundle(@NotNull Bundle bundle) {
		super.restoreFromBundle(bundle);
		if (bundle.contains(STORED_TRAP)) storedTrap = bundle.getClass(STORED_TRAP);
	}
	
	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {

		private static final int OUT_QUANTITY = 5;
		
		{
			inputs =  new Class[]{ScrollOfMagicMapping.class, MetalShard.class};
			inQuantity = new int[]{1, 1};
			
			cost = 8;
			
			output = ReclaimTrap.class;
			outQuantity = OUT_QUANTITY;
		}

		@Override
		public Item brew(ArrayList<Item> ingredients, Hero hero) {
			Catalog.countUse(MetalShard.class);
			return super.brew(ingredients, hero);
		}
	}

	public static class ReclaimedTrap extends Buff {

		{
			revivePersists = true;
		}

		private Class<?extends Trap> trap;

		private static final String TRAP = "trap";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(TRAP, trap);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			trap = bundle.getClass(TRAP);
		}
	}
	
}
