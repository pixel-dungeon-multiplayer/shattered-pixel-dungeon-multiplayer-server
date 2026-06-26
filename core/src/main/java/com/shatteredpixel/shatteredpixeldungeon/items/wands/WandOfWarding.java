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

package com.shatteredpixel.shatteredpixeldungeon.items.wands;

import io.github.pixeldungeonmultiplayer.shattered.server.noosa.audio.Sample;
import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.Stasis;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.journal.Bestiary;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.WardSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class WandOfWarding extends Wand {

	{
		image = ItemSpriteSheet.WAND_WARDING;
		usesTargeting = false; //player usually targets wards or spaces, not enemies
	}

	@Override
	public int collisionProperties(int target) {
		if (cursed)                                 return super.collisionProperties(target);
		else if (!Dungeon.visibleforAnyHero(target))    return Ballistica.PROJECTILE;
		else                                        return Ballistica.STOP_TARGET;
	}

	@Override
	public void execute(Hero hero, String action) {
		//cursed warding does use targeting as it's just doing regular cursed zaps
		usesTargeting = cursed && cursedKnown;
		super.execute(hero, action);
	}

	private boolean wardAvailable = true;
	
	@Override
	public boolean tryToZap(Hero owner, int target) {
		
		int currentWardEnergy = 0;
		for (Char ch : Actor.chars()){
			if (ch instanceof Ward){
				currentWardEnergy += ((Ward) ch).tier;
			}
		}

		if (Stasis.getStasisAlly(owner) instanceof Ward){
			currentWardEnergy += ((Ward) Stasis.getStasisAlly(owner)).tier;
		}

		int maxWardEnergy = 0;
		for (Buff buff : curUser.buffs()){
			if (buff instanceof Wand.Charger){
				if (((Charger) buff).wand() instanceof WandOfWarding){
					maxWardEnergy += 2 + ((Charger) buff).wand().level(owner);
				}
			}
		}
		
		wardAvailable = (currentWardEnergy < maxWardEnergy);
		
		Char ch = Actor.findChar(target);
		if (ch instanceof Ward){
			if (!wardAvailable && ((Ward) ch).tier <= 3){
				GLog.w( Messages.get(this, "no_more_wards"));
				return false;
			}
		} else {
			if ((currentWardEnergy + 1) > maxWardEnergy){
				GLog.w( Messages.get(this, "no_more_wards"));
				return false;
			}
		}
		
		return super.tryToZap(owner, target);
	}
	
	@Override
	public void onZap(Ballistica bolt, Hero hero) {

		int target = bolt.collisionPos;
		Char ch = Actor.findChar(target);
		if (ch != null && !(ch instanceof Ward)){
			if (bolt.dist > 1) target = bolt.path.get(bolt.dist-1);

			ch = Actor.findChar(target);
			if (ch != null && !(ch instanceof Ward)){
				GLog.w( Messages.get(this, "bad_location"));
				Dungeon.level.pressCell(bolt.collisionPos);
				return;
			}
		}

		if (ch != null){
			if (ch instanceof Ward){
				if (wardAvailable) {
					((Ward) ch).upgrade( buffedLvl() );
				} else {
					((Ward) ch).wandHeal( buffedLvl() );
				}
				ch.getSprite().emitter().burst(MagicMissile.WardParticle.UP, ((Ward) ch).tier);
			} else {
				GLog.w( Messages.get(this, "bad_location"));
				Dungeon.level.pressCell(target);
			}
			
		} else if (!Dungeon.level.passable[target]){
			GLog.w( Messages.get(this, "bad_location"));
			Dungeon.level.pressCell(target);

		} else {
			Ward ward = new Ward();
			ward.owner = hero;
			ward.pos = target;
			ward.wandLevel = buffedLvl();
			GameScene.add(ward, 1f);
			Dungeon.level.occupyCell(ward);
			ward.getSprite().emitter().burst(MagicMissile.WardParticle.UP, ward.tier);
			Dungeon.level.pressCell(target);

		}
	}

	@Override
	public void fx(Ballistica bolt, Callback callback) {
		MagicMissile m = MagicMissile.boltFromChar(curUser.getSprite().parent,
				MagicMissile.WARD,
                curUser.getSprite(),
				bolt.collisionPos,
				callback);
		
		if (bolt.dist > 10){
			m.setSpeed(bolt.dist*20);
		}
		Sample.INSTANCE.play(Assets.Sounds.ZAP);
	}

	@Override
	public void onHit(MagesStaff staff, Char attacker, Char defender, int damage) {
		int level = Math.max( 0, staff.buffedLvl() );

		// lvl 0 - 20%
		// lvl 1 - 33%
		// lvl 2 - 43%
		float procChance = (level+1f)/(level+5f) * procChanceMultiplier(attacker);
		if (Random.Float() < procChance) {

			float powerMulti = Math.max(1f, procChance);

			for (Char ch : Actor.chars()){
				if (ch instanceof Ward){
					((Ward) ch).wandHeal(staff.buffedLvl(), powerMulti);
					ch.getSprite().emitter().burst(MagicMissile.WardParticle.UP, ((Ward) ch).tier);
				}
			}
		}
	}

	@Override
	public void staffFx(MagesStaff.StaffParticle particle) {
		particle.color( 0x8822FF );
		particle.am = 0.3f;
		particle.setLifespan(3f);
		particle.speed.polar(Random.Float(PointF.PI2), 0.3f);
		particle.setSize( 1f, 2f);
		particle.radiateXY(2.5f);
	}

	@Override
	public LocalizedString statsDesc(Hero hero) {
		if (levelKnown)
			return Messages.get(this, "stats_desc", level(hero)+2);
		else
			return Messages.get(this, "stats_desc", 2);
	}

	@Override
	public String upgradeStat1(int level) {
		return 2+level + "-" + (8+4*level);
	}

	@Override
	public String upgradeStat2(int level) {
		return Integer.toString(level+2);
	}

	public static class Ward extends NPC {
		public Hero owner;

		public int tier = 1;
		private int wandLevel = 1;

		public int totalZaps = 0;

		{
			spriteClass = WardSprite.class;

			alignment = Alignment.ALLY;

			properties.add(Property.IMMOVABLE);
			properties.add(Property.INORGANIC);

			viewDistance = 4;
			state = WANDERING;
		}

		@Override
		public LocalizedString name() {
			return Messages.get(this, "name_" + tier );
		}

		public void upgrade(int wandLevel ){
			if (this.wandLevel < wandLevel){
				this.wandLevel = wandLevel;
			}

			switch (tier){
				case 1: case 2: default:
					break; //do nothing
				case 3:
					setHT(35);
					setHP(15 + (5-totalZaps)*4);
					break;
				case 4:
					setHT(54);
					setHP(getHP() + 19);
					break;
				case 5:
					setHT(84);
					setHP(getHP() + 30);
					break;
				case 6:
					wandHeal(wandLevel);
					break;
			}

			if (Actor.chars().contains(this) && tier >= 3){
				Bestiary.setSeen(WardSentry.class);
			}

			if (tier < 6){
				tier++;
				viewDistance++;
				if (getSprite() != null){
					((WardSprite) getSprite()).updateTier(tier);
					getSprite().place(pos);
				}
				GameScene.updateFog(pos, viewDistance+1);
			}

		}

		//this class is used so that wards and sentries can have two entries in the Bestiary
		public static class WardSentry extends Ward{
		};

		public void wandHeal( int wandLevel ){
			wandHeal( wandLevel, 1f );
		}

		public void wandHeal( int wandLevel, float healFactor ){
			if (this.wandLevel < wandLevel){
				this.wandLevel = wandLevel;
			}

			int heal;
			switch(tier){
				default:
					return;
				case 2:
					heal = Math.round(1 * healFactor);
					break;
				case 3:
					heal = Math.round(Random.IntRange(1, 2) * healFactor);
					break;
				case 4:
					heal = Math.round(9 * healFactor); //9/5 1.8
					break;
				case 5:
					heal = Math.round(12 * healFactor); //12/6, 2
					break;
				case 6:
					heal = Math.round(16 * healFactor); //16/7, 2.28
					break;
			}

			if (tier <= 3){
				totalZaps = (Math.max(0, totalZaps-heal));
			} else {
				setHP(Math.min(getHT(), getHP() + heal));
			}
			if (getSprite() != null) getSprite().showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(heal), FloatingText.HEALING);

		}

		@Override
		public int defenseSkill(Char enemy) {
			if (tier > 3){
				defenseSkill = 4 + Dungeon.scalingDepth();
			}
			return super.defenseSkill(enemy);
		}

		@Override
		public int drRoll() {
			int dr = super.drRoll();
			if (tier > 3){
				return dr + Math.round(Random.NormalIntRange(0, 3 + Dungeon.scalingDepth()/2) / (7f - tier));
			} else {
				return dr;
			}
		}

		@Override
		protected boolean canAttack( Char enemy ) {
			return new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos;
		}

		@Override
		protected boolean doAttack(Char enemy) {
			boolean visible = fieldOfView[pos] || fieldOfView[enemy.pos];
			if (visible) {
				getSprite().zap( enemy.pos );
			} else {
				zap();
			}

			return !visible;
		}

		private void zap() {
			spend( 1f );

			//always hits
			int dmg = Hero.heroDamageIntRange( 2 + wandLevel, 8 + 4*wandLevel );
			Char enemy = this.enemy;
			enemy.damage( dmg, this );
			if (enemy.isAlive()){
				if (owner != null) {
					//TODO: check this
					Wand.wandProc(enemy, wandLevel, 1, owner);
				}
			}

			if (!enemy.isAlive() && enemy instanceof Hero) {
				Badges.validateDeathFromFriendlyMagic();
				GLog.n(Messages.capitalize(Messages.get( this, "kill", name() )));
				Dungeon.fail( WandOfWarding.class );
			}

			totalZaps++;
			switch(tier){
				case 1: case 2: case 3: default:
					if (totalZaps >= (2*tier-1)){
						die(new DamageCause( this));
					}
					break;
				case 4:
					damage(5, this);
					break;
				case 5:
					damage(6, this);
					break;
				case 6:
					damage(7, this);
					break;
			}
		}

		public void onZapComplete() {
			zap();
			next();
		}

		@Override
		protected boolean getCloser(int target) {
			return false;
		}

		@Override
		protected boolean getFurther(int target) {
			return false;
		}

		@Override
		public CharSprite sprite() {
			WardSprite sprite = (WardSprite) super.sprite();
			sprite.linkVisuals(this);
			return sprite;
		}

		@Override
		public void updateSpriteState() {
			super.updateSpriteState();
			((WardSprite) getSprite()).updateTier(tier);
			getSprite().place(pos);
		}
		
		@Override
		public void destroy() {
			super.destroy();
			Dungeon.observe();
			GameScene.updateFog(pos, viewDistance+1);
		}
		
		@Override
		public boolean canInteract(Char c) {
			return true;
		}

		@Override
		public boolean interact( Char c ) {
			if (!(c instanceof Hero)){
				return true;
			}
			GameScene.show(new WndOptions((Hero) c, sprite(),
					Messages.get(Ward.this, "dismiss_title"),
					Messages.get(Ward.this, "dismiss_body"),
					Messages.get(Ward.this, "dismiss_confirm"),
					Messages.get(Ward.this, "dismiss_cancel") ){
				@Override
				protected void onSelect(int index) {
					if (index == 0){
						die(  new DamageCause( null)); //use `(Hero)c`?
					}
				}
			});
			return true;
		}

		@Override
		public LocalizedString description() {
			if (!Actor.chars().contains(this)){
				//for viewing in the journal
				if (tier < 4){
					return Messages.get(this, "desc_generic_ward");
				} else {
					return Messages.get(this, "desc_generic_sentry");
				}
			} else {
				return Messages.get(this, "desc_" + tier, 2 + wandLevel, 8 + 4 * wandLevel, tier);
			}
		}
		
		{
			immunities.add( Sleep.class );
			immunities.add( Terror.class );
			immunities.add( Dread.class );
			immunities.add( Vertigo.class );
			immunities.add( AllyBuff.class );
		}

		private static final String TIER = "tier";
		private static final String WAND_LEVEL = "wand_level";
		private static final String TOTAL_ZAPS = "total_zaps";
		private static final String OWNER_UUID = "owner_uuid";
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(TIER, tier);
			bundle.put(WAND_LEVEL, wandLevel);
			bundle.put(TOTAL_ZAPS, totalZaps);
			if (owner != null) {
				bundle.put(OWNER_UUID, owner.uuid);
			}
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			tier = bundle.getInt(TIER);
			viewDistance = 3 + tier;
			wandLevel = bundle.getInt(WAND_LEVEL);
			totalZaps = bundle.getInt(TOTAL_ZAPS);
			if (bundle.contains(OWNER_UUID)) {
				String uuid = bundle.getString(OWNER_UUID);
				for (Hero hero: Dungeon.heroes) {
				if (hero != null && hero.uuid.equals(uuid)){
					owner = hero;
				}
				}
			}
		}
	}
}
