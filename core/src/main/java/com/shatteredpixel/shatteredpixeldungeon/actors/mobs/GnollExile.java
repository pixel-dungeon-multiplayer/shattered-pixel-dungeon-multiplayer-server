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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;


import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GnollExileSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.BArray;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class GnollExile extends Gnoll {

	//has 2x HP, +50% other stats, and +1 reach vs. a regular gnoll scout
	//in exchange, they do not aggro automatically, and drop extra loot

	{
		spriteClass = GnollExileSprite.class;

		PASSIVE = new Passive();
		WANDERING = new Wandering();
		state = PASSIVE;

		defenseSkill = 6;
		HP = HT = 24;

		lootChance = 0f; //see rollToDropLoot
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 1, 10 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 15;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 1);
	}

	@Override
	protected boolean canAttack( Char enemy ) {
		if (Dungeon.level.adjacent( pos, enemy.pos )){
			return true;
		}

		if (Dungeon.level.distance( pos, enemy.pos ) <= 2){
			boolean[] passable = BArray.not(Dungeon.level.solid, null);

			for (Char ch : Actor.chars()) {
				//our own tile is always passable
				passable[ch.pos] = ch == this;
			}

			PathFinder.buildDistanceMap(enemy.pos, passable, 2);

			if (PathFinder.distance[pos] <= 2){
				return true;
			}
		}

		return super.canAttack(enemy);
	}

	@Override
	public void rollToDropLoot(Hero hero) {
		super.rollToDropLoot(hero);

		if (hero.lvl > maxLvl + 2) return;

		//drops 2 or 3 random items
		ArrayList<Item> items = new ArrayList<>();
		items.add(Generator.randomUsingDefaults());
		items.add(Generator.randomUsingDefaults());
		if (Random.Int(2) == 0) items.add(Generator.randomUsingDefaults());

		for (Item item : items){
			int ofs;
			do {
				ofs = PathFinder.NEIGHBOURS9[Random.Int(9)];
			} while (Dungeon.level.solid[pos + ofs] && !Dungeon.level.passable[pos + ofs]);
			Dungeon.level.drop( item, pos + ofs ).sprite.drop( pos );
		}

	}

	@Override
	public void beckon(int cell) {
		if (state != PASSIVE) {
			super.beckon(cell);
		} else {
			//still attracts if passive, but doesn't remove passive state
			target = cell;
		}
	}

	@Override
	public LocalizedString description() {
		LocalizedString desc = super.description();
		if (state == PASSIVE){
			desc = LocalizedString.concat(desc, LocalizedString.concat("\n\n", Messages.get(this, "desc_passive")));
		} else {
			desc = LocalizedString.concat(desc, LocalizedString.concat("\n\n", Messages.get(this, "desc_aggro")));
		}
		return desc;
	}

	//gnoll exiles wander around while passive
	private class Passive extends Mob.Wandering {

		private int seenNotifyCooldown = 0;

		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {

			for (Buff b : buffs()){
				if (b.type == Buff.buffType.NEGATIVE){
					//swap to aggro if we've been debuffed
					state = WANDERING;
					return true;
				}
			}
			boolean didNotify = false;
			for (Hero hero : Dungeon.heroes) {
				if (hero != null && hero.fieldOfView[pos]) {
					if (seenNotifyCooldown <= 0) {
						GLog.p(Messages.get(GnollExile.class, "seen_passive", hero));
					}
					seenNotifyCooldown = 10;
					didNotify = true;
					break;
				}
			}
			if (!didNotify) {
				seenNotifyCooldown--;
			}
			if (enemyInFOV && justAlerted) {

				if (Dungeon.visibleforAnyHero(pos)) {
					GLog.w(Messages.get(GnollExile.class, "seen_aggro"));
				}
				return noticeEnemy();

			} else {

				return continueWandering();

			}
		}
	}

	//standard wandering but with a warning that the exile is aggroed
	private class Wandering extends Mob.Wandering {
		@Override
		protected boolean noticeEnemy() {
			GLog.w(Messages.get(GnollExile.class, "seen_aggro"));
			return super.noticeEnemy();
		}
	}

}
