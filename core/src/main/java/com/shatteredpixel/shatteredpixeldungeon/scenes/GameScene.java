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

import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import io.github.pixeldungeonmultiplayer.shattered.server.ui.Banner;
import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DemonSpawner;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Ghoul;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Snake;
import com.shatteredpixel.shatteredpixeldungeon.effects.BannerSprites;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.EmoIcon;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.items.Ankh;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Honeypot;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.items.journal.Guidebook;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.DimensionalSundial;
import com.shatteredpixel.shatteredpixeldungeon.journal.Bestiary;
import com.shatteredpixel.shatteredpixeldungeon.journal.Document;
import com.shatteredpixel.shatteredpixeldungeon.journal.Journal;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import io.github.pixeldungeonmultiplayer.shattered.server.network.SendData;
import io.github.pixeldungeonmultiplayer.shattered.server.network.Server;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DiscardedItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTerrainTilemap;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTileSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonWallsTilemap;
import com.shatteredpixel.shatteredpixeldungeon.tiles.GridTileMap;
import com.shatteredpixel.shatteredpixeldungeon.tiles.RaisedTerrainTilemap;
import com.shatteredpixel.shatteredpixeldungeon.tiles.TerrainFeaturesTilemap;
import com.shatteredpixel.shatteredpixeldungeon.tiles.WallBlockingTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.CharHealthIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.GameLog;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.MenuPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.ResumeIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.StatusPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Tag;
import com.shatteredpixel.shatteredpixeldungeon.ui.TargetHealthIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.Toast;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndHero;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoCell;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoMob;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoPlant;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoTrap;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndResurrect;
import com.watabou.glwrap.Blending;
import com.watabou.input.ControllerHandler;
import com.watabou.input.KeyBindings;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;
import com.watabou.noosa.NoosaScript;
import com.watabou.noosa.NoosaScriptNoLighting;
import com.watabou.noosa.SkinnedBlock;
import com.watabou.noosa.Visual;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.*;
import io.github.pixeldungeonmultiplayer.shattered.server.noosa.audio.Sample;
import com.shatteredpixel.shatteredpixeldungeon.particles.Emitter;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.GameMath;
import com.watabou.utils.Point;
import com.watabou.utils.Random;
import com.watabou.utils.RectF;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

//FIXME
//FIXME
//FIXME
public class GameScene extends PixelScene {

	static GameScene scene;

	private SkinnedBlock water;
	public static DungeonTerrainTilemap tiles;
	private GridTileMap visualGrid;
	private TerrainFeaturesTilemap terrainFeatures;
	private RaisedTerrainTilemap raisedTerrain;
	private DungeonWallsTilemap walls;
	private WallBlockingTilemap wallBlocking;
	private HeroSprite hero;

	private MenuPane menu;

	private GameLog log;

	private Group terrain;
	private Group customTiles;
	private Group levelVisuals;
	private Group levelWallVisuals;
	private Group customWalls;
	private Group ripples;
	private Group plants;
	private Group traps;
	private Group heaps;
	private Group mobs;
	private Group floorEmitters;
	private Group emitters;
	private Group effects;
	private Group gases;
	private Group spells;
	private Group statuses;
	private Group emoicons;
	private Group overFogEffects;
	private Group healthIndicators;

	private Toast prompt;
	private ResumeIndicator resume;

	{
		inGameScene = true;
	}

	public static void setUpdateItemDisplays(Hero hero) {
		if (hero != null) {
			GameScene.updateItemDisplays.add(HeroHelp.getHeroID(hero));
		} else {

		}
	}

