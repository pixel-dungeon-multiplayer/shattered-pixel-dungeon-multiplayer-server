/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.scenes;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.heroes;

import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.LostBackpack;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SpecialRoom;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import io.github.pixeldungeonmultiplayer.shattered.server.network.SendData;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.InterlevelSceneAction;
import com.shatteredpixel.shatteredpixeldungeon.ui.GameLog;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.watabou.noosa.*;
import com.watabou.utils.BArray;
import com.watabou.utils.DeviceCompat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class InterLevelSceneServer extends Scene {

	public enum Mode {
		DESCEND, ASCEND, CONTINUE, RESURRECT, RETURN, FALL, RESET, NONE
	}
	public static Mode mode;

	public static LevelTransition curTransition = null;
	public static int returnDepth;
	public static int returnBranch;
	public static int returnPos;

	public static boolean fallIntoPit;
	
	private enum Phase {
		FADE_IN, STATIC, FADE_OUT
	}
	private Phase phase;
	
	private Thread thread = null;
	private static Exception error = null;
	public static int lastRegion = -1;
	public static Hero hero;

	public static void create(Hero hero){
		InterLevelSceneServer.hero = hero;
		Game.switchScene(InterLevelSceneServer.class);
	}
	@Override
	public void create() {
        String loadingAsset;
		int loadingDepth;
		QuickSlotButton.lastTarget = null;
		@SuppressWarnings("unused")
		final float scrollSpeed;

		InterlevelSceneAction.FadeTime fadeTime;
		fadeTime = InterlevelSceneAction.FadeTime.NORM_FADE;
		switch (mode){
			default:
				loadingDepth = Dungeon.depth;
				scrollSpeed = 0;
				break;
			case CONTINUE:
				loadingDepth = GamesInProgress.check(GamesInProgress.curSlot).depth;
				scrollSpeed = 5;
				break;
			case DESCEND:
				if (heroes == null){
					loadingDepth = 1;
					fadeTime = InterlevelSceneAction.FadeTime.SLOW_FADE;
				} else {
					if (curTransition != null)  loadingDepth = curTransition.destDepth;
					else                        loadingDepth = Dungeon.depth+1;
					if (Statistics.deepestFloor >= loadingDepth) {
						fadeTime = InterlevelSceneAction.FadeTime.FAST_FADE;
					} else if (loadingDepth % 5 == 1) {
						fadeTime = InterlevelSceneAction.FadeTime.SLOW_FADE;
					}
				}
				scrollSpeed = 5;
				break;
			case FALL:
				loadingDepth = Dungeon.depth+1;
				scrollSpeed = 50;
				break;
			case ASCEND:
				fadeTime = InterlevelSceneAction.FadeTime.FAST_FADE;
				if (curTransition != null)  loadingDepth = curTransition.destDepth;
				else                        loadingDepth = Dungeon.depth-1;
				scrollSpeed = -5;
				break;
			case RETURN:
				loadingDepth = returnDepth;
				scrollSpeed = returnDepth > Dungeon.depth ? 15 : -15;
				break;
		}

		//flush the texture cache whenever moving between regions, helps reduce memory load
		//These seem to cause some issues
//		int region = (int)Math.ceil(loadingDepth / 5f);
//		if (region != lastRegion){
//			TextureCache.clear();
//			lastRegion = region;
//		}
		//TODO: check this
//		if      (lastRegion == 1)    loadingAsset = Assets.Interfaces.LOADING_SEWERS;
//		else if (lastRegion == 2)    loadingAsset = Assets.Interfaces.LOADING_PRISON;
//		else if (lastRegion == 3)    loadingAsset = Assets.Interfaces.LOADING_CAVES;
//		else if (lastRegion == 4)    loadingAsset = Assets.Interfaces.LOADING_CITY;
//		else if (lastRegion == 5)    loadingAsset = Assets.Interfaces.LOADING_HALLS;
//		else
			loadingAsset = Assets.Interfaces.SHADOW;

		phase = Phase.FADE_IN;
		// We do not send the message and the scrolling speed
		// to allow the client to determine them independently according to the current mode
		// the background scale is determined by the texture pack
		InterlevelSceneAction paramsObject = new InterlevelSceneAction(mode, loadingAsset, fadeTime, null, null, true);
		SendData.sendInterLevelSceneForAll(paramsObject);

		if (thread == null) {
			thread = new Thread() {
				@Override
				public void run() {
					
					try {

						Actor.fixTime();

						switch (mode) {
							case DESCEND:
								descend();
								break;
							case ASCEND:
								ascend();
								break;
							case CONTINUE:
								restore();
								break;
							case RESURRECT:
								resurrect(hero);
								break;
							case RETURN:
								returnTo();
								break;
							case FALL:
								fall();
								break;
							case RESET:
								reset();
								break;
						}
						
					} catch (Exception e) {
						
						error = e;
						
					}

					synchronized (thread) {
						if (phase == Phase.STATIC && error == null) {
							phase = Phase.FADE_OUT;
						}
					}
				}
			};
			thread.start();
		}
        try {
            thread.join();
        } catch (InterruptedException e) {

		}
		handleError();
		for (int i = 0; i < heroes.length; i++) {
			SendData.sendInterLevelSceneFadeOut(i);
		}
		Game.switchScene(GameScene.class);
	}

	public void handleError() {

		if (error == null){
			/*if (phase != Phase.FADE_OUT){
				error = new RuntimeException("InterlevelScene is not loaded fully");
			}*/
		}
		if (error != null) {
			LocalizedString errorMsg;
			if (error instanceof FileNotFoundException)
				errorMsg = Messages.get(this, "file_not_found");
			else if (error instanceof IOException) errorMsg = Messages.get(this, "io_error");
			else if (error.getMessage() != null &&
					error.getMessage().equals("old save"))
				errorMsg = Messages.get(this, "io_error");

			else throw new RuntimeException("fatal error occurred while moving between floors. " +
						"Seed:" + Dungeon.seed + " depth:" + Dungeon.depth, error);

			/*add(new WndError(errorMsg, null) {
				public void onBackPressed() {
					super.onBackPressed();
					Game.switchScene(StartScene.class);
				}
			});*/
			thread = null;
			error = null;
			throw new RuntimeException(String.format("Fatal error: %s", errorMsg), error);
		}
	}

	private void descend() throws IOException {

		if (Dungeon.heroes == null) {
			Mob.clearHeldAllies();
			Dungeon.init();
			GameLog.wipe();

			//When debugging, we may start a game at a later depth to quickly test something
			// if this happens, the games quickly generates all prior levels on branch 0 first,
			// which ensures levelgen consistency with a regular game that was played to that depth.
			if (DeviceCompat.isDebug()){
				int trueDepth = Dungeon.depth;
				int trueBranch = Dungeon.branch;
				for (int i = 1; i < trueDepth + (trueBranch == 0 ? 0 : 1); i++){
					if (!Dungeon.levelHasBeenGenerated(i, 0)){
						Dungeon.depth = i;
						Dungeon.branch = 0;
						Dungeon.level = Dungeon.newLevel();
						Dungeon.saveLevel();
					}
				}
				Dungeon.depth = trueDepth;
				Dungeon.branch = trueBranch;
			}

			Level level = Dungeon.newLevel();
			Dungeon.switchLevel( level, -1 );
		} else {
			Mob.holdAlliesForAllHeroes( Dungeon.level );
			Dungeon.saveAll();

			Level level;
			Dungeon.depth = curTransition.destDepth;
			Dungeon.branch = curTransition.destBranch;

			if (Dungeon.levelHasBeenGenerated(Dungeon.depth, Dungeon.branch)) {
				level = Dungeon.loadLevel();
			} else {
				level = Dungeon.newLevel();
			}

			LevelTransition destTransition = level.getTransition(curTransition.destType);
			curTransition = null;
			Dungeon.switchLevel( level, destTransition.cell() );
		}

	}

	//TODO atm falling always just increments depth by 1, do we eventually want to roll it into the transition system?
	private void fall() throws IOException {
		
		Mob.holdAlliesForAllHeroes( Dungeon.level );
		for (Hero hero: Dungeon.heroes) {
			if (hero != null) {
				Buff.affect(hero, Chasm.Falling.class);
			}
		}
		Dungeon.saveAll();

		Level level;
		Dungeon.depth++;
		if (Dungeon.levelHasBeenGenerated(Dungeon.depth, Dungeon.branch)) {
			level = Dungeon.loadLevel();
		} else {
			level = Dungeon.newLevel();
		}
		Dungeon.switchLevel( level, level.fallCell( fallIntoPit ));
	}

	private void ascend() throws IOException {
		Mob.holdAlliesForAllHeroes( Dungeon.level );
		Dungeon.saveAll();

		Level level;
		Dungeon.depth = curTransition.destDepth;
		Dungeon.branch = curTransition.destBranch;

		if (Dungeon.levelHasBeenGenerated(Dungeon.depth, Dungeon.branch)) {
			level = Dungeon.loadLevel();
		} else {
			level = Dungeon.newLevel();
		}

		LevelTransition destTransition = level.getTransition(curTransition.destType);
		curTransition = null;
		Dungeon.switchLevel( level, destTransition.cell() );
	}
	
	private void returnTo() throws IOException {
		Mob.holdAlliesForAllHeroes( Dungeon.level );
		Dungeon.saveAll();

		Level level;
		Dungeon.depth = returnDepth;
		Dungeon.branch = returnBranch;
		if (Dungeon.levelHasBeenGenerated(Dungeon.depth, Dungeon.branch)) {
			level = Dungeon.loadLevel();
		} else {
			level = Dungeon.newLevel();
		}

		Dungeon.switchLevel( level, returnPos );
	}
	
	private void restore() throws IOException {
		
		Mob.clearHeldAllies();

		GameLog.wipe();

		Dungeon.loadGame( GamesInProgress.curSlot, true );
		if (Dungeon.depth == -1) {
			Dungeon.depth = Statistics.deepestFloor;
			Dungeon.switchLevel( Dungeon.loadLevel(), -1 );
		} else {
			Level level = Dungeon.loadLevel();
			Dungeon.switchLevelForAll( level, -3 );
		}
	}
	
	private void resurrect(Hero hero) {
		
		Mob.holdAlliesForAllHeroes( Dungeon.level );

		Level level;
		if (Dungeon.level.locked) {
			ArrayList<Item> preservedItems = Dungeon.level.getItemsToPreserveFromSealedResurrect();

			hero.resurrect();
			level = Dungeon.newLevel();
			hero.pos = level.randomRespawnCell(hero);
			if (hero.pos == -1) hero.pos = level.entrance();

			for (Item i : preservedItems){
				int pos = level.randomRespawnCell(null);
				if (pos == -1) pos = level.entrance();
				level.drop(i, pos);
			}
			int pos = level.randomRespawnCell(null);
			if (pos == -1) pos = level.entrance();
			level.drop(new LostBackpack(), pos);

		} else {
			level = Dungeon.level;
			BArray.setFalse(hero.fieldOfView);
			BArray.setFalse(level.visited);
			BArray.setFalse(level.mapped);
			int invPos = hero.pos;
			int tries = 0;
			do {
				hero.pos = level.randomRespawnCell(hero);
				tries++;

			//prevents spawning on traps or plants, prefers farther locations first
			} while (level.traps.get(hero.pos) != null
					|| (level.plants.get(hero.pos) != null && tries < 500)
					|| level.trueDistance(invPos, hero.pos) <= 30 - (tries/10));

			//directly trample grass
			if (level.map[hero.pos] == Terrain.HIGH_GRASS || level.map[hero.pos] == Terrain.FURROWED_GRASS){
				level.map[hero.pos] = Terrain.GRASS;
			}
			hero.resurrect();
			level.drop(new LostBackpack(), invPos);
		}

		Dungeon.switchLevel( level, hero.pos );
	}

	private void reset() throws IOException {
		
		Mob.holdAlliesForAllHeroes( Dungeon.level );

		SpecialRoom.resetPitRoom(Dungeon.depth+1);

		Level level = Dungeon.newLevel();
		Dungeon.switchLevel( level, level.entrance() );
	}

	@Override
	public void destroy() {
		hero = null;
		super.destroy();
	}

}
