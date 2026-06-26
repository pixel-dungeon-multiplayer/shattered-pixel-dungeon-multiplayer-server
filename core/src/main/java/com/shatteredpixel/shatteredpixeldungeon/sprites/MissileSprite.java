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

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import io.github.pixeldungeonmultiplayer.shattered.server.utils.Log;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import io.github.pixeldungeonmultiplayer.shattered.server.network.SendData;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.MissileSpriteVisualAction;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.HolyLance;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GnollGeomancer;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Crossbow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Bolas;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.FishingSpear;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.HeavyBoomerang;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Javelin;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Kunai;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Shuriken;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingKnife;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingSpear;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingSpike;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Trident;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.Dart;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.tweeners.PosTweener;
import com.watabou.noosa.tweeners.Tweener;
import com.watabou.utils.Callback;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.PointF;

import java.util.HashMap;

public final class MissileSprite extends ItemSprite implements Tweener.Listener {

	private static final float SPEED	= 240f;
	
	private Callback callback;
	
	public void reset( int from, int to, Item item, Callback listener ) {
		reset( Anchor.cell(from), Anchor.cell(to), item, listener );
	}

	public void reset( CharSprite from, int to, Item item, Callback listener ) {
		reset( Anchor.character(from), Anchor.cell(to), item, listener );
	}

	public void reset( int from, CharSprite to, Item item, Callback listener ) {
		reset( Anchor.cell(from), Anchor.character(to), item, listener );
	}

	public void reset( CharSprite from, CharSprite to, Item item, Callback listener ) {
		reset( Anchor.character(from), Anchor.character(to), item, listener );
	}

	private void reset( Anchor from, Anchor to, Item item, Callback listener) {
		revive();

		if (item == null) {
			view(0, null);
			Log.e("MissileSprite", "item is null");
			if (DeviceCompat.isDebug()) {
				throw new RuntimeException("MissileSprite: item is null");
			}
		}
		else                view( item );

		setup( from,
				to,
				item,
				listener );
	}
	
	private static final int DEFAULT_ANGULAR_SPEED = 720;
	
	private static final HashMap<Class<?extends Item>, Integer> ANGULAR_SPEEDS = new HashMap<>();
	static {
		ANGULAR_SPEEDS.put(Dart.class,          0);
		ANGULAR_SPEEDS.put(ThrowingKnife.class, 0);
		ANGULAR_SPEEDS.put(ThrowingSpike.class, 0);
		ANGULAR_SPEEDS.put(FishingSpear.class,  0);
		ANGULAR_SPEEDS.put(ThrowingSpear.class, 0);
		ANGULAR_SPEEDS.put(Kunai.class,         0);
		ANGULAR_SPEEDS.put(Javelin.class,       0);
		ANGULAR_SPEEDS.put(Trident.class,       0);
		
		ANGULAR_SPEEDS.put(SpiritBow.SpiritArrow.class,       0);
		ANGULAR_SPEEDS.put(ScorpioSprite.ScorpioShot.class,   0);
		ANGULAR_SPEEDS.put(HolyLance.HolyLanceVFX.class,      0);

		//720 is default

		ANGULAR_SPEEDS.put(GnollGeomancer.Boulder.class,   90);
		
		ANGULAR_SPEEDS.put(HeavyBoomerang.class,1440);
		ANGULAR_SPEEDS.put(Bolas.class,         1440);
		
		ANGULAR_SPEEDS.put(Shuriken.class,                  2160);
		ANGULAR_SPEEDS.put(TenguSprite.TenguShuriken.class, 2160);
	}

	//TODO it might be nice to have a source and destination angle, to improve thrown weapon visuals
	private void setup( Anchor anchorFrom, Anchor anchorTo, Item item, Callback listener ){

		PointF from = anchorFrom.toPointF();
		PointF to = anchorTo.toPointF();

		originToCenter();

		//adjust points so they work with the center of the missile sprite, not the corner
		from.x -= width()/2;
		to.x -= width()/2;
		from.y -= height()/2;
		to.y -= height()/2;

		this.callback = listener;

		point( from );

		PointF d = PointF.diff( to, from );
		speed.set(d).normalize().scale(SPEED);
		
		angularSpeed = DEFAULT_ANGULAR_SPEED;
		for (Class<?extends Item> cls : ANGULAR_SPEEDS.keySet()){
			if (cls.isAssignableFrom(item.getClass())){
				angularSpeed = ANGULAR_SPEEDS.get(cls);
				break;
			}
		}
		
		angle = 135 - (float)(Math.atan2( d.x, d.y ) / 3.1415926 * 180);
		
		if (d.x >= 0){
			flipHorizontal = false;
			updateFrame();
			
		} else {
			angularSpeed = -angularSpeed;
			angle += 90;
			flipHorizontal = true;
			updateFrame();
		}

		if (item instanceof GnollGeomancer.Boulder){
			angle = 0;
			flipHorizontal = false;
			updateFrame();
		}
		
		float speed = SPEED;

		Hero owner = item.findOwner(); //todo fixme
		if (owner != null && item instanceof Dart
				&& (owner.belongings.weapon() instanceof Crossbow
				|| owner.belongings.secondWep() instanceof Crossbow)){
			speed *= 3f;
			
		} else if (item instanceof SpiritBow.SpiritArrow
				|| item instanceof ScorpioSprite.ScorpioShot
				|| item instanceof TenguSprite.TenguShuriken){
			speed *= 1.5f;
		}
		
		/*
		at this moment we have
		1) source position (from)
		2) target position (to)
		3) speed
		4) angle
		5) angular speed
		6) horizontal flipping flag
		7) item

		we can't use "time" because real (visual) coords may be broken
		we can call a "callback" in the next frame to avoid adding the next visual element to this network packet
		 */
		SendData.packAndSendActionForAll(new MissileSpriteVisualAction(
				anchorFrom,
				anchorTo,
				speed,
				angularSpeed,
				angle,
				flipHorizontal,
				item
		));
		
		PosTweener tweener = new PosTweener( this, to, d.length() / speed );
		tweener.listener = this;
		parent.add( tweener );
	}

	@Override
	public void onComplete( Tweener tweener ) {
		kill();
		if (callback != null) {
			callback.call();
		}
	}

	public static class Anchor {
		public static final String TYPE_CELL = "cell";
		public static final String TYPE_CHAR = "char";

		public final String type;
		public final Integer cell;
		public final Integer charId;

		private final transient CharSprite sprite;

		private Anchor(String type, Integer cell, Integer charId, CharSprite sprite) {
			this.type = type;
			this.cell = cell;
			this.charId = charId;
			this.sprite = sprite;
		}

		public static Anchor cell(int cell) {
			return new Anchor(TYPE_CELL, cell, null, null);
		}

		public static Anchor character(CharSprite sprite) {
			return new Anchor(TYPE_CHAR, null, sprite.ch.id(), sprite);
		}

		public PointF toPointF() {
			if (TYPE_CELL.equals(type)) {
				return DungeonTilemap.raisedTileCenterToWorld(cell);
			} else if (TYPE_CHAR.equals(type)) {
				if (sprite != null) {
					return sprite.center();
				}
				Char ch = (Char) Actor.findById(charId);
				if (ch != null && ch.getSprite() != null) {
					return ch.getSprite().center();
				}
				if (ch != null) {
					return DungeonTilemap.raisedTileCenterToWorld(ch.pos);
				}
				throw new IllegalStateException("No char with id " + charId);
			}
			throw new IllegalStateException("Invalid anchor type");
		}
	}
}