	@Override
	public void create() {

		if (Dungeon.heroes == null || Dungeon.level == null) {
			ShatteredPixelDungeon.switchNoFade(TitleScene.class);
			return;
		}

		Dungeon.level.playLevelMusic();

		//SPDSettings.lastClass(Dungeon.heroes.heroClass.ordinal());

		super.create();
		Camera.main.zoom(GameMath.gate(minZoom, defaultZoom + SPDSettings.zoom(), maxZoom));
		Camera.main.edgeScroll.set(1);

		switch (SPDSettings.cameraFollow()) {
			case 4:
			default:
				Camera.main.setFollowDeadzone(0);
				break;
			case 3:
				Camera.main.setFollowDeadzone(0.2f);
				break;
			case 2:
				Camera.main.setFollowDeadzone(0.5f);
				break;
			case 1:
				Camera.main.setFollowDeadzone(0.9f);
				break;
		}

		scene = this;

		terrain = new Group();
		add(terrain);

		water = new SkinnedBlock(
				Dungeon.level.width() * DungeonTilemap.SIZE,
				Dungeon.level.height() * DungeonTilemap.SIZE,
				Dungeon.level.waterTex()) {

			@Override
			protected NoosaScript script() {
				return NoosaScriptNoLighting.get();
			}

			@Override
			public void draw() {
				//water has no alpha component, this improves performance
				Blending.disable();
				super.draw();
				Blending.enable();
			}
		};
		water.autoAdjust = true;
		terrain.add(water);

		ripples = new Group();
		terrain.add(ripples);

		DungeonTileSheet.setupVariance(Dungeon.level.map.length, Dungeon.seedCurDepth());

		tiles = new DungeonTerrainTilemap();
		terrain.add(tiles);

		customTiles = new Group();
		terrain.add(customTiles);

		for (CustomTilemap visual : Dungeon.level.customTiles) {
			addCustomTile(visual);
			SendData.sendLateLiveStateActionForAll(new CustomTilemapActions.Add(false, customTilesList.indexOf(visual), visual));
		}

		visualGrid = new GridTileMap();
		terrain.add(visualGrid);

		terrainFeatures = new TerrainFeaturesTilemap(Dungeon.level.plants, Dungeon.level.traps);
		terrain.add(terrainFeatures);

		//levelVisuals = Dungeon.level.addVisuals();
		// add(levelVisuals);

		floorEmitters = new Group();
		add(floorEmitters);

		heaps = new Group();
		add(heaps);

		for (Heap heap : Dungeon.level.heaps.valueList()) {
			addHeapSprite(heap);
		}

		emitters = new Group();
		effects = new Group();
		healthIndicators = new Group();
		emoicons = new Group();
		overFogEffects = new Group();

		mobs = new Group();
		add(mobs);
		for (Hero notSprite : Dungeon.heroes) {
			if (notSprite != null) {
				hero = new HeroSprite(notSprite);
				hero.place(notSprite.pos);
				hero.updateArmor();
				mobs.add(hero);
			}
		}

		for (Mob mob : Dungeon.level.mobs) {
			addMobSprite(mob);
		}

		raisedTerrain = new RaisedTerrainTilemap();
		add(raisedTerrain);

		walls = new DungeonWallsTilemap();
		add(walls);

		customWalls = new Group();
		add(customWalls);

		for (CustomTilemap visual : Dungeon.level.customWalls) {
			addCustomWall(visual);
			SendData.sendLateLiveStateActionForAll(new CustomTilemapActions.Add(true, customWallsList.indexOf(visual), visual));
		}

		levelWallVisuals = Dungeon.level.addWallVisuals();
		add(levelWallVisuals);

		wallBlocking = new WallBlockingTilemap();
		add(wallBlocking);

		add(emitters);
		add(effects);

		gases = new Group();
		add(gases);

		for (Blob blob : Dungeon.level.blobs.values()) {
			blob.emitter = null;
			addBlobSprite(blob);
		}

		spells = new Group();
		add(spells);

		add(overFogEffects);

		statuses = new Group();
		add(statuses);

		add(healthIndicators);
		//always appears ontop of other health indicators
		add(new TargetHealthIndicator());

		add(emoicons);

		int uiSize = SPDSettings.interfaceSize();

		menu = new MenuPane();
		menu.camera = uiCamera;
		menu.setPos(uiCamera.width - MenuPane.WIDTH, uiSize > 0 ? 0 : 1);
		add(menu);

		resume = new ResumeIndicator();
		resume.camera = uiCamera;
		add(resume);

		log = new GameLog();
		log.camera = uiCamera;
		log.newLine();
		add(log);

		layoutTags();

		switch (InterLevelSceneServer.mode) {
			case RESURRECT:
				for (Hero hero : Dungeon.heroes) {
					if (hero != null) {
						Sample.INSTANCE.play(Assets.Sounds.TELEPORT);
						ScrollOfTeleportation.appearVFX(hero);
						SpellSprite.show(hero, SpellSprite.ANKH);
						new Flare(5, 16).color(0xFFFF00, true).show(hero.getSprite(), 4f);
					}
				}
				break;
			case RETURN:
				for (Hero hero : Dungeon.heroes) {
					if (hero != null) {
						ScrollOfTeleportation.appearVFX(hero);
					}
				}
				break;
			case DESCEND:
			case FALL:
				Badges.validateNoKilling();
				break;
		}

		ArrayList<Item> dropped = Dungeon.droppedItems.get(Dungeon.depth);
		if (dropped != null) {
			for (Item item : dropped) {
				int pos = Dungeon.level.randomRespawnCell(null);
				if (pos == -1) pos = Dungeon.level.entrance();
				if (item instanceof Potion) {
					((Potion) item).shatter(pos, null);
				} else if (item instanceof Plant.Seed && !Dungeon.isChallenged(Challenges.NO_HERBALISM)) {
					Dungeon.level.plant((Plant.Seed) item, pos);
				} else if (item instanceof Honeypot) {
					Dungeon.level.drop(((Honeypot) item).shatter(null, pos), pos);
				} else {
					Dungeon.level.drop(item, pos);
				}
			}
			Dungeon.droppedItems.remove(Dungeon.depth);
		}
		for (Hero hero : Dungeon.heroes) {
			if (hero != null) {
				hero.next();

			}
		}
		/*
		switch (InterLevelSceneServer.mode) {
			case FALL:
			case DESCEND:
			case CONTINUE:
				Camera.main.snapTo(hero.center().x, hero.center().y - DungeonTilemap.SIZE * (defaultZoom / Camera.main.zoom));
				break;
			case ASCEND:
				Camera.main.snapTo(hero.center().x, hero.center().y + DungeonTilemap.SIZE * (defaultZoom / Camera.main.zoom));
				break;
			default:
				Camera.main.snapTo(hero.center().x, hero.center().y);
		}
		Camera.main.panTo(hero.center(), 2.5f);
*/
		if (InterLevelSceneServer.mode != InterLevelSceneServer.Mode.NONE) {
			if (Dungeon.depth == Statistics.deepestFloor
					&& (InterLevelSceneServer.mode == InterLevelSceneServer.Mode.DESCEND || InterLevelSceneServer.mode == InterLevelSceneServer.Mode.FALL)) {
				GLog.h(Messages.get(this, "descend", Dungeon.depth));
				Sample.INSTANCE.play(Assets.Sounds.DESCEND);

				for (Char ch : Actor.chars()) {
					if (ch instanceof DriedRose.GhostHero) {
						((DriedRose.GhostHero) ch).sayAppeared();
					}
				}

				int spawnersAbove = Statistics.spawnersAlive;
				if (spawnersAbove > 0 && Dungeon.depth <= 25) {
					for (Mob m : Dungeon.level.mobs) {
						if (m instanceof DemonSpawner && ((DemonSpawner) m).spawnRecorded) {
							spawnersAbove--;
						}
					}

					if (spawnersAbove > 0) {
						if (Dungeon.bossLevel()) {
							GLog.n(Messages.get(this, "spawner_warn_final"));
						} else {
							GLog.n(Messages.get(this, "spawner_warn"));
						}
					}
				}

			} else if (InterLevelSceneServer.mode == InterLevelSceneServer.Mode.RESET) {
				GLog.h(Messages.get(this, "warp"));
			} else if (InterLevelSceneServer.mode == InterLevelSceneServer.Mode.RESURRECT) {
				GLog.h(Messages.get(this, "resurrect", Dungeon.depth));
			} else {
				GLog.h(Messages.get(this, "return", Dungeon.depth));
			}
			for (Hero hero : Dungeon.heroes) {
				if (hero != null) {
					if (hero.hasTalent(Talent.ROGUES_FORESIGHT)
							&& Dungeon.level instanceof RegularLevel && Dungeon.branch == 0) {
						int reqSecrets = Dungeon.level.feeling == Level.Feeling.SECRETS ? 2 : 1;
						for (Room r : ((RegularLevel) Dungeon.level).rooms()) {
							if (r instanceof SecretRoom) reqSecrets--;
						}

						//75%/100% chance, use level's seed so that we get the same result for the same level
						//offset seed slightly to avoid output patterns
						Random.pushGenerator(Dungeon.seedCurDepth() + 1);
						if (reqSecrets <= 0 && Random.Int(4) < 2 + hero.pointsInTalent(Talent.ROGUES_FORESIGHT)) {
							GLog.p(Messages.get(this, "secret_hint"));
						}
						Random.popGenerator();
					}

					boolean unspentTalents = false;
					for (int i = 1; i <= hero.talents.size(); i++) {
						if (hero.talentPointsAvailable(i) > 0) {
							unspentTalents = true;
							break;
						}
					}
					if (unspentTalents) {
						GLog.newLine();
						GLog.w(Messages.get(hero, "unspent"));
						StatusPane.talentBlink = 10f;
						WndHero.lastIdx = 1;
					}
				}
			}
			switch (Dungeon.level.feeling) {
				case CHASM:
					GLog.w(Dungeon.level.feeling.desc());
					Notes.add(Notes.Landmark.CHASM_FLOOR);
					break;
				case WATER:
					GLog.w(Dungeon.level.feeling.desc());
					Notes.add(Notes.Landmark.WATER_FLOOR);
					break;
				case GRASS:
					GLog.w(Dungeon.level.feeling.desc());
					Notes.add(Notes.Landmark.GRASS_FLOOR);
					break;
				case DARK:
					GLog.w(Dungeon.level.feeling.desc());
					Notes.add(Notes.Landmark.DARK_FLOOR);
					break;
				case LARGE:
					GLog.w(Dungeon.level.feeling.desc());
					Notes.add(Notes.Landmark.LARGE_FLOOR);
					break;
				case TRAPS:
					GLog.w(Dungeon.level.feeling.desc());
					Notes.add(Notes.Landmark.TRAPS_FLOOR);
					break;
				case SECRETS:
					GLog.w(Dungeon.level.feeling.desc());
					Notes.add(Notes.Landmark.SECRETS_FLOOR);
					break;
			}

			for (Mob mob : Dungeon.level.mobs) {
				if (!mob.buffs(ChampionEnemy.class).isEmpty()) {
					GLog.w(Messages.get(ChampionEnemy.class, "warn"));
				}
			}
			for (Hero hero : Dungeon.heroes) {
				if (hero == null) continue;
				if (hero.buff(AscensionChallenge.class) != null) {
					hero.buff(AscensionChallenge.class).saySwitch();
				}
			}
			DimensionalSundial.sundialWarned = true;
			if (DimensionalSundial.spawnMultiplierAtCurrentTime() > 1) {
				GLog.w(Messages.get(DimensionalSundial.class, "warning"));
			} else {
				DimensionalSundial.sundialWarned = false;
			}

			InterLevelSceneServer.mode = InterLevelSceneServer.Mode.NONE;


		}

		//Tutorial
		if (SPDSettings.intro()) {

			if (Document.ADVENTURERS_GUIDE.isPageFound(Document.GUIDE_INTRO)) {
				//GameScene.flashForDocument(Document.ADVENTURERS_GUIDE, Document.GUIDE_INTRO);
			} else if (ControllerHandler.isControllerConnected()) {
				GameLog.wipe();
				GLog.p(Messages.get(GameScene.class, "tutorial_move_controller"));
			} else if (SPDSettings.interfaceSize() == 0) {
				GameLog.wipe();
				GLog.p(Messages.get(GameScene.class, "tutorial_move_mobile"));
			} else {
				GameLog.wipe();
				GLog.p(Messages.get(GameScene.class, "tutorial_move_desktop"));
			}
		}

		if (!SPDSettings.intro() &&
				Rankings.INSTANCE.totalNumber > 0 &&
				!Document.ADVENTURERS_GUIDE.isPageRead(Document.GUIDE_DIEING)) {
		//	GameScene.flashForDocument(Document.ADVENTURERS_GUIDE, Document.GUIDE_DIEING);
		}

//		TrinketCatalyst cata = Dungeon.hero.belongings.getItem(TrinketCatalyst.class);
//		if (cata != null && cata.hasRolledTrinkets()) {
//			addToFront(new TrinketCatalyst.WndTrinket(cata));
//		}
//
//		if (!invVisible) toggleInvPane();
		fadeIn();

		//re-show WndResurrect if needed
		for (Hero hero : Dungeon.heroes) {
			if (hero != null) {

				if (!hero.isAlive()) {
					//check if hero has an unblessed ankh
					Ankh ankh = null;
					for (Ankh i : hero.belongings.getAllItems(Ankh.class)) {
						if (!i.isBlessed()) {
							ankh = i;
						}
					}
					if (ankh != null && GamesInProgress.gameExists(GamesInProgress.curSlot)) {
						add(new WndResurrect(hero, ankh));
					} else {
						gameOver(hero);
					}
				}

			}
		}
	}

