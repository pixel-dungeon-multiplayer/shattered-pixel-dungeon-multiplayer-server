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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.PrismaticGuard;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.AntiMagic;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfAccuracy;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEvasion;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import io.github.pixeldungeonmultiplayer.shattered.server.network.Server;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.PrismaticSprite;
import io.github.pixeldungeonmultiplayer.shattered.server.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class PrismaticImage extends NPC {

	{
		spriteClass = PrismaticSprite.class;

		setHP(setHT(10));
		defenseSkill = 1;

		alignment = Alignment.ALLY;
		intelligentAlly = true;
		state = HUNTING;

		WANDERING = new Wandering();

		//before other mobs
		actPriority = MOB_PRIO + 1;
	}

	private Hero hero;
	private String heroUUID;
	public int armTier;

	private int deathTimer = -1;

	@Override
	protected boolean act() {

		if (!isAlive()){
			deathTimer--;

			if (deathTimer > 0) {
				getSprite().alpha((deathTimer + 3) / 8f);
				spend(TICK);
			} else {
				destroy();
				getSprite().die();
			}
			return true;
		}

		if (deathTimer != -1){
			if (paralysed == 0) getSprite().remove(CharSprite.State.PARALYSED);
			deathTimer = -1;
			getSprite().resetColor();
		}

		if ( hero == null ){
			findHero();
			if ( hero == null ){
				destroy();
				getSprite().die();
				return true;
			}
		}

		if (hero.tier() != armTier){
			armTier = hero.tier();
			((PrismaticSprite) getSprite()).updateArmor( armTier );
		}

		return super.act();
	}

	@Override
	public void die(@NotNull DamageCause cause) {
		if (deathTimer == -1) {
			if (cause.getCause() == Chasm.class){
				super.die( cause );
			} else {
				deathTimer = 5;
				getSprite().add(CharSprite.State.PARALYSED);
			}
		}
	}

	@Override
	public boolean isActive() {
		return isAlive() || deathTimer > 0;
	}

	private static final String HEROUUID	= "hero_id";
	private static final String TIMER	= "timer";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put(HEROUUID , heroUUID );
		bundle.put( TIMER, deathTimer );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		heroUUID = bundle.getString( HEROUUID );
		hero = Server.findAndLoadHeroByUUID(heroUUID);
		deathTimer = bundle.getInt( TIMER );
	}

	public void duplicate( Hero hero, int HP ) {
		this.hero = hero;
		heroUUID = this.hero.uuid;
		this.setHP(HP);
		setHT(PrismaticGuard.maxHP( hero ));
	}

	@Override
	public int damageRoll() {
		if (hero != null) {
			return Random.NormalIntRange( 2 + hero.lvl/4, 4 + hero.lvl/2 );
		} else {
			return Random.NormalIntRange( 2, 4 );
		}
	}

	@Override
	public int attackSkill( Char target ) {
		if (hero != null) {
			//same base attack skill as hero, benefits from accuracy ring
			return (int)((9 + hero.lvl) * RingOfAccuracy.accuracyMultiplier(hero));
		} else {
			return 0;
		}
	}

	@Override
	public int defenseSkill(Char enemy) {
		if (hero != null) {
			int baseEvasion = 4 + hero.lvl;
			int heroEvasion = (int)((4 + hero.lvl) * RingOfEvasion.evasionMultiplier( hero ));
			if (hero.belongings.armor() != null){
				heroEvasion = (int)hero.belongings.armor().evasionFactor(this, heroEvasion);
			}

			//if the hero has more/less evasion, 50% of it is applied
			//includes ring of evasion and armor boosts
			return super.defenseSkill(enemy) * (baseEvasion + heroEvasion) / 2;
		} else {
			return 0;
		}
	}

	@Override
	public int drRoll() {
		int dr = super.drRoll();
		if (hero != null){
			return dr + hero.drRoll();
		} else {
			return dr;
		}
	}

	@Override
	public int defenseProc(Char enemy, int damage) {
		if (hero != null && hero.belongings.armor() != null){
			damage = hero.belongings.armor().proc( enemy, this, damage );
		}
		return super.defenseProc(enemy, damage);
	}

	@Override
	public void damage(int dmg, @NotNull DamageCause source) {
		Object src = source.getCause();

		//TODO improve this when I have proper damage source logic
		if (hero != null && hero.belongings.armor() != null && hero.belongings.armor().hasGlyph(AntiMagic.class, this)
				&& AntiMagic.RESISTS.contains(src.getClass())){
			dmg -= AntiMagic.drRoll(hero, hero.belongings.armor().buffedLvl());
			dmg = Math.max(dmg, 0);
		}

		super.damage(dmg, source);
	}

	@Override
	public float speed() {
        if (hero != null && hero.belongings.armor() != null) {
            return hero.belongings.armor().speedFactor(this, super.speed());
        }
		return super.speed();
    }

    public int glyphLevel(Class<? extends Armor.Glyph> cls) {
		if (hero != null){
			return Math.max(super.glyphLevel(cls), hero.glyphLevel(cls));
		} else {
			return super.glyphLevel(cls);
		}
	}

	@Override
	public int attackProc( Char enemy, int damage ) {

		if (enemy instanceof Mob) {
			((Mob)enemy).aggro( this );
		}

		return super.attackProc( enemy, damage );
	}

	@Override
	public CharSprite sprite() {
		CharSprite s = super.sprite();

		findHero();
		if (hero != null) {
			armTier = hero.tier();
		} else {
			armTier = 1;
		}
		((PrismaticSprite)s).updateArmor( armTier );
		return s;
	}

	{
		immunities.add( ToxicGas.class );
		immunities.add( CorrosiveGas.class );
		immunities.add( Burning.class );
		immunities.add( AllyBuff.class );
	}

	private class Wandering extends Mob.Wandering{

		@Override
		public boolean act(boolean enemyInFOV, boolean justAlerted) {
			if (!enemyInFOV){
				Buff.affect(hero, PrismaticGuard.class).set( PrismaticImage.this );
				destroy();
				CellEmitter.get(pos).start( Speck.factory(Speck.LIGHT), 0.2f, 3 );
				getSprite().die();
				Sample.INSTANCE.play( Assets.Sounds.TELEPORT );
				return true;
			} else {
				return super.act(enemyInFOV, justAlerted);
			}
		}

	}
	private void findHero(){
		if (hero == null){
			hero = Dungeon.getHeroByUUID(heroUUID);
		}

	}
	public Hero getHero() {
		if (hero == null){
			hero = Server.findAndLoadHeroByUUID(heroUUID);
		}
    	return hero;
	}
}
