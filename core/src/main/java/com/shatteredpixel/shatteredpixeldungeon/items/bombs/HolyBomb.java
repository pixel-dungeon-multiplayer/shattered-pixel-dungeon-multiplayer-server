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

package com.shatteredpixel.shatteredpixeldungeon.items.bombs;

import io.github.pixeldungeonmultiplayer.shattered.server.utils.Log;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import io.github.pixeldungeonmultiplayer.shattered.server.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import org.jetbrains.annotations.Contract;

import java.util.ArrayList;

public class HolyBomb extends Bomb {
	
	{
		image = ItemSpriteSheet.HOLY_BOMB;
	}
	@Override
	protected int explosionRange() {
		return 2;
	}

	@Override
	public void explode(int cell) {
		super.explode(cell);

		for (Hero h: Dungeon.heroes) {
			if (h == null) continue;
			if (h.fieldOfView[cell]) {
				new Flare(10, 64).show(cell, 2f);
			}
		}
		ArrayList<Char> affected = new ArrayList<>();
		
		PathFinder.buildDistanceMap( cell, BArray.not( Dungeon.level.solid, null ), explosionRange() );
		for (int i = 0; i < PathFinder.distance.length; i++) {
			if (PathFinder.distance[i] < Integer.MAX_VALUE) {
				Char ch = Actor.findChar(i);
				if (ch != null) {
					affected.add(ch);
					
				}
			}
		}
		
		for (Char ch : affected){
			if (ch.properties().contains(Char.Property.UNDEAD) || ch.properties().contains(Char.Property.DEMONIC)){
				ch.getSprite().emitter().start( ShadowParticle.UP, 0.05f, 10 );
				
				//bomb deals an additional 50% damage to unholy enemies
				int damage = Math.round(Random.NormalIntRange( Dungeon.scalingDepth()+4, 12 + 3*Dungeon.scalingDepth() ) * 0.5f);
				if (curItem == this) {
					ch.damage(damage, new Char.DamageCause(new HolyDamage(), curUser));
				} else {
					Log.e("HolyBomb","Acracne bomb explosion curr item is not this");
					ch.damage(damage, new Char.DamageCause(new HolyDamage(), null));
				}
			}
		}
		
		Sample.INSTANCE.play( Assets.Sounds.READ );
	}

	public static class HolyDamage{}
	
	@Contract(pure = true)
    @Override
	public int value() {
		//prices of ingredients
		return quantity() * (20 + 30);
	}
}