	public void destroy() {

		//tell the actor thread to finish, then wait for it to complete any actions it may be doing.
		if (!waitForActorThread(4500, true)) {
			Throwable t = new Throwable();
			t.setStackTrace(actorThread.getStackTrace());
			throw new RuntimeException("timeout waiting for actor thread! ", t);
		}

		Emitter.freezeEmitters = false;

		scene = null;
		Badges.saveGlobal();
		Journal.saveGlobal();

		super.destroy();
	}

	public static void endActorThread() {
		if (actorThread != null && actorThread.isAlive()) {
			Actor.keepActorThreadAlive = false;
			actorThread.interrupt();
		}
	}

	public boolean waitForActorThread(int msToWait, boolean interrupt) {
		if (actorThread == null || !actorThread.isAlive()) {
			return true;
		}
		synchronized (actorThread) {
			if (interrupt) actorThread.interrupt();
			try {
				actorThread.wait(msToWait);
			} catch (InterruptedException e) {
				ShatteredPixelDungeon.reportException(e);
			}
			return !Actor.processing();
		}
	}

	@Override
	public synchronized void onPause() {
		try {
			//TODO: check this
			waitForActorThread(500, false);
			Dungeon.saveAll();
			Badges.saveGlobal();
			Journal.saveGlobal();
		} catch (IOException e) {
			ShatteredPixelDungeon.reportException(e);
		}
	}

