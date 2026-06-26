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

package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.journal.Bestiary;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import io.github.pixeldungeonmultiplayer.shattered.server.network.SendData;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import io.github.pixeldungeonmultiplayer.shattered.server.noosa.audio.Sample;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

public abstract class Trap implements Bundlable {

	//trap colors
	public static final int RED     = 0;
	public static final int ORANGE  = 1;
	public static final int YELLOW  = 2;
	public static final int GREEN   = 3;
	public static final int TEAL    = 4;
	public static final int VIOLET  = 5;
	public static final int WHITE   = 6;
	public static final int GREY    = 7;
	public static final int BLACK   = 8;

	//trap shapes
	public static final int DOTS        = 0;
	public static final int WAVES       = 1;
	public static final int GRILL       = 2;
	public static final int STARS       = 3;
	public static final int DIAMOND     = 4;
	public static final int CROSSHAIR   = 5;
	public static final int LARGE_DOT   = 6;

	public int color; //used for visual
	public int shape; //used for visual

	public int pos; //used for visual
	public boolean reclaimed = false; //if this trap was spawned by reclaim trap

	public boolean visible; //we remove trap from client if it is invisible
	public boolean active = true; //used for visual
	public boolean disarmedByActivation = true;
	
	public boolean canBeHidden = true;
	public boolean canBeSearched = true;

	public boolean avoidsHallways = false; //whether this trap should avoid being placed in hallways

	public Trap set(int pos){
		// Don't send this. This is a builder method for `pos`, and it's used to execute a trap
		// effect without changing the tiles. Look for `level.traps.put()` instead.
		this.pos = pos;
		return this;
	}

	public Trap reveal() {
		visible = true;
		GameScene.updateMap(pos);
		if (Dungeon.level != null) {
			SendData.sendTrap(this.pos);
		}
		return this;
	}

	public Trap hide() {
		if (canBeHidden) {
			visible = false;
			GameScene.updateMap(pos);
			return this;
		} else {
			return reveal();
		}
	}

	public void trigger() {
		if (isActive()) {
			if (Dungeon.visibleforAnyHero(pos)) {
				Sample.INSTANCE.play(Assets.Sounds.TRAP);
			}
			if (disarmedByActivation) disarm();
			Dungeon.level.discover(pos);
			Bestiary.setSeen(getClass());
			Bestiary.countEncounter(getClass());
			activate();
		}
	}

	public abstract void activate();

	public void disarm(){
		setActive(false);
		Dungeon.level.disarmTrap(pos);
	}

	//returns the depth value the trap should use for determining its power
	//If the trap is part of the level, it should use the true depth
	//If it's not part of the level (e.g. effect from reclaim trap), use scaling depth
	protected int scalingDepth(){
		return (reclaimed || Dungeon.level.traps.get(pos) != this) ? Dungeon.scalingDepth() : Dungeon.depth;
	}

	public LocalizedString name(){
		return Messages.get(this, "name");
	}

	public LocalizedString desc() {
		return Messages.get(this, "desc");
	}

	private static final String POS	= "pos";
	private static final String VISIBLE	= "visible";
	private static final String ACTIVE = "active";

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		pos = bundle.getInt( POS );
		visible = bundle.getBoolean( VISIBLE );
		if (bundle.contains(ACTIVE)){
			setActive(bundle.getBoolean(ACTIVE));
		}
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		bundle.put( POS, pos );
		bundle.put( VISIBLE, visible );
		bundle.put( ACTIVE, isActive());
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
		if (Dungeon.level!= null && Dungeon.level.traps != null && Dungeon.level.traps.get(pos, null) == this) {
			SendData.sendTrap(this.pos);
		}
	}

	//this buff is used to keep track of hazards recently affecting a character
	public static class HazardAssistTracker extends FlavourBuff{
		public static final float DURATION = 50f;
	}
}
