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

package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MossyClump extends Trinket {

	{
		image = ItemSpriteSheet.MOSSY_CLUMP;
	}

	@Override
	protected int upgradeEnergyCost() {
		//6 -> 10(16) -> 15(31) -> 20(51)
		return 10+5*level();
	}

	@Override
	public LocalizedString statsDesc() {
		if (isIdentified()){
			return Messages.get(this, "stats_desc", (int)(100*overrideNormalLevelChance(buffedLvl())));
		} else {
			return Messages.get(this, "typical_stats_desc", (int)(100*overrideNormalLevelChance(0)));
		}
	}

	public static float overrideNormalLevelChance() {
		// highest level mossy clump gets to override the level
		float currentOverrideNormalLevelChance = 0f;
		for (Hero hero : Dungeon.heroes) {
			if (hero != null) {
				float overrideNormalLevelChance = overrideNormalLevelChance(trinketLevel(MossyClump.class, hero));
				if (overrideNormalLevelChance > currentOverrideNormalLevelChance) {
					currentOverrideNormalLevelChance = overrideNormalLevelChance;

				}			}
		}
		return currentOverrideNormalLevelChance;
	}

	public static float overrideNormalLevelChance( int level ){
		if (level == -1){
			return 0f;
		} else {
			return 0.25f + 0.25f*level;
		}
	}

	//true for grass, false for water
	//ensures a little consistency of RNG
	private ArrayList<Boolean> levelFeels = new ArrayList<>();
	private int shuffles = 0;

	public static Level.Feeling getNextFeeling() {
		ArrayList<Boolean> feelings = new ArrayList<>();
		for (Hero hero : Dungeon.heroes) {
			if(hero != null) {
			MossyClump clump = hero.belongings.getItem(MossyClump.class);
			if (clump == null) {
				continue;
			}
			if (feelings.isEmpty()) {
				Random.pushGenerator(Dungeon.seed + 1);
				feelings.add(true);
				feelings.add(true);
				feelings.add(false);
				feelings.add(false);
				feelings.add(false);
				feelings.add(false);
				for (int i = 0; i <= clump.shuffles; i++) {
					Random.shuffle(clump.levelFeels);
				}
				clump.shuffles++;
				Random.popGenerator();
			}
		}
	}
		return feelings.remove(0) ? Level.Feeling.GRASS : Level.Feeling.WATER;
	}


	private static final String FEELS = "feels";
	private static final String SHUFFLES = "shuffles";

	@Override
	public void storeInBundle(@NotNull Bundle bundle) {
		super.storeInBundle(bundle);
		if (!levelFeels.isEmpty()){
			boolean[] storeFeels = new boolean[levelFeels.size()];
			for (int i = 0; i < storeFeels.length; i++){
				storeFeels[i] = levelFeels.get(i);
			}
			bundle.put(FEELS, storeFeels);
		}
		bundle.put( SHUFFLES, shuffles );
	}

	@Override
	public void restoreFromBundle(@NotNull Bundle bundle) {
		super.restoreFromBundle(bundle);
		levelFeels.clear();
		if (bundle.contains(FEELS)){
			for (boolean b : bundle.getBooleanArray(FEELS)){
				levelFeels.add(b);
			}
		}
		shuffles = bundle.getInt( SHUFFLES );
	}
}
