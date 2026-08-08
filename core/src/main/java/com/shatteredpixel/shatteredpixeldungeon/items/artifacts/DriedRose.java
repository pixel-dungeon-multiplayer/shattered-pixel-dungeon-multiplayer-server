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

package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Regeneration;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.Stasis;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Wraith;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.DirectableAlly;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShaftParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRetribution;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfPsionicBlast;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import io.github.pixeldungeonmultiplayer.shattered.server.network.SendData;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.UpdateWindowAction;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GhostSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.ui.ItemButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndQuest;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUseItem;
import com.watabou.noosa.Game;
import io.github.pixeldungeonmultiplayer.shattered.server.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.ArrayList;

public class DriedRose extends Artifact {

	{
		image = ItemSpriteSheet.ARTIFACT_ROSE1;

		levelCap = 10;

		setCharge(100);
		chargeCap = 100;

		defaultAction = AC_SUMMON;
	}

	private boolean talkedTo = false;
	private boolean firstSummon = false;
	
	private GhostHero ghost = null;
	private int ghostID = 0;
	
	private MeleeWeapon weapon = null;
	private Armor armor = null;

	public int droppedPetals = 0;

	public static final String AC_SUMMON = "SUMMON";
	public static final String AC_DIRECT = "DIRECT";
	public static final String AC_OUTFIT = "OUTFIT";

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (!Ghost.Quest.completed()){
			return actions;
		}
		if (isEquipped( hero )
				&& getCharge() == chargeCap
				&& !cursed
				&& hero.buff(MagicImmune.class) == null
				&& ghostID == 0) {
			actions.add(AC_SUMMON);
		}
		if (ghostID != 0){
			actions.add(AC_DIRECT);
		}
		if (isIdentified() && !cursed){
			actions.add(AC_OUTFIT);
		}
		
