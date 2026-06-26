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

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import io.github.pixeldungeonmultiplayer.shattered.server.network.SendData;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

//FIXME needs a refactor, lots of weird thread interaction here.
public class AttackIndicator {
	
	private Char lastTarget;
	private final ArrayList<Char> candidates = new ArrayList<>();
	private final Hero owner;
	
	public AttackIndicator(@NotNull Hero hero) {
		owner = hero;
		synchronized (this) {
			lastTarget = null;
		}
	}


	/**
	 * Updates {@link #candidates}. Updates {@link #lastTarget}.
	 * <p>
	 * If {@link #lastTarget} not in {@link #candidates},  chooses random element from
	 * {@link #candidates} or {@code null} if {@link #candidates} is empty.
	 */
	private synchronized void checkEnemies() {

		candidates.clear();
		int v = owner.visibleEnemies();
		for (int i=0; i < v; i++) {
			Char mob = owner.visibleEnemy( i );
			if ( owner.canAttack(mob) ) {
				candidates.add( mob );
			}
		}
		
		if (lastTarget == null || !candidates.contains( lastTarget )) {
			if (candidates.isEmpty()) {
				setLastTarget(null);
			} else {
				setLastTarget(Random.element( candidates ));
			}
		}
	}

	protected void onClick() {
		if ((lastTarget != null) && owner.isReady()) {
			if (owner.handle(lastTarget.pos)) {
				owner.next();
			}
		}
	}

	public void target(Char target) {
		if (target == null) return;
		synchronized (this) {
			setLastTarget(target);

			QuickSlotButton.target(target);
		}
	}
	
	public void updateState() {
		this.checkEnemies();
	}

	private void setLastTarget(Char lastTarget) {
		this.lastTarget = lastTarget;
		SendData.sendHeroAttackIndicator(lastTarget == null? null: lastTarget.id(), owner.networkID);
	}
}