	private static Thread actorThread;

	//sometimes UI changes can be prompted by the actor thread.
	// We queue any removed element destruction, rather than destroying them in the actor thread.
	private ArrayList<Gizmo> toDestroy = new ArrayList<>();

	//the actor thread processes at a maximum of 60 times a second
	//this caps the speed of resting for higher refresh rate displays
	private float notifyDelay = 1 / 60f;

	private static final Set<Integer> updateItemDisplays = new HashSet<>();
	private static final Set<Integer> fullUpdate = new HashSet<>();

	public static boolean tagDisappeared = false;
	public static boolean updateTags = false;
	private static float waterOfs = 0;

	public static boolean shouldProcess = false;

	private void updateItemDisplays() {
		for (int id = 0; id < Dungeon.heroes.length; id++){
			final Hero hero = Dungeon.heroes[id];
			if (hero == null) {
				continue;
			}
			if (updateItemDisplays.contains(id)) {
				updateItemDisplays.remove(id);
				updateItemDisplays(hero);
			}
		}
	}

	private void updateItemDisplays(@NotNull Hero hero) {
		for (Item item: hero.belongings) {
			if (item.isNeedUpdateVisual()) {
				SendData.packAndSendAction(hero, new ItemAction.Update(item));
				item.setNeedUpdateVisual(false);
			}
		}
	}
	@Override
	public synchronized void update() {
		Server.parseActions();
		SendData.updatePendingChat(Game.elapsed);
		lastOffset = null;

		updateItemDisplays();

		if (Dungeon.heroes == null || scene == null) {
			return;
		}

		super.update();

		if (notifyDelay > 0) notifyDelay -= Game.elapsed;

		if (!Emitter.freezeEmitters) {
			waterOfs -= 5 * Game.elapsed;
			water.offsetTo(0, waterOfs);
			waterOfs = water.offsetY(); //re-assign to account for auto adjust
		}
		water.offsetTo(0, waterOfs);
		waterOfs = water.offsetY(); //re-assign to account for auto adjust

		//TODO: check this
		if (!Actor.processing()) {
			if (actorThread == null || !actorThread.isAlive()) {

				actorThread = new Thread() {
					@Override
					public void run() {
						SendData.forceFlushAll();
						if (shouldProcess) {
						Actor.process();
						}
					}
				};

				//if cpu cores are limited, game should prefer drawing the current frame
				if (Runtime.getRuntime().availableProcessors() == 1) {
					actorThread.setPriority(Thread.NORM_PRIORITY - 1);
				}
				actorThread.setName("SHPD Actor Thread");
				Thread.currentThread().setName("SHPD Render Thread");
				Actor.keepActorThreadAlive = true;
				actorThread.start();
			} else if (notifyDelay <= 0f) {
				notifyDelay += 1 / 60f;
				synchronized (actorThread) {
					actorThread.notify();
				}
			}
		}
		for (Hero hero : Dungeon.heroes) {
			if (hero != null) {
				if (hero.isReady() && hero.paralysed == 0) {
					log.newLine();
				}
			}
		}

		if (updateTags) {
			tagResume = resume.visible;

			layoutTags();

		} else if (
				tagResume != resume.visible) {

			boolean tagAppearing =
					(resume.visible && !tagResume);
			tagResume = resume.visible;

			//if a new tag appears, re-layout tags immediately
			//otherwise, wait until the hero acts, so as to not suddenly change their position
			if (tagAppearing) layoutTags();
			else tagDisappeared = true;

		}
		for (Hero hero : Dungeon.heroes) {
			if (hero != null) {
				hero.cellSelector.enable(hero.isReady());
			}
		}

		{
			// We are sending all the events accumulated per frame for the hero we are currently processing.
			// The main sending is done in Actor.update(), whereas this place is created for interactive dialogues
			// that do not change the current actor.
			//todo it may be worth sending events for everyone if we have an interaction of an inactive (!hero.ready()) user.
			Hero current = Actor.getCurrentProcessingReadyHero();
			if (current != null) {
				SendData.forceFlush(current);
			}
		}

		if (!toDestroy.isEmpty()) {
			for (Gizmo g : toDestroy) {
				g.destroy();
			}
			toDestroy.clear();
		}
	}