		return actions;
	}

	@Override
	public String defaultAction() {
		if (ghost != null){
			return AC_DIRECT;
		} else {
			return AC_SUMMON;
		}
	}

	@Override
	public void execute( Hero hero, String action ) {

		super.execute(hero, action);

		if (action.equals(AC_SUMMON)) {

			if (hero.buff(MagicImmune.class) != null) return;

			if (!Ghost.Quest.completed())   GameScene.show(new WndUseItem(hero, null, this));
			else if (ghost != null)         GLog.i( Messages.get(this, "spawned") );
			else if (!isEquipped( hero ))   GLog.i( Messages.get(Artifact.class, "need_to_equip") );
			else if (getCharge() != chargeCap)   GLog.i( Messages.get(this, "no_charge") );
			else if (cursed)                GLog.i( Messages.get(this, "cursed") );
			else {
				ArrayList<Integer> spawnPoints = new ArrayList<>();
				for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
					int p = hero.pos + PathFinder.NEIGHBOURS8[i];
					if (Actor.findChar(p) == null && (Dungeon.level.passable[p] || Dungeon.level.avoid[p])) {
						spawnPoints.add(p);
					}
				}

				if (spawnPoints.size() > 0) {
					ghost = new GhostHero( this , hero);
					ghostID = ghost.id();
					ghost.pos = Random.element(spawnPoints);

					GameScene.add(ghost, 1f);
					Dungeon.level.occupyCell(ghost);
					
					CellEmitter.get(ghost.pos).start( ShaftParticle.FACTORY, 0.3f, 4 );
					CellEmitter.get(ghost.pos).start( Speck.factory(Speck.LIGHT), 0.2f, 3 );

					hero.spend(1f);
					hero.busy();
					hero.getSprite().operate(hero.pos);

					if (!firstSummon) {
						ghost.yell( Messages.get(GhostHero.class, "hello", Messages.titleCase(hero.name())) );
						Sample.INSTANCE.play( Assets.Sounds.GHOST );
						firstSummon = true;
						
					} else {
						if (BossHealthBar.isAssigned()) {
							ghost.sayBoss();
						} else {
							ghost.sayAppeared();
						}
					}

					Invisibility.dispel(hero);
					Talent.onArtifactUsed(hero);
					setCharge(0, hero);
					partialCharge = 0;
					updateQuickslot();

				} else
					GLog.i( Messages.get(this, "no_space") );
			}

		} else if (action.equals(AC_DIRECT)){
			if (ghost == null && ghostID != 0){
				findGhost(hero);
			}
			if (ghost != null && ghost != Stasis.getStasisAlly(hero)){
				GameScene.selectCell(hero, ghostDirector);
			}

		} else if (action.equals(AC_OUTFIT)){
			GameScene.show( new WndGhostHero(this, hero) );
		}
	}

	private void findGhost(Hero hero){
		Actor a = Actor.findById(ghostID);
		if (a != null){
			ghost = (GhostHero)a;
		} else {
            if (Stasis.getStasisAlly(hero) instanceof GhostHero) {
                ghost = (GhostHero) Stasis.getStasisAlly(hero);
                ghostID = ghost.id();
            } else {
                ghostID = 0;
            }
        }
	}
	
	public int ghostStrength(){
		return 13 + level()/2;
	}

	@Override
	public LocalizedString desc(Hero hero) {
		if (!Ghost.Quest.completed()
				&& (ShatteredPixelDungeon.scene() instanceof GameScene)){
			return Messages.get(this, "desc_no_quest");
		}
		
		LocalizedString desc = super.desc();

		if (isEquipped(hero)){
			if (!cursed){

				if (level() < levelCap)
					desc = LocalizedString.concat(desc, LocalizedString.concat("\n\n", Messages.get(this, "desc_hint")));

			} else {
				desc = LocalizedString.concat(desc, LocalizedString.concat("\n\n", Messages.get(this, "desc_cursed")));
			}
		}

		if (weapon != null || armor != null) {
			desc = LocalizedString.concat( desc, "\n");

			if (weapon != null) {
				desc = LocalizedString.concat(desc, LocalizedString.concat("\n", Messages.get(this, "desc_weapon", Messages.titleCase(weapon.title()))));
			}

			if (armor != null) {
				desc = LocalizedString.concat(desc, LocalizedString.concat("\n", Messages.get(this, "desc_armor", Messages.titleCase(armor.title()))));
			}

			desc = LocalizedString.concat(desc, LocalizedString.concat("\n", Messages.get(this, "desc_strength", ghostStrength())));

		}
		
		return desc;
	}
	
	@Contract(pure = true)
    @Override
	public int value() {
		if (weapon != null){
			return -1;
		}
		if (armor != null){
			return -1;
		}
		return super.value();
	}

	@Override
	public @Nullable LocalizedString status(Hero hero) {
		if (ghost == null && ghostID != 0){
			try {
				findGhost(hero);
			} catch ( ClassCastException e ){
				ShatteredPixelDungeon.reportException(e);
				ghostID = 0;
			}
		}
		if (ghost == null){
			return super.status();
		} else {
			return LocalizedString.raw(((ghost.getHP() *100) / ghost.getHT()) + "%");
		}
	}
	
	@Override
	protected ArtifactBuff passiveBuff() {
		return new roseRecharge();
	}
	
	@Override
	public void charge(Hero target, float amount) {
		if (cursed || target.buff(MagicImmune.class) != null) return;

		if (ghost == null){
			if (getCharge() < chargeCap) {
				partialCharge += 4*amount;
				while (partialCharge >= 1f){
					setCharge(getCharge() + 1, target);
					partialCharge--;
				}
				if (getCharge() >= chargeCap) {
					setCharge(chargeCap, target);
					partialCharge = 0;
					GLog.p(Messages.get(DriedRose.class, "charged"));
				}
				updateQuickslot();
			}
		} else if (ghost.getHP() < ghost.getHT()) {
			int heal = Math.round((1 + level()/3f)*amount);
			ghost.setHP(Math.min(ghost.getHT(), ghost.getHP() + heal));
			if (ghost.getSprite() != null) {
				ghost.getSprite().showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(heal), FloatingText.HEALING);
			}
			updateQuickslot();
		}
	}
	
	@Override
	public Item upgrade(Hero hero) {
		if (level() >= 9)
			image = ItemSpriteSheet.ARTIFACT_ROSE3;
		else if (level() >= 4)
			image = ItemSpriteSheet.ARTIFACT_ROSE2;

		//For upgrade transferring via well of transmutation
		droppedPetals = Math.max( level(), droppedPetals );
		
		if (ghost != null){
			ghost.updateRose(hero);
			ghost.setHP(Math.min(ghost.getHP()+8, ghost.getHT()));
		}

		return super.upgrade(hero);
	}
	
	public Weapon ghostWeapon(){
		return weapon;
	}
	
	public Armor ghostArmor(){
		return armor;
	}

	private static final String TALKEDTO =      "talkedto";
	private static final String FIRSTSUMMON =   "firstsummon";
	private static final String GHOSTID =       "ghostID";
	private static final String PETALS =        "petals";
	
	private static final String WEAPON =        "weapon";
	private static final String ARMOR =         "armor";

	@Override
	public void storeInBundle(@NotNull Bundle bundle ) {
		super.storeInBundle(bundle);

		bundle.put( TALKEDTO, talkedTo );
		bundle.put( FIRSTSUMMON, firstSummon );
		bundle.put( GHOSTID, ghostID );
		bundle.put( PETALS, droppedPetals );
		
		if (weapon != null) bundle.put( WEAPON, weapon );
		if (armor != null)  bundle.put( ARMOR, armor );
	}

	@Override
	public void restoreFromBundle(@NotNull Bundle bundle ) {
		super.restoreFromBundle(bundle);

		talkedTo = bundle.getBoolean( TALKEDTO );
		firstSummon = bundle.getBoolean( FIRSTSUMMON );
		ghostID = bundle.getInt( GHOSTID );
		droppedPetals = bundle.getInt( PETALS );
		
		if (bundle.contains(WEAPON)) weapon = (MeleeWeapon)bundle.get( WEAPON );
		if (bundle.contains(ARMOR))  armor = (Armor)bundle.get( ARMOR );
	}

	public class roseRecharge extends ArtifactBuff {

		@Override
		public boolean act() {
			
			spend( TICK );
			
			if (ghost == null && ghostID != 0){
				findGhost((Hero) target);
			}

			if (ghost != null && !ghost.isAlive()){
				ghost = null;
			}
			
			//rose does not charge while ghost hero is alive
			if (ghost != null && !cursed && target.buff(MagicImmune.class) == null){
				
				//heals to full over 500 turns
				if (ghost.getHP() < ghost.getHT() && Regeneration.regenOn((Hero) target)) {
					partialCharge += (ghost.getHT() / 500f) * RingOfEnergy.artifactChargeMultiplier(target);
					updateQuickslot();
					
					while (partialCharge > 1) {
						ghost.setHP(ghost.getHP() + 1);
						partialCharge--;
						if (ghost.getHP() == ghost.getHT()){
							partialCharge = 0;
						}
					}
				} else {
					partialCharge = 0;
				}
				
				return true;
			}
			
			if (getCharge() < chargeCap
					&& !cursed
					&& target.buff(MagicImmune.class) == null
					&& Regeneration.regenOn((Hero) target)) {
				//500 turns to a full charge
				partialCharge += (1/5f * RingOfEnergy.artifactChargeMultiplier(target));
				while (partialCharge > 1){
					setCharge(getCharge() + 1, (Hero) target);
					partialCharge--;
					if (getCharge() == chargeCap){
						partialCharge = 0f;
						GLog.p( Messages.get(DriedRose.class, "charged") );
					}
				}
			} else if (cursed && Random.Int(100) == 0) {

				ArrayList<Integer> spawnPoints = new ArrayList<>();

				for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
					int p = target.pos + PathFinder.NEIGHBOURS8[i];
					if (Actor.findChar(p) == null && (Dungeon.level.passable[p] || Dungeon.level.avoid[p])) {
						spawnPoints.add(p);
					}
				}

				if (spawnPoints.size() > 0) {
					Wraith.spawnAt(Random.element(spawnPoints), Wraith.class);
					Sample.INSTANCE.play(Assets.Sounds.CURSED);
				}

			}

			updateQuickslot();

			return true;
		}
	}
	
	public CellSelector.Listener ghostDirector = new CellSelector.Listener(){
		
		@Override
		public void onSelect(Integer cell) {
			if (cell == null) return;
			
			Sample.INSTANCE.play( Assets.Sounds.GHOST );

			ghost.directTocell(cell);

		}
		
		@Override
		public LocalizedString prompt() {
			return  LocalizedString.concat("\"" , Messages.get(GhostHero.class, "direct_prompt"), "\"");
		}
	};

	public static class Petal extends Item {

		{
			stackable = true;
			dropsDownHeap = true;
			
			image = ItemSpriteSheet.PETAL;
		}

		@Override
		public boolean doPickUp(Hero hero, int pos) {
			Catalog.setSeen(getClass());
			Statistics.itemTypesDiscovered.add(getClass());
			DriedRose rose = hero.belongings.getItem( DriedRose.class );

			if (rose == null){
				GLog.w( Messages.get(this, "no_rose") );
				return false;
			} if ( rose.level() >= rose.levelCap ){
				GLog.i( Messages.get(this, "no_room") );
				hero.spendAndNext(pickupDelay());
				return true;
			} else {

				rose.upgrade(hero);
				Catalog.countUse(rose.getClass());
				if (rose.level() == rose.levelCap) {
					GLog.p( Messages.get(this, "maxlevel") );
				} else
					GLog.i( Messages.get(this, "levelup") );

				Sample.INSTANCE.play( Assets.Sounds.DEWDROP );
				GameScene.pickUp(this, pos);
				hero.spendAndNext(pickupDelay());
				return true;

			}
		}

		@Override
		public boolean isUpgradable() {
			return false;
		}

		@Override
		public boolean isIdentified() {
			return true;
		}

	}

	public static class GhostHero extends DirectableAlly {

		{
			spriteClass = GhostSprite.class;

			flying = true;
			
			state = HUNTING;
			
			properties.add(Property.UNDEAD);
			properties.add(Property.INORGANIC);
		}
		
		private DriedRose rose = null;
		
		public GhostHero(Hero hero){
			super(hero);
		}

		public GhostHero(DriedRose rose, Hero hero){
			super(hero);
			this.rose = rose;
			updateRose(hero);
			setHP(getHT());
		}

		public GhostHero() {
			super();
		}

		@Override
		public void defendPos(int cell) {
			yell(Messages.get(this, "directed_position_" + Random.IntRange(1, 5)));
			super.defendPos(cell);
		}

		@Override
		public void followHero() {
			yell(Messages.get(this, "directed_follow_" + Random.IntRange(1, 5)));
			super.followHero();
		}

		@Override
		public void targetChar(Char ch) {
			yell(Messages.get(this, "directed_attack_" + Random.IntRange(1, 5)));
			super.targetChar(ch);
		}

		private void updateRose(Hero hero){
			if (rose == null && hero != null) {
				rose = hero.belongings.getItem(DriedRose.class);
				if (rose != null) {
					rose.ghost = this;
					rose.ghostID = id();
				}
			}
			
			//same dodge as the hero
			if (hero != null) {
				defenseSkill = (hero.lvl + 4);
			}
			if (rose == null) return;
			setHT(20 + 8*rose.level());
		}

		public Weapon weapon(){
			if (rose != null)   return rose.weapon;
			else                return null;
		}

		public Armor armor(){
			if (rose != null)   return rose.armor;
			else                return null;
		}

		@Override
		protected boolean act() {
			updateRose(getOwner());
			if (owner == null
					|| rose == null
					|| !rose.isEquipped(getOwner())
					|| getOwner().buff(MagicImmune.class) != null){
				damage(1, new DamageCause(new NoRoseDamage(), getOwner()));
			}
			
			if (!isAlive()) {
				return true;
			}
			return super.act();
		}

		public static class NoRoseDamage{}

		@Override
		public int attackSkill(Char target) {
			int acc;
			//same accuracy as the hero.
			if (getOwner() != null) {
				acc = getOwner().lvl + 9;

				if (weapon() != null) {
					acc *= weapon().accuracyFactor(this, target);
				}

				return acc;
			}
			//Worst possible accuracy, 1 + 9

			return 10;
		}

		@Override
		public float attackDelay() {
			float delay = super.attackDelay();
			if (weapon() != null){
				delay *= weapon().delayFactor(this);
			}
			return delay;
		}
		
		@Override
		protected boolean canAttack(Char enemy) {
			return super.canAttack(enemy) || (weapon() != null && weapon().canReach(this, enemy.pos));
		}
		
		@Override
		public int damageRoll() {
			int dmg = 0;
			if (weapon() != null){
				dmg += weapon().damageRoll(this);
			} else {
				dmg += Random.NormalIntRange(0, 5);
			}
			
			return dmg;
		}
		
		@Override
		public int attackProc(Char enemy, int damage) {
			damage = super.attackProc(enemy, damage);

			if (weapon() != null) {
				damage = weapon().proc(this, enemy, damage);
				if (!enemy.isAlive() && enemy instanceof Hero) {
					Dungeon.fail(this);
					GLog.n(Messages.capitalize(Messages.get(Char.class, "kill", name())));
				}
			}

			return damage;
		}
		
		@Override
		public int defenseProc(Char enemy, int damage) {
			if (armor() != null) {
				damage = armor().proc( enemy, this, damage );
			}
			return super.defenseProc(enemy, damage);
		}
		
		@Override
		public void damage(int dmg, @NotNull DamageCause source) {
			Object src = source.getCause();
			super.damage( dmg, source );
			
			//for the rose status indicator
			if (rose != null) {
				rose.updateQuickslot();
			}
		}
		
		@Override
		public float speed() {
			float speed = super.speed();

			//moves 2 tiles at a time when returning to the hero
			if (state == WANDERING
					&& defendingPos == -1
					&& Dungeon.level.distance(pos, getOwner().pos) > 1){
				speed *= 2;
			}
			
			return speed;
		}
		
		@Override
		public int defenseSkill(Char enemy) {
			int defense = super.defenseSkill(enemy);

			if (defense != 0 && armor() != null ){
				defense = Math.round(armor().evasionFactor( this, defense ));
			}
			
			return defense;
		}

		@Override
		public int drRoll() {
			int dr = super.drRoll();
			if (armor() != null){
				dr += Random.NormalIntRange( armor().DRMin(getOwner()), armor().DRMax(getOwner()));
			}
			if (weapon() != null){
				dr += Random.NormalIntRange( 0, weapon().defenseFactor( this ));
			}
			return dr;
		}

		@Override
		public int glyphLevel(Class<? extends Armor.Glyph> cls) {
			if (armor() != null && armor().hasGlyph(cls, this)){
				return Math.max(super.glyphLevel(cls), armor().buffedLvl());
			} else {
				return super.glyphLevel(cls);
			}
		}

		@Override
		public boolean interact(Char c) {
			updateRose(getOwner());
			if (c instanceof Hero && rose != null && !rose.talkedTo){
				rose.talkedTo = true;
				Game.runOnRenderThread(new Callback() {
					@Override
					public void call() {
						GameScene.show(new WndQuest(GhostHero.this, Messages.get(GhostHero.this, "introduce"), (Hero) c));
					}
				});
				return true;
			} else {
				return super.interact(c);
			}
		}

		@Override
		public void die(@NotNull DamageCause cause) {
			sayDefeated();
			super.die(cause);
		}

		@Override
		public void destroy() {
			updateRose(getOwner());
			//TODO stasis?
			if (rose != null) {
				rose.ghost = null;
				rose.setCharge(0, getOwner());
				rose.partialCharge = 0;
				rose.ghostID = -1;
			}
			super.destroy();
		}
		
		public void sayAppeared(){
			if (getOwner().buff(AscensionChallenge.class) != null){
				yell( Messages.get( this, "dialogue_ascension_" + Random.IntRange(1, 6) ));

			} else {

				int depth = (Dungeon.depth - 1) / 5;

				//only some lines are said on the first floor of a depth
				int variant = Dungeon.depth % 5 == 1 ? Random.IntRange(1, 3) : Random.IntRange(1, 6);

				switch (depth) {
					case 0:
						yell(Messages.get(this, "dialogue_sewers_" + variant));
						break;
					case 1:
						yell(Messages.get(this, "dialogue_prison_" + variant));
						break;
					case 2:
						yell(Messages.get(this, "dialogue_caves_" + variant));
						break;
					case 3:
						yell(Messages.get(this, "dialogue_city_" + variant));
						break;
					case 4:
					default:
						yell(Messages.get(this, "dialogue_halls_" + variant));
						break;
				}
			}
			if (ShatteredPixelDungeon.scene() instanceof GameScene) {
				Sample.INSTANCE.play( Assets.Sounds.GHOST );
			}
		}
		
		public void sayBoss(){
			int depth = (Dungeon.depth - 1) / 5;
			
			switch(depth){
				case 0:
					yell( Messages.get( this, "seen_goo_" + Random.IntRange(1, 3) ));
					break;
				case 1:
					yell( Messages.get( this, "seen_tengu_" + Random.IntRange(1, 3) ));
					break;
				case 2:
					yell( Messages.get( this, "seen_dm300_" + Random.IntRange(1, 3) ));
					break;
				case 3:
					yell( Messages.get( this, "seen_king_" + Random.IntRange(1, 3) ));
					break;
				case 4: default:
					yell( Messages.get( this, "seen_yog_" + Random.IntRange(1, 3) ));
					break;
			}
			Sample.INSTANCE.play( Assets.Sounds.GHOST );
		}
		
		public void sayDefeated(){
			if (BossHealthBar.isAssigned()){
				yell( Messages.get( this, "defeated_by_boss_" + Random.IntRange(1, 3) ));
			} else {
				yell( Messages.get( this, "defeated_by_enemy_" + Random.IntRange(1, 3) ));
			}
			Sample.INSTANCE.play( Assets.Sounds.GHOST );
		}
		
		public void sayHeroKilled(){
			yell( Messages.get( this, "player_killed_" + Random.IntRange(1, 3) ));
			GLog.newLine();
			Sample.INSTANCE.play( Assets.Sounds.GHOST );
		}
		
		public void sayAnhk(){
			yell( Messages.get( this, "blessed_ankh_" + Random.IntRange(1, 3) ));
			Sample.INSTANCE.play( Assets.Sounds.GHOST );
		}
		
		{
			immunities.add( CorrosiveGas.class );
			immunities.add( Burning.class );
			immunities.add( ScrollOfRetribution.class );
			immunities.add( ScrollOfPsionicBlast.class );
			immunities.add( AllyBuff.class );
		}

	}
	
	public static class WndGhostHero extends Window{

		private static final String TYPE = "ghost_hero";

		private static final int BTN_SIZE	= 32;
		private static final float GAP		= 2;
		private static final float BTN_GAP	= 12;
		private static final int WIDTH		= 116;
		
		private final ItemButton btnWeapon;
		private final ItemButton btnArmor;
		private final DriedRose rose;
		private boolean hidden = false;
		private final LocalizedString title;
		private final LocalizedString message;

		WndGhostHero(final DriedRose rose, Hero hero){
			super(hero);
			this.rose = rose;
			title = Messages.get(this, "title");
			message = Messages.get(this, "desc", rose.ghostStrength());
			
			btnWeapon = new ItemButton(){
				@Override
				protected void onClick() {
					if (rose.weapon != null){
						item(new WndBag.Placeholder(ItemSpriteSheet.WEAPON_HOLDER));
						if (!rose.weapon.doPickUp(getOwnerHero())){
							Dungeon.level.drop( rose.weapon, getOwnerHero().pos);
						}
						rose.weapon = null;
					} else {
						GameScene.selectItem(new WndBag.ItemSelector() {

							@Override
							public LocalizedString textPrompt() {
								return Messages.get(WndGhostHero.class, "weapon_prompt");
							}

							@Override
							public Class<? extends Bag> preferredBag() {
								return Belongings.Backpack.class;
							}

							@Override
							public boolean itemSelectable(Item item) {
								return item instanceof MeleeWeapon;
							}

							@Override
							public void onSelect(Item item) {
								if (!(item instanceof MeleeWeapon)) {
									//do nothing, should only happen when window is cancelled
									sendSelf();
								} else if (item.unique) {
									GLog.w(Messages.get(WndGhostHero.class, "cant_unique"));
									hide();
								} else if (item.cursed || !item.cursedKnown) {
									GLog.w(Messages.get(WndGhostHero.class, "cant_cursed"));
									hide();
								}  else if (!item.levelKnown && ((MeleeWeapon)item).STRReq(0) > rose.ghostStrength()){
									GLog.w(Messages.get(WndGhostHero.class, "cant_strength_unknown"));
									hide();
								} else if (((MeleeWeapon) item).STRReq() > rose.ghostStrength()) {
									GLog.w(Messages.get(WndGhostHero.class, "cant_strength"));
									hide();
								} else {
									if (item.isEquipped(getOwner())) {
										((MeleeWeapon) item).doUnequip(getOwner(), false, false);
									} else {
										item.detach(getOwner().belongings.backpack);
									}
									rose.weapon = (MeleeWeapon) item;
									item(rose.weapon);
									sendSelf();
								}

							}
						}, getOwnerHero());
					}
				}

				@Override
				protected boolean onLongClick() {
					if (item() != null && item().name() != null){
						GameScene.show(new WndInfoItem(getOwnerHero(), item()));
						return true;
					}
					return false;
				}
			};
			if (rose.weapon != null) {
				btnWeapon.item(rose.weapon);
			} else {
				btnWeapon.item(new WndBag.Placeholder(ItemSpriteSheet.WEAPON_HOLDER));
			}
			add( btnWeapon );
			
			btnArmor = new ItemButton(){
				@Override
				protected void onClick() {
					if (rose.armor != null){
						item(new WndBag.Placeholder(ItemSpriteSheet.ARMOR_HOLDER));
						if (!rose.armor.doPickUp(getOwnerHero())){
							Dungeon.level.drop( rose.armor, getOwnerHero().pos);
						}
						rose.armor = null;
					} else {
						GameScene.selectItem(new WndBag.ItemSelector() {

							@Override
							public LocalizedString textPrompt() {
								return Messages.get(WndGhostHero.class, "armor_prompt");
							}

							@Override
							public Class<?extends Bag> preferredBag(){
								return Belongings.Backpack.class;
							}

							@Override
							public boolean itemSelectable(Item item) {
								return item instanceof Armor;
							}

							@Override
							public void onSelect(Item item) {
								if (!(item instanceof Armor)) {
									//do nothing, should only happen when window is cancelled
									sendSelf();
								} else if (item.unique || ((Armor) item).checkSeal() != null) {
									GLog.w( Messages.get(WndGhostHero.class, "cant_unique"));
									hide();
								} else if (item.cursed || !item.cursedKnown) {
									GLog.w(Messages.get(WndGhostHero.class, "cant_cursed"));
									hide();
								}  else if (!item.levelKnown && ((Armor)item).STRReq(0) > rose.ghostStrength()){
									GLog.w( Messages.get(WndGhostHero.class, "cant_strength_unknown"));
									hide();
								} else if (((Armor)item).STRReq() > rose.ghostStrength()) {
									GLog.w( Messages.get(WndGhostHero.class, "cant_strength"));
									hide();
								} else {
									if (item.isEquipped(getOwner())){
										((Armor) item).doUnequip(getOwner(), false, false);
									} else {
										item.detach(getOwner().belongings.backpack);
									}
									rose.armor = (Armor) item;
									item(rose.armor);
									sendSelf();
								}
								
							}
						}, getOwnerHero());
					}
				}

				@Override
				protected boolean onLongClick() {
					if (item() != null && item().name() != null){
						GameScene.show(new WndInfoItem(getOwnerHero(), item()));
						return true;
					}
					return false;
				}
			};
			btnArmor.setRect( btnWeapon.right() + BTN_GAP, btnWeapon.top(), BTN_SIZE, BTN_SIZE );
			if (rose.armor != null) {
				btnArmor.item(rose.armor);
			} else {
				btnArmor.item(new WndBag.Placeholder(ItemSpriteSheet.ARMOR_HOLDER));
			}
			add( btnArmor );
			
			resize(WIDTH, (int)(btnArmor.bottom() + GAP));
		}
		private void sendSelf() {
			SendData.packAndSendAction(getOwnerHero(), new UpdateWindowAction(this));
		}

		public Item weaponItem() {
			return btnWeapon.item();
		}

		public Item armorItem() {
			return btnArmor.item();
		}

		public DriedRose rose() {
			return rose;
		}

		public LocalizedString title() {
			return title;
		}

		public LocalizedString message() {
			return message;
		}

		@Override
		public void hide() {
			hidden = true;
			super.hide();
			//SendData.sendWindow(this, Window.HIDE_WINDOW_TYPE);
		}

		@Override
		public void onSelect(int button, @Nullable JSONObject args) {
			boolean isLongClick = args != null && args.optBoolean("is_long_click", false);
			if (button == 0) {
				if (isLongClick) {
					btnWeapon.onLongClickNetwork();
				} else {
					btnWeapon.onClickNetwork();
				}
			} else if (button == 1) {
				if (isLongClick) {
					btnArmor.onLongClickNetwork();
				} else {
					btnArmor.onClickNetwork();
				}
			}
		}
	}
}
