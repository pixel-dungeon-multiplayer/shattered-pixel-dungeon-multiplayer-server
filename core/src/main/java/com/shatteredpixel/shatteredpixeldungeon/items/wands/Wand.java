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

import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Degrade;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LostInventory;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Recharging;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Regeneration;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ScrollEmpower;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.SoulMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.WildMagic;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.DivineSense;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.GuidingLight;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TalismanOfForesight;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.MagicalHolster;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ShardOfOblivion;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.WondrousResin;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import io.github.pixeldungeonmultiplayer.shattered.server.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class Wand extends Item {

	public static final String AC_ZAP	= "ZAP";

	private static final float TIME_TO_ZAP	= 1f;
	
	public int maxCharges = initialCharges();
	private int curCharges = maxCharges;
	public float partialCharge = 0f;
	
	protected Charger charger;
	
	public boolean curChargeKnown = false;
	
	public boolean curseInfusionBonus = false;
	public int resinBonus = 0;

	private static final int USES_TO_ID = 10;
	private float usesLeftToID = USES_TO_ID;
	private float availableUsesToID = USES_TO_ID/2f;

	protected int collisionProperties = Ballistica.MAGIC_BOLT;
    public MagesStaff staffToUpdate = null;
	{
		defaultAction = AC_ZAP;
		usesTargeting = true;
		bones = true;
	}
	
	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
        if ((getCurCharges() > 0 || !curChargeKnown) && canUse(hero)) {
			actions.add( AC_ZAP );
		}

		return actions;
	}
	
	@Override
	public void execute( Hero hero, String action ) {

		super.execute( hero, action );

		if (action.equals( AC_ZAP )) {
			
			curUser = hero;
			curItem = this;
			GameScene.selectCell(hero, zapper);
			
		}
	}

	@Override
	public int targetingPos(@NotNull Hero user, int dst) {
		if (cursed && cursedKnown){
			return new Ballistica(user.pos, dst, Ballistica.MAGIC_BOLT).collisionPos;
		} else {
			return new Ballistica(user.pos, dst, collisionProperties).collisionPos;
		}
	}

	public void onZap(Ballistica attack){};
	public void onZap(Ballistica attack, Hero hero){
		onZap(attack);
	};

	public abstract void onHit( MagesStaff staff, Char attacker, Char defender, int damage);

	//not affected by enchantment proc chance changers
	public static float procChanceMultiplier( Char attacker ){
		if (attacker.buff(Talent.EmpoweredStrikeTracker.class) != null){
			return 1f + ((Hero)attacker).pointsInTalent(Talent.EMPOWERED_STRIKE)/2f;
		}
		return 1f;
	}

	public boolean tryToZap( Hero owner, int target ){

		if (owner.buff(WildMagic.WildMagicTracker.class) == null && owner.buff(MagicImmune.class) != null){
			GLog.w( Messages.get(this, "no_magic") );
			return false;
		}

		//if we're using wild magic, then assume we have charges
		if ( owner.buff(WildMagic.WildMagicTracker.class) != null || getCurCharges() >= chargesPerCast()){
			return true;
		} else {
			GLog.w(Messages.get(this, "fizzles"));
			return false;
		}
	}

	@Override
	public boolean collect( Bag container ) {
		if (super.collect( container )) {
			if (container.owner != null) {
				if (container instanceof MagicalHolster)
					charge( container.owner, ((MagicalHolster) container).HOLSTER_SCALE_FACTOR );
				else
					charge( container.owner );
			}
			return true;
		} else {
			return false;
		}
	}

	public void gainCharge( float amt ){
		gainCharge( amt, false );
	}

	public void gainCharge( float amt, boolean overcharge ){
		partialCharge += amt;
		while (partialCharge >= 1) {
			if (overcharge) setCurCharges(Math.min(maxCharges+(int)amt, getCurCharges() +1));
			else setCurCharges(Math.min(maxCharges, getCurCharges() +1));
			partialCharge--;
			updateQuickslot();
		}
	}
	
	public void charge( Char owner ) {
		if (charger == null) charger = new Charger();
		charger.attachTo( owner );
	}

	public void charge( Char owner, float chargeScaleFactor ){
		charge( owner );
		charger.setScaleFactor( chargeScaleFactor );
	}

	protected void wandProc(Char target, int chargesUsed, Hero hero){
		wandProc(target, buffedLvl(), chargesUsed, hero);
	}

	//TODO Consider externalizing char awareness buff
	protected static void wandProc(Char target, int wandLevel, int chargesUsed, Hero hero){
		if (hero.hasTalent(Talent.ARCANE_VISION)) {
			int dur = 5 + 5*hero.pointsInTalent(Talent.ARCANE_VISION);
			Buff.append(hero, TalismanOfForesight.CharAwareness.class, dur).charID = target.id();
		}

		if (!(target instanceof Hero)&&
				hero.subClass == HeroSubClass.WARLOCK &&
				//standard 1 - 0.92^x chance, plus 7%. Starts at 15%
				Random.Float() > (Math.pow(0.92f, (wandLevel*chargesUsed)+1) - 0.07f)){
			SoulMark.prolong(target, SoulMark.class, SoulMark.DURATION + wandLevel);
		}

		if (hero.subClass == HeroSubClass.PRIEST && target.buff(GuidingLight.Illuminated.class) != null) {
			target.buff(GuidingLight.Illuminated.class).detach();
			target.damage(hero.lvl+5, new Char.DamageCause(GuidingLight.INSTANCE, hero));
		}

		if (target.alignment != Char.Alignment.ALLY
				&& hero.heroClass != HeroClass.CLERIC
				&& hero.hasTalent(Talent.SEARING_LIGHT)
				&& hero.buff(Talent.SearingLightCooldown.class) == null){
			Buff.affect(target, GuidingLight.Illuminated.class);
			Buff.affect(hero, Talent.SearingLightCooldown.class, 20f);
		}

		if (target.alignment != Char.Alignment.ALLY
				&& hero.heroClass != HeroClass.CLERIC
				&& hero.hasTalent(Talent.SUNRAY)){
			// 15/25% chance
			if (Random.Int(20) < 1 + 2*hero.pointsInTalent(Talent.SUNRAY)){
				Buff.prolong(target, Blindness.class, 4f);
			}
		}
	}

	@Override
	public void onDetach( ) {
		stopCharging();
	}

	public void stopCharging() {
		if (charger != null){
			Charger oldCharger = charger;
			charger = null;
			oldCharger.detach();
		}
	}
	
	public void level( int value, Hero hero) {
		super.level( value );
		updateLevel(hero);
	}
	
	@Override
	public Item identify( boolean byHero, Hero hero ) {
		
		curChargeKnown = true;
		super.identify(byHero, hero);
		
		updateQuickslot();
		
		return this;
	}

	public void setIDReady(){
		usesLeftToID = -1;
	}

	public boolean readyToIdentify(){
		return !isIdentified() && usesLeftToID <= 0;
	}

	public void onHeroGainExp( float levelPercent, Hero hero ){
		levelPercent *= Talent.itemIDSpeedFactor(hero, this);
		if (!isIdentified() && availableUsesToID <= USES_TO_ID/2f) {
			//gains enough uses to ID over 1 level
			availableUsesToID = Math.min(USES_TO_ID/2f, availableUsesToID + levelPercent * USES_TO_ID/2f);
		}
	}

	@Override
	public LocalizedString info(Hero hero) {
		LocalizedString desc = super.info(hero);

		LocalizedString.concat(desc, "\n\n", statsDesc(hero));

		if (resinBonus == 1){
			desc = LocalizedString.concat(desc, LocalizedString.concat("\n\n", Messages.get(Wand.class, "resin_one")));
		} else if (resinBonus > 1){
			desc = LocalizedString.concat(desc, LocalizedString.concat("\n\n", Messages.get(Wand.class, "resin_many", resinBonus)));
		}

		if (cursed && cursedKnown) {
			desc = LocalizedString.concat(desc, LocalizedString.concat("\n\n", Messages.get(Wand.class, "cursed")));
		} else if (!isIdentified() && cursedKnown){
			desc = LocalizedString.concat(desc, LocalizedString.concat("\n\n", Messages.get(Wand.class, "not_cursed")));
		}

		if (hero != null && hero.subClass == HeroSubClass.BATTLEMAGE){
			desc = LocalizedString.concat(desc, LocalizedString.concat("\n\n", Messages.get(this, "bmage_desc")));
		}

		return desc;
	}

	public LocalizedString statsDesc(Hero hero){
		return statsDesc();
	}
	protected LocalizedString statsDesc(){
		return Messages.get(this, "stats_desc");
	}
	@Deprecated
	public String upgradeStat1(int level){
		return null;
	}
	@Deprecated
	public String upgradeStat2(int level){
		return null;
	}
	@Deprecated
	public String upgradeStat3(int level){
		return null;
	}
	public String upgradeStat1(int level, Hero hero){
		return upgradeStat1(level);
	}
	public String upgradeStat2(int level, Hero hero){
		return upgradeStat2(level);
	}
	public String upgradeStat3(int level, Hero hero){
		return upgradeStat3(level);
	}



	@Override
	public boolean isIdentified() {
		return super.isIdentified() && curChargeKnown;
	}
	
	@Override
	public @Nullable LocalizedString status() {
		if (levelKnown) {
			return LocalizedString.raw((curChargeKnown ? getCurCharges() : "?") + "/" + maxCharges);
		} else {
			return null;
		}
	}
	
	@Override
	public int level(Hero hero) {
		if (!cursed && curseInfusionBonus){
			curseInfusionBonus = false;
			updateLevel(hero);
		}
		int level = super.level(hero);
		if (curseInfusionBonus) level += 1 + level/6;
		level += resinBonus;
		return level;
	}
	
	@Override
	public Item upgrade() {

		super.upgrade();

		if (Random.Int(3) == 0) {
			cursed = false;
		}

		if (resinBonus > 0){
			resinBonus--;
		}
		Hero owner = findOwner();
		if (owner != null) {
			updateLevel(owner);
		}
		setCurCharges(Math.min( getCurCharges() + 1, maxCharges ));
		updateQuickslot();
		
		return this;
	}
	
	@Override
	public Item degrade() {
		super.degrade();
		Hero owner = findOwner();
		if (owner != null) {
			updateLevel(owner);
		}
		updateQuickslot();
		
		return this;
	}

	@Override
	public int buffedLvl(Char owner) {
		int lvl = super.buffedLvl();

		if (charger != null && charger.target != null) {

			//inside staff, still need to apply degradation
			if (charger.target instanceof Hero
					&& !((Hero) charger.target).belongings.contains(this)
					&& charger.target.buff( Degrade.class ) != null){
				lvl = Degrade.reduceLevel(lvl);
			}

			if (charger.target.buff(ScrollEmpower.class) != null){
				lvl += 2;
			}

			if (getCurCharges() == 1 && charger.target instanceof Hero && ((Hero)charger.target).hasTalent(Talent.DESPERATE_POWER)){
				lvl += ((Hero)charger.target).pointsInTalent(Talent.DESPERATE_POWER);
			}

			if (charger.target.buff(WildMagic.WildMagicTracker.class) != null){
				int bonus = 4 + ((Hero)charger.target).pointsInTalent(Talent.WILD_POWER);
				if (Random.Int(2) == 0) bonus++;
				bonus /= 2; // +2/+2.5/+3/+3.5/+4 at 0/1/2/3/4 talent points

				int maxBonusLevel = 3 + ((Hero)charger.target).pointsInTalent(Talent.WILD_POWER);
				if (lvl < maxBonusLevel) {
					lvl = Math.min(lvl + bonus, maxBonusLevel);
				}
			}

			WandOfMagicMissile.MagicCharge buff = charger.target.buff(WandOfMagicMissile.MagicCharge.class);
			if (buff != null && buff.level() > lvl){
				return buff.level();
			}
		}
		return lvl;
	}

	public void updateLevel(Hero hero) {
		maxCharges = Math.min( initialCharges() + level(hero), 10 );
		setCurCharges(Math.min(getCurCharges(), maxCharges ));
	}
	
	public int initialCharges() {
		return 2;
	}

	protected int chargesPerCast() {
		return 1;
	}
	
	public void fx(Ballistica bolt, Callback callback) {
		MagicMissile.boltFromChar( curUser.getSprite().parent,
				MagicMissile.MAGIC_MISSILE,
                curUser.getSprite(),
				bolt.collisionPos,
				callback);
		Sample.INSTANCE.play( Assets.Sounds.ZAP );
	}

	public void staffFx( MagesStaff.StaffParticle particle ){
		particle.color(0xFFFFFF); particle.am = 0.3f;
		particle.setLifespan( 1f);
		particle.speed.polar( Random.Float(PointF.PI2), 2f );
		particle.setSize( 1f, 2f );
		particle.radiateXY(0.5f);
	}

	public void wandUsed(Hero hero) {
		if (!isIdentified()) {
			float uses = Math.min( availableUsesToID, Talent.itemIDSpeedFactor(hero, this) );
			availableUsesToID -= uses;
			usesLeftToID -= uses;
			if (usesLeftToID <= 0 || hero.pointsInTalent(Talent.SCHOLARS_INTUITION) == 2) {
				if (ShardOfOblivion.passiveIDDisabled()){
					if (usesLeftToID > -1){
						GLog.p(Messages.get(ShardOfOblivion.class, "identify_ready", name()));
					}
					usesLeftToID = -1;
				} else {
					identify(hero);
					GLog.p(Messages.get(Wand.class, "identify"));
					Badges.validateItemLevelAquired(this);
				}
			}
			if (ShardOfOblivion.passiveIDDisabled()){
				Buff.prolong(curUser, ShardOfOblivion.WandUseTracker.class, 50f);
			}
		}

		//inside staff
		if (charger != null && charger.target instanceof Hero && !hero.belongings.contains(this)){
			if (hero.hasTalent(Talent.EXCESS_CHARGE) && getCurCharges() >= maxCharges){
				int shieldToGive = Math.round(buffedLvl()*0.67f*hero.pointsInTalent(Talent.EXCESS_CHARGE));
				Buff.affect(hero, Barrier.class).setShield(shieldToGive);
				hero.getSprite().showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(shieldToGive), FloatingText.SHIELDING);
			}
		}
		
		setCurCharges(getCurCharges() - (cursed ? 1 : chargesPerCast()));

		//remove magic charge at a higher priority, if we are benefiting from it are and not the
		//wand that just applied it
		WandOfMagicMissile.MagicCharge buff = curUser.buff(WandOfMagicMissile.MagicCharge.class);
		if (buff != null
				&& buff.wandJustApplied() != this
				&& buff.level() == buffedLvl()
				&& buffedLvl() > super.buffedLvl()){
			buff.detach();
		} else {
			ScrollEmpower empower = curUser.buff(ScrollEmpower.class);
			if (empower != null){
				empower.use();
			}
		}

		//If hero owns wand but it isn't in belongings it must be in the staff
		if (hero.hasTalent(Talent.EMPOWERED_STRIKE)
				&& charger != null && charger.target instanceof Hero
				&& !hero.belongings.contains(this)){

			Buff.prolong(hero, Talent.EmpoweredStrikeTracker.class, 10f);
		}

		if (hero.hasTalent(Talent.LINGERING_MAGIC)
				&& charger != null && charger.target instanceof Hero){

			Buff.prolong(hero, Talent.LingeringMagicTracker.class, 5f);
		}

		if (hero.heroClass != HeroClass.CLERIC
				&& hero.hasTalent(Talent.DIVINE_SENSE)){
			Buff.prolong(hero, DivineSense.DivineSenseTracker.class, hero.cooldown()+1);
		}

		// 10/20/30%
		if (hero.heroClass != HeroClass.CLERIC
				&& hero.hasTalent(Talent.CLEANSE)
				&& Random.Int(10) < hero.pointsInTalent(Talent.CLEANSE)){
			boolean removed = false;
			for (Buff b : hero.buffs()) {
				if (b.type == Buff.buffType.NEGATIVE
						&& !(b instanceof LostInventory)) {
					b.detach();
					removed = true;
				}
			}
			if (removed) new Flare( 6, 32 ).color(0xFF4CD2, true).show( hero.getSprite(), 2f );
		}

		//TODO: might want to move this somewhere
		Hero owner = findOwner();
		if (owner != null) {
			Invisibility.dispel(owner);
		}
		updateQuickslot();

		curUser.spendAndNext( TIME_TO_ZAP );
	}
	
	@Override
	public Item random() {
		//+0: 66.67% (2/3)
		//+1: 26.67% (4/15)
		//+2: 6.67%  (1/15)
		int n = 0;
		if (Random.Int(3) == 0) {
			n++;
			if (Random.Int(5) == 0){
				n++;
			}
		}
		level(n);
		setCurCharges(getCurCharges() + n);
		
		//30% chance to be cursed
		if (Random.Float() < 0.3f) {
			cursed = true;
		}

		return this;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		if (resinBonus == 0) return null;

		return new ItemSprite.Glowing(0xFFFFFF, 1f/(float)resinBonus);
	}

	@Contract(pure = true)
    @Override
	public int value() {
		int price = 75;
		if (cursed && cursedKnown) {
			price /= 2;
		}
		if (levelKnown) {
			if (level() > 0) {
				price *= (level() + 1);
			} else if (level() < 0) {
				price /= (1 - level());
			}
		}
		if (price < 1) {
			price = 1;
		}
		return price;
	}
	
	private static final String USES_LEFT_TO_ID     = "uses_left_to_id";
	private static final String AVAILABLE_USES      = "available_uses";
	private static final String CUR_CHARGES         = "curCharges";
	private static final String CUR_CHARGE_KNOWN    = "curChargeKnown";
	private static final String PARTIALCHARGE       = "partialCharge";
	private static final String CURSE_INFUSION_BONUS= "curse_infusion_bonus";
	private static final String RESIN_BONUS         = "resin_bonus";

	@Override
	public void storeInBundle(@NotNull Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( USES_LEFT_TO_ID, usesLeftToID );
		bundle.put( AVAILABLE_USES, availableUsesToID );
		bundle.put( CUR_CHARGES, getCurCharges());
		bundle.put( CUR_CHARGE_KNOWN, curChargeKnown );
		bundle.put( PARTIALCHARGE , partialCharge );
		bundle.put( CURSE_INFUSION_BONUS, curseInfusionBonus );
		bundle.put( RESIN_BONUS, resinBonus );
	}
	
	@Override
	public void restoreFromBundle(@NotNull Bundle bundle ) {
		super.restoreFromBundle( bundle );
		usesLeftToID = bundle.getInt( USES_LEFT_TO_ID );
		availableUsesToID = bundle.getInt( AVAILABLE_USES );
		curseInfusionBonus = bundle.getBoolean(CURSE_INFUSION_BONUS);
		resinBonus = bundle.getInt(RESIN_BONUS);
		//TODO: check this
		//updateLevel();

		setCurCharges(bundle.getInt( CUR_CHARGES ));
		curChargeKnown = bundle.getBoolean( CUR_CHARGE_KNOWN );
		partialCharge = bundle.getFloat( PARTIALCHARGE );
	}
	
	@Override
	public void reset() {
		super.reset();
		usesLeftToID = USES_TO_ID;
		availableUsesToID = USES_TO_ID/2f;
	}

	public int collisionProperties(int target){
		if (cursed)     return Ballistica.MAGIC_BOLT;
		else            return collisionProperties;
	}

	public int getCurCharges() {
		return curCharges;
	}

	public void setCurCharges(int curCharges) {
		this.curCharges = curCharges;
		updateQuickslot();
	}

	public static class PlaceHolder extends Wand {

		{
			image = ItemSpriteSheet.WAND_HOLDER;
		}

		@Override
		public boolean isSimilar(Item item) {
			return item instanceof Wand;
		}

		@Override
		public void onZap(Ballistica attack) {}
		public void onHit(MagesStaff staff, Char attacker, Char defender, int damage) {}

		@Override
		public LocalizedString info() {
			return LocalizedString.EMPTY;
		}
	}
	
	protected static CellSelector.Listener zapper = new  CellSelector.Listener() {
		
		@Override
		public void onSelect( Integer target ) {
			if (target != null) {
				
				//FIXME this safety check shouldn't be necessary
				//it would be better to eliminate the curItem static variable.
				final Wand curWand;
				if (curItem instanceof Wand) {
					curWand = (Wand) Wand.curItem;
				} else {
					return;
				}

				final Ballistica shot = new Ballistica( curUser.pos, target, curWand.collisionProperties(target));
				int cell = shot.collisionPos;
				
				if (target == curUser.pos || cell == curUser.pos) {
					if (target == curUser.pos && curUser.hasTalent(Talent.SHIELD_BATTERY)){

						if (curUser.buff(MagicImmune.class) != null){
							GLog.w( Messages.get(Wand.class, "no_magic") );
							return;
						}

						if (curWand.getCurCharges() == 0){
							GLog.w( Messages.get(Wand.class, "fizzles") );
							return;
						}

						float shield = curUser.getHT() * (0.04f* curWand.getCurCharges());
						if (curUser.pointsInTalent(Talent.SHIELD_BATTERY) == 2) shield *= 1.5f;
						Buff.affect(curUser, Barrier.class).setShield(Math.round(shield));
						curUser.getSprite().showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(Math.round(shield)), FloatingText.SHIELDING);
						curWand.setCurCharges(0);
						curUser.getSprite().operate(curUser.pos);
						Sample.INSTANCE.play(Assets.Sounds.CHARGEUP);
						ScrollOfRecharging.charge(curUser);
						curWand.updateQuickslot(curUser);
						curUser.spendAndNext(Actor.TICK);
						return;
					}
					GLog.i( Messages.get(Wand.class, "self_target") );
					return;
				}

				curUser.getSprite().zap(cell);

				//attempts to target the cell aimed at if something is there, otherwise targets the collision pos.
				if (Actor.findChar(target) != null)
					QuickSlotButton.target(Actor.findChar(target));
				else
					QuickSlotButton.target(Actor.findChar(cell));
				
				if (curWand.tryToZap(curUser, target)) {
					
					curUser.busy();

					//backup barrier logic
					//This triggers before the wand zap, mostly so the barrier helps vs skeletons
					if (curUser.hasTalent(Talent.BACKUP_BARRIER)
							&& curWand.getCurCharges() == curWand.chargesPerCast()
							&& curWand.charger != null && curWand.charger.target == curUser){

						//regular. If hero owns wand but it isn't in belongings it must be in the staff
						if (curUser.heroClass == HeroClass.MAGE && !curUser.belongings.contains(curWand)){
							//grants 3/5 shielding
							int shieldToGive = 1 + 2 * getOwner().pointsInTalent(Talent.BACKUP_BARRIER);
							Buff.affect(getOwner(), Barrier.class).setShield(shieldToGive);
							getOwner().getSprite().showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(shieldToGive), FloatingText.SHIELDING);

						//metamorphed. Triggers if wand is highest level hero has
						} else if (curUser.heroClass != HeroClass.MAGE) {
							boolean highest = true;
							for (Item i : curUser.belongings.getAllItems(Wand.class)){
								if (i.level() > curWand.level()){
									highest = false;
								}
							}
							if (highest){
								//grants 3/5 shielding
								int shieldToGive = 1 + 2 * getOwner().pointsInTalent(Talent.BACKUP_BARRIER);
								Buff.affect(getOwner(), Barrier.class).setShield(shieldToGive);
								getOwner().getSprite().showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(shieldToGive), FloatingText.SHIELDING);
							}
						}
					}
					
					if (curWand.cursed){
						if (!curWand.cursedKnown){
							GLog.n(Messages.get(Wand.class, "curse_discover", curWand.name()));
						}
						CursedWand.cursedZap(curWand,
								curUser,
								new Ballistica(curUser.pos, target, Ballistica.MAGIC_BOLT),
								new Callback() {
									@Override
									public void call() {
										curWand.wandUsed(getOwner());
									}
								});
					} else {
						curWand.fx(shot, new Callback() {
							public void call() {
								curWand.onZap(shot, getOwner());
								if (Random.Float() < WondrousResin.extraCurseEffectChance(getOwner())){
									WondrousResin.forcePositive = true;
									CursedWand.cursedZap(curWand,
											curUser,
											new Ballistica(curUser.pos, target, Ballistica.MAGIC_BOLT), new Callback() {
												@Override
												public void call() {
													WondrousResin.forcePositive = false;
													curWand.wandUsed(getOwner());
												}
											});
								} else {
									curWand.wandUsed(getOwner());
								}
							}
						});

					}
					curWand.cursedKnown = true;
					curWand.sendSelfUpdate(getOwner());

                    if (curWand.staffToUpdate != null) {
                        curWand.staffToUpdate.sendSelfUpdate(getOwner());
                        curWand.staffToUpdate = null;
                    }
                }

			}
		}
		
		@Override
		public LocalizedString prompt() {
			return Messages.get(Wand.class, "prompt");
		}
	};
	
	public class Charger extends Buff {
		
		private static final float BASE_CHARGE_DELAY = 10f;
		private static final float SCALING_CHARGE_ADDITION = 40f;
		private static final float NORMAL_SCALE_FACTOR = 0.875f;

		private static final float CHARGE_BUFF_BONUS = 0.25f;

		float scalingFactor = NORMAL_SCALE_FACTOR;

		@Override
		public boolean attachTo( Char target ) {
			if (super.attachTo( target )) {
				//if we're loading in and the hero has partially spent a turn, delay for 1 turn
				if (target instanceof Hero && (!Arrays.asList(Dungeon.heroes).contains(target)) && cooldown() == 0 && target.cooldown() > 0) {
					spend(TICK);
				}
				return true;
			}
			return false;
		}

		@Override
		public void detach() {
			super.detach();
			Wand.this.stopCharging();
		}

		@Override
		public boolean act() {
			if (getCurCharges() < maxCharges && target.buff(MagicImmune.class) == null)
				recharge((Hero) target);
			
			while (partialCharge >= 1 && getCurCharges() < maxCharges) {
				partialCharge--;
				setCurCharges(getCurCharges() + 1);
				updateQuickslot();
			}
			
			if (getCurCharges() == maxCharges){
				partialCharge = 0;
			}
			
			spend( TICK );
			
			return true;
		}

		private void recharge(Hero hero){
			int missingCharges = maxCharges - getCurCharges();
			missingCharges = Math.max(0, missingCharges);

			float turnsToCharge = (float) (BASE_CHARGE_DELAY
					+ (SCALING_CHARGE_ADDITION * Math.pow(scalingFactor, missingCharges)));

			if (Regeneration.regenOn(hero))
				partialCharge += (1f/turnsToCharge) * RingOfEnergy.wandChargeMultiplier(target);

			for (Recharging bonus : target.buffs(Recharging.class)){
				if (bonus != null && bonus.remainder() > 0f) {
					partialCharge += CHARGE_BUFF_BONUS * bonus.remainder();
				}
			}
		}
		
		public Wand wand(){
			return Wand.this;
		}

		public void gainCharge(float charge){
			if (getCurCharges() < maxCharges) {
				partialCharge += charge;
				while (partialCharge >= 1f) {
					setCurCharges(getCurCharges() + 1);
					partialCharge--;
				}
				if (getCurCharges() >= maxCharges){
					partialCharge = 0;
					setCurCharges(maxCharges);
				}
				updateQuickslot();
			}
		}

		private void setScaleFactor(float value){
			this.scalingFactor = value;
		}
	}
}