	private static Point lastOffset = null;

	@Override
	public synchronized Gizmo erase(Gizmo g) {
		Gizmo result = super.erase(g);
		if (result instanceof Window) {
			lastOffset = ((Window) result).getOffset();
		}
		return result;
	}

	private boolean tagResume = false;

	public static void layoutTags() {

		updateTags = false;

		if (scene == null) return;

		//move the camera center up a bit if we're on full UI and it is taking up lots of space
		{
			Camera.main.setCenterOffset(0, 0);
		}
		//Camera.main.panTo(Dungeon.hero.sprite.center(), 5f);

		//primarily for phones displays with notches
		//TODO Android never draws into notch atm, perhaps allow it for center notches?
		RectF insets = DeviceCompat.getSafeInsets();
		insets = insets.scale(1f / uiCamera.zoom);

		boolean tagsOnLeft = SPDSettings.flipTags();
		float tagWidth = Tag.SIZE + (tagsOnLeft ? insets.left : insets.right);
		float tagLeft = tagsOnLeft ? 0 : uiCamera.width - tagWidth;

		float y = 0;
		if (SPDSettings.interfaceSize() == 0) {
			if (tagsOnLeft) {
				scene.log.setRect(tagWidth, y, uiCamera.width - tagWidth - insets.right, 0);
			} else {
				scene.log.setRect(insets.left, y, uiCamera.width - tagWidth - insets.left, 0);
			}
		} else {
			if (tagsOnLeft) {
				scene.log.setRect(tagWidth, y, 160 - tagWidth, 0);
			} else {
				scene.log.setRect(insets.left, y, 160 - insets.left, 0);
			}
		}

		float pos = 0;

		if (scene.tagResume) {
			scene.resume.setRect(tagLeft, pos - Tag.SIZE, tagWidth, Tag.SIZE);
			scene.resume.flip(tagsOnLeft);
		}
	}

	@Override
	protected void onBackPressed() {
	}

	private final ArrayList<CustomTilemap> customTilesList = new ArrayList<CustomTilemap>();
	private final ArrayList<CustomTilemap> customWallsList = new ArrayList<CustomTilemap>();

	public void addCustomTile(CustomTilemap visual) {
		customTiles.add(visual.create());
		customTilesList.add(visual);
		if (!Dungeon.level.customTiles.contains(visual)) {
			Dungeon.level.customTiles.add(visual);
			SendData.sendLateLiveStateActionForAll(new CustomTilemapActions.Add(false, customTilesList.indexOf(visual), visual));
		}
	}

	public void addCustomWall(CustomTilemap visual) {
		customWalls.add(visual.create());
		customWallsList.add(visual);
		if (!Dungeon.level.customWalls.contains(visual)) {
			Dungeon.level.customWalls.add(visual);
			SendData.sendLateLiveStateActionForAll(new CustomTilemapActions.Add(true, customWallsList.indexOf(visual), visual));
		}
	}

	private void addHeapSprite(Heap heap) {
		ItemSprite sprite = heap.sprite = (ItemSprite) heaps.recycle(ItemSprite.class);
		sprite.revive();
		sprite.link(heap);
		heaps.add(sprite);
	}

	private void addDiscardedSprite(Heap heap) {
		heap.sprite = (DiscardedItemSprite) heaps.recycle(DiscardedItemSprite.class);
		heap.sprite.revive();
		heap.sprite.link(heap);
		heaps.add(heap.sprite);
	}

	private void addPlantSprite(Plant plant) {

	}

	private void addTrapSprite(Trap trap) {

	}

	private void addBlobSprite(final Blob gas) {
		if (gas.emitter == null) {
			gases.add(new BlobEmitter(gas));
		}
	}

	private synchronized void addMobSprite(Mob mob) {
		CharSprite sprite = mob.sprite();
		sprite.visible = Dungeon.visibleforAnyHero(mob.pos);
		mobs.add(sprite);
		sprite.link(mob);
		sortMobSprites();
	}

	//ensures that mob sprites are drawn from top to bottom, in case of overlap
	public static void sortMobSprites() {
		if (scene != null) {
			synchronized (scene) {
				scene.mobs.sort(new Comparator() {
					@Override
					public int compare(Object a, Object b) {
						//elements that aren't visual go to the end of the list
						if (a instanceof Visual && b instanceof Visual) {
							return (int) Math.signum((((Visual) a).y + ((Visual) a).height())
									- (((Visual) b).y + ((Visual) b).height()));
						} else if (a instanceof Visual) {
							return -1;
						} else if (b instanceof Visual) {
							return 1;
						} else {
							return 0;
						}
					}
				});
			}
		}
	}

	private synchronized void prompt(LocalizedString text) {

		if (prompt != null) {
			prompt.killAndErase();
			toDestroy.add(prompt);
			prompt = null;
		}

		if (text != null) {
			prompt = new Toast(text) {
				@Override
				protected void onClose() {
					//cancel();
				}
			};
			prompt.camera = uiCamera;
			prompt.setPos((uiCamera.width - prompt.width()) / 2, uiCamera.height - 60);

			add(prompt);
		}
	}


