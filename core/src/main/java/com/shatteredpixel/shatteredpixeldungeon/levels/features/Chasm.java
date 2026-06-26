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

package com.shatteredpixel.shatteredpixeldungeon.levels.features;

import com.nikita22007.multiplayer.utils.Log;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfFeatherFall;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.WeakFloorRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterLevelSceneServer;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Image;
import com.nikita22007.multiplayer.noosa.audio.Sample;
import com.watabou.utils.Random;

public class Chasm implements Hero.Doom {

	public static boolean jumpConfirmed = false;
	private static int heroPos;
	
	public static void heroJump( final Hero hero ) {
		heroPos = hero.pos;
				GameScene.show(
						new WndOptions(hero,  new Image(Dungeon.level.tilesTex(), 176, 16, 16, 16),
								Messages.get(Chasm.class, "chasm"),
								Messages.get(Chasm.class, "jump"),
								Messages.get(Chasm.class, "yes"),
								Messages.get(Chasm.class, "no") ) {



							@Override
							protected void onSelect( int index ) {
								if (index == 0) {
									if (getOwnerHero().pos == heroPos) {
										jumpConfirmed = true;
										hero.resume();
									}
								} else {
									hide();
								}
							}
						});
	}
	
	public static void heroFall( int pos, Hero hero ) {
		
		jumpConfirmed = false;
				
		Sample.INSTANCE.play( Assets.Sounds.FALLING );

		Level.beforeTransition();

		if (hero.isAlive()) {
			hero.interrupt();
			InterLevelSceneServer.mode = InterLevelSceneServer.Mode.FALL;
			if (Dungeon.level instanceof RegularLevel &&
						((RegularLevel)Dungeon.level).room( pos ) instanceof WeakFloorRoom){
				InterLevelSceneServer.fallIntoPit = true;
				Notes.remove(Notes.Landmark.DISTANT_WELL);
			} else {
				InterLevelSceneServer.fallIntoPit = false;
			}
			InterLevelSceneServer.create(hero);
		} else {
			hero.getSprite().visible = false;
		}
	}

	@Override
	public void onDeath() {
		Badges.validateDeathFromFalling();

		Dungeon.fail( Chasm.class );
		GLog.n( Messages.get(Chasm.class, "ondeath") );
	}

	public static void heroLand(Hero hero) {
		
		ElixirOfFeatherFall.FeatherBuff b = hero.buff(ElixirOfFeatherFall.FeatherBuff.class);
		
		if (b != null){
			hero.getSprite().emitter().burst( Speck.factory( Speck.JET ), 20);
			b.processFall();
			return;
		}
		
		PixelScene.shake( 4, 1f );

		Dungeon.level.occupyCell(hero );
		Buff.prolong( hero, Cripple.class, Cripple.DURATION );

		//The lower the hero's HP, the more bleed and the less upfront damage.
		//Hero has a 50% chance to bleed out at 66% HP, and begins to risk instant-death at 25%
		Buff.affect( hero, Bleeding.class).set( Math.round(hero.getHT() / (6f + (6f*(hero.getHP() /(float) hero.getHT())))), Chasm.class);
		hero.damage( Math.max( hero.getHP() / 2, Random.NormalIntRange( hero.getHP() / 2, hero.getHT() / 4 )), new Char.DamageCause(new Chasm(), null) );
	}

	public static void mobFall( Mob mob ) {
		if (mob.isAlive()) {
			Buff.prolong(mob, Trap.HazardAssistTracker.class, Trap.HazardAssistTracker.DURATION);
			mob.die( new Char.DamageCause( Chasm.class, null ));
		}
		
		if (mob.getSprite() != null) ((MobSprite) mob.getSprite()).fall();
	}
	
	public static class Falling extends Buff {
		
		{
			actPriority = VFX_PRIO;
		}
		
		@Override
		public boolean act() {
			if (target instanceof Hero) {
				heroLand((Hero) target);
			} else {
				Log.e("Chasm","Falling actor is not hero");
			}
			detach();
			return true;
		}
	}

}