	// -------------------------------------------------------

	public static void add(Plant plant) {
		if (scene != null) {
			scene.addPlantSprite(plant);
		}
	}

	public static void add(Trap trap) {
		if (scene != null) {
			scene.addTrapSprite(trap);
		}
	}

	public static void add(Blob gas) {
		Actor.add(gas);
		if (scene != null) {
			scene.addBlobSprite(gas);
		}
	}

	public static void add(Heap heap) {
		if (scene != null) {
			//heaps that aren't added as part of levelgen don't count for exploration bonus
			heap.autoExplored = true;
			scene.addHeapSprite(heap);
		}
	}

	public static void discard(Heap heap) {
		if (scene != null) {
			scene.addDiscardedSprite(heap);
			SendData.sendActionForAll(new HeapRemoveAction(heap.pos));
		}
	}

	public static void add(Mob mob) {
		Dungeon.level.mobs.add(mob);
		if (scene != null) {
			scene.addMobSprite(mob);
			Actor.add(mob);
		}
	}

	public static void addSprite(Mob mob) {
		scene.addMobSprite(mob);
	}

	public static void add(Mob mob, float delay) {
		Dungeon.level.mobs.add(mob);
		Actor.addDelayed(mob, delay);
		scene.addMobSprite(mob);
	}

	public static void add(EmoIcon icon) {
		scene.emoicons.add(icon);
	}

	public static void add(CharHealthIndicator indicator) {
		if (scene != null) scene.healthIndicators.add(indicator);
	}

	public static void add(CustomTilemap t, boolean wall) {
		if (scene == null) return;
		if (wall) {
			scene.addCustomWall(t);
		} else {
			scene.addCustomTile(t);
		}
	}

	public static void effect(Visual effect) {
		if (scene != null) scene.effects.add(effect);
	}

	public static void effectOverFog(Visual effect) {
		scene.overFogEffects.add(effect);
	}

	public static void ripple(int pos) {
		SendData.sendActionForAll(new RippleVisualAction(pos));
	}

	public static synchronized SpellSprite spellSprite() {
		return (SpellSprite) scene.spells.recycle(SpellSprite.class);
	}

	public static synchronized Emitter emitter() {
		if (scene != null) {
			Emitter emitter = (Emitter) scene.emitters.recycle(Emitter.class);
			emitter.revive();
			return emitter;
		} else {
			return null;
		}
	}

	public static synchronized Emitter floorEmitter() {
		if (scene != null) {
			Emitter emitter = (Emitter) scene.floorEmitters.recycle(Emitter.class);
			emitter.revive();
			return emitter;
		} else {
			return null;
		}
	}

	public static FloatingText status() {
		return scene != null ? (FloatingText) scene.statuses.recycle(FloatingText.class) : null;
	}

	public static void pickUp(Item item, int pos) {
	}

	public static void pickUpJournal(Item item, int pos) {
		if (scene != null) {
			//todo send this
		}
	}

	//TODO: send this to hero
	public static void flashForDocument(Document doc, String page, Hero target) {
		if (scene != null) {
			if (target != null) {
				if (doc == Document.ADVENTURERS_GUIDE) {
					if (!page.equals(Document.GUIDE_INTRO)) {
						if (SPDSettings.interfaceSize() == 0) {
							GLog.p(Messages.get(Guidebook.class, "hint_mobile"));
						} else {
							GLog.p(Messages.get(Guidebook.class, "hint_desktop", KeyBindings.getKeyName(KeyBindings.getFirstKeyForAction(SPDAction.JOURNAL, ControllerHandler.isControllerConnected()))));
						}
					}
					target.getSprite().showStatus(CharSprite.POSITIVE, Messages.get(Guidebook.class, "hint_status"));
				}
			}
			if (doc == Document.ADVENTURERS_GUIDE) {
				if (!page.equals(Document.GUIDE_INTRO)) {
					if (SPDSettings.interfaceSize() == 0) {
						GLog.p(Messages.get(Guidebook.class, "hint_mobile"));
					} else {
						GLog.p(Messages.get(Guidebook.class, "hint_desktop", KeyBindings.getKeyName(KeyBindings.getFirstKeyForAction(SPDAction.JOURNAL, ControllerHandler.isControllerConnected()))));
					}
				}
				//hero.sprite.showStatus(CharSprite.POSITIVE, Messages.get(Guidebook.class, "hint_status"));
			}
			scene.menu.flashForPage(doc, page);
		}
	}

	public static void endIntro() {
		if (scene != null) {
			SPDSettings.intro(false);
			GameLog.wipe();
			if (SPDSettings.interfaceSize() == 0) {
				GLog.p(Messages.get(GameScene.class, "tutorial_ui_mobile"));
			} else {
				GLog.p(Messages.get(GameScene.class, "tutorial_ui_desktop",
						KeyBindings.getKeyName(KeyBindings.getFirstKeyForAction(SPDAction.HERO_INFO, ControllerHandler.isControllerConnected())),
						KeyBindings.getKeyName(KeyBindings.getFirstKeyForAction(SPDAction.INVENTORY, ControllerHandler.isControllerConnected()))));
			}

			//clear hidden doors, it's floor 1 so there are only the entrance ones
			for (int i = 0; i < Dungeon.level.length(); i++) {
				if (Dungeon.level.map[i] == Terrain.SECRET_DOOR) {
					Dungeon.level.discover(i);
					discoverTile(i, Terrain.SECRET_DOOR);
				}
			}
		}
	}

	public static void updateKeyDisplay() {
		if (scene != null && scene.menu != null) scene.menu.updateKeys();
	}

	public static void showlevelUpStars() {
		//todo addToSend
		if (scene != null && scene.status() != null) return;
	}

	//TODO: check this
	public static void updateAvatar() {
		//if (scene != null && scene.status() != null) scene.status.updateAvatar();
	}

	public static void resetMap() {
		if (scene != null) {
			scene.tiles.map(Dungeon.level.map, Dungeon.level.width());
			scene.visualGrid.map(Dungeon.level.map, Dungeon.level.width());
			scene.terrainFeatures.map(Dungeon.level.map, Dungeon.level.width());
			scene.raisedTerrain.map(Dungeon.level.map, Dungeon.level.width());
			scene.walls.map(Dungeon.level.map, Dungeon.level.width());
		}
		updateFog();
	}

	//updates the whole map
	public static void updateMap() {
		if (scene != null) {
			scene.tiles.updateMap();
			scene.visualGrid.updateMap();
			scene.terrainFeatures.updateMap();
			scene.raisedTerrain.updateMap();
			scene.walls.updateMap();
			updateFog();
		}
	}

	public static void updateMap(int cell) {
		if (scene != null) {
			scene.tiles.updateMapCell(cell);
			scene.visualGrid.updateMapCell(cell);
			scene.terrainFeatures.updateMapCell(cell);
			scene.raisedTerrain.updateMapCell(cell);
			scene.walls.updateMapCell(cell);
			//update adjacent cells too
			updateFog(cell, 1);
		}
	}

	public static void plantSeed(int cell) {
		if (scene != null) {
			scene.terrainFeatures.growPlant(cell);
		}
	}

	public static void discoverTile(int pos, int oldValue) {
		SendData.sendActionForAll(new DiscoverTileAction(pos, oldValue));
	}

	@Override
	public synchronized Gizmo addToFront(Gizmo g) {
		if (g instanceof Window && ((Window) g).getOwnerHero() != null) {
			show((Window) g);
			return g;
		} else {
			return super.addToFront(g);
		}
	}

	public static void show(Window wnd) {
		if (scene != null) {
			if (wnd.getOwnerHero() != null) {
				cancel(wnd.getOwnerHero());
				SendData.forceFlush(wnd.getOwnerHero());
				SendData.packAndSendAction(wnd.getOwnerHero(), new UpdateWindowAction(wnd));
				SendData.forceFlush(wnd.getOwnerHero());
			} else {
				scene.addToFront(wnd);
			}
		}
	}

	public static boolean showingWindow() {
		if (scene == null) return false;

		for (Gizmo g : scene.members) {
			if (g instanceof Window) return true;
		}

		return false;
	}

	public static boolean interfaceBlockingHero() {
		if (scene == null) return false;

		if (showingWindow()) return true;

		return false;
	}

	public static void centerNextWndOnInvPane() {

	}

	public static void updateFog() {
		if (scene != null) {
			scene.wallBlocking.updateMap();
		}
	}

	public static void updateFog(int x, int y, int w, int h) {
		if (scene != null) {
			scene.wallBlocking.updateArea(x, y, w, h);
		}
	}

	public static void updateFog(int cell, int radius) {
		if (scene != null) {
			scene.wallBlocking.updateArea(cell, radius);
		}
	}

	public static void afterObserve() {
		if (scene != null) {
			for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
				if (mob.getSprite() != null) {
					if (mob instanceof Mimic && mob.state == mob.PASSIVE && ((Mimic) mob).stealthy() && Dungeon.level.visited[mob.pos]) {
						//mimics stay visible in fog of war after being first seen
						mob.getSprite().visible = true;
					} else {
						mob.getSprite().visible = Dungeon.visibleforAnyHero(mob.pos);
					}
				}
				if (mob instanceof Ghoul) {
					for (Ghoul.GhoulLifeLink link : mob.buffs(Ghoul.GhoulLifeLink.class)) {
						link.updateVisibility();
					}
				}
			}
		}
	}

	public static void flash(int color) {
		SendData.sendActionForAll(new GameSceneFlashAction(color));
	}

	public static void flash(int color, boolean lightmode) {
		SendData.sendActionForAll(new GameSceneFlashAction(color, lightmode));
	}

	@Deprecated
	public static void gameOver(Hero hero) {
		Banner.show(hero, BannerSprites.Type.GAME_OVER, 0x000000, 1f);
		Sample.INSTANCE.play(Assets.Sounds.DEATH);
	}

	//FIXME
	public static void bossSlain() {
		for (Hero hero : Dungeon.heroes) {
			if (hero == null) {
				continue;
			}
			Banner.show(hero, BannerSprites.Type.BOSS_SLAIN, 0xFFFFFF, 0.3f, 5f);
		}
		Sample.INSTANCE.play(Assets.Sounds.BOSS);

	}

	public static void handleCell(Hero hero, int cell) {
		hero.cellSelector.select(cell, PointerEvent.LEFT);
	}

	public static void selectCell(Hero hero, CellSelector.Listener listener) {
		CellSelector cellSelector = hero.cellSelector;

		if (cellSelector.getListener() != null && cellSelector.getListener() != hero.defaultCellListener) {
			cellSelector.getListener().onSelect(null);
		}
		cellSelector.setListener(listener);
		cellSelector.enabled = listener.getOwner().isReady();
		if (scene != null) {
			scene.prompt(listener.prompt());
		}
	}

	public static boolean cancelCellSelector(Hero hero) {
		if (hero.cellSelector.getListener() != null && hero.cellSelector.getListener() != hero.defaultCellListener) {
			hero.cellSelector.resetKeyHold();
			hero.cellSelector.cancel();
			return true;
		} else {
			return false;
		}
	}

	public static WndBag selectItem(WndBag.ItemSelector listener, Hero hero) {
		cancel(hero);

		if (scene != null) {
			{
				WndBag wnd = WndBag.getBag(listener, hero);
				show(wnd);
				return wnd;
			}
		}

		return null;
	}

	public static boolean cancel(Hero hero) {
		hero.cellSelector.resetKeyHold();
		if (hero.curAction != null || hero.resting) {

			hero.curAction = null;
			hero.resting = false;
			return true;

		} else {

			return cancelCellSelector(hero);

		}
	}

	public static void ready(@NotNull Hero hero) {
		selectCell(hero, hero.defaultCellListener);
		//todo use Hero
		QuickSlotButton.cancel();
		if (tagDisappeared) {
			tagDisappeared = false;
			updateTags = true;
		}
	}

	//FIXME
	public static void checkKeyHold(Hero hero) {
		hero.cellSelector.processKeyHold();
	}

	public static void resetKeyHold(Hero hero) {
		hero.cellSelector.resetKeyHold();
	}

	public static void examineCell(Integer cell, Hero hero) {
		if (cell == null
				|| cell < 0
				|| cell > Dungeon.level.length()
				|| (!Dungeon.level.visited[cell] && !Dungeon.level.mapped[cell])) {
			return;
		}

		ArrayList<Object> objects = getObjectsAtCell(cell);

		if (objects.isEmpty()) {
			GameScene.show(new WndInfoCell(cell, hero));
		} else if (objects.size() == 1) {
			examineObject(objects.get(0), hero);
		} else {
			LocalizedString[] names = getObjectNames(objects).toArray(new LocalizedString[0]);

			GameScene.show(new WndOptions(hero, Icons.get(Icons.INFO),
					Messages.get(GameScene.class, "choose_examine"),
					Messages.get(GameScene.class, "multiple_examine"),
					names) {
				@Override
				protected void onSelect(int index) {
					examineObject(objects.get(index), getOwnerHero());
				}
			});

		}
	}

	private static ArrayList<Object> getObjectsAtCell(int cell) {
		ArrayList<Object> objects = new ArrayList<>();
		for (Hero hero : Dungeon.heroes) {
			if (hero != null) {
				if (cell == hero.pos) {
					objects.add(hero);

				} else if (Dungeon.visibleforAnyHero(cell)) {
					Mob mob = (Mob) Actor.findChar(cell);
					if (mob != null) objects.add(mob);
				}
			}
		}
		Heap heap = Dungeon.level.heaps.get(cell);
		if (heap != null && heap.isSeen()) objects.add(heap);

		Plant plant = Dungeon.level.plants.get(cell);
		if (plant != null) objects.add(plant);

		Trap trap = Dungeon.level.traps.get(cell);
		if (trap != null && trap.visible) objects.add(trap);

		return objects;
	}

	private static ArrayList<LocalizedString> getObjectNames(ArrayList<Object> objects) {
		ArrayList<LocalizedString> names = new ArrayList<>();
		for (Object obj : objects) {
			if (obj instanceof Hero) names.add(((Hero) obj).className().toUpperCase(Locale.ENGLISH));
			else if (obj instanceof Mob) names.add(Messages.titleCase(((Mob) obj).name()));
			else if (obj instanceof Heap) names.add(Messages.titleCase(((Heap) obj).title()));
			else if (obj instanceof Plant) names.add(Messages.titleCase(((Plant) obj).name()));
			else if (obj instanceof Trap) names.add(Messages.titleCase(((Trap) obj).name()));
		}
		return names;
	}

	public static void examineObject(Object o, Hero hero) {
		if (o instanceof Hero) {
			GameScene.show(new WndHero((Hero) o));
		} else if (o instanceof Mob && ((Mob) o).isActive()) {
			GameScene.show(new WndInfoMob((Mob) o, hero));
			if (o instanceof Snake && !Document.ADVENTURERS_GUIDE.isPageRead(Document.GUIDE_SURPRISE_ATKS)) {
				GameScene.flashForDocument(Document.ADVENTURERS_GUIDE, Document.GUIDE_SURPRISE_ATKS, hero);
			}
		} else if (o instanceof Heap && !((Heap) o).isEmpty()) {
			GameScene.show(new WndInfoItem((Heap) o, hero));
		} else if (o instanceof Plant) {
			GameScene.show(new WndInfoPlant((Plant) o, hero));
			//plants can be harmful to trample, so let the player ID just by examine
			Bestiary.setSeen(o.getClass());//plants can be harmful to trample, so let the player ID just by examine
			Bestiary.setSeen(o.getClass());
		} else if (o instanceof Trap) {
			GameScene.show(new WndInfoTrap((Trap) o, hero));
			//traps are often harmful to trigger, so let the player ID just by examine
			Bestiary.setSeen(o.getClass());
			//traps are often harmful to trigger, so let the player ID just by examine
			Bestiary.setSeen(o.getClass());
		} else {
			GameScene.show(new WndMessage(hero, Messages.get(GameScene.class, "dont_know")));
		}
	}

	//FIXME
	public static class DefaultCellListener extends CellSelector.Listener {
		@Override
		public void onSelect(Integer cell) {
			if (Dungeon.level != null) {
				if (cell == null) return;
				if (getOwner().handle(cell)) {
					getOwner().next();
				}
			}
		}

		@Override
		public LocalizedString prompt() {
			return null;
		}
	}

	public static void addHeroSprite(Hero hero) {
		CharSprite sprite = hero.getSprite();
		sprite.visible = true;
		sprite.link(hero);
	}
}
