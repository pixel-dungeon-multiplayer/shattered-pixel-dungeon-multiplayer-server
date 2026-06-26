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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.AntiMagic;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.StatueSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class ArmoredStatue extends Statue {

	{
		spriteClass = StatueSprite.class;
	}

	protected Armor armor;

	public ArmoredStatue(){
		super();

		//double HP
		setHP(setHT(30 + Dungeon.depth * 10));
	}

	@Override
	public void createWeapon(boolean useDecks) {
		super.createWeapon(useDecks);

		armor = Generator.randomArmor();
		armor.cursed = false;
		armor.inscribe(Armor.Glyph.random());
	}

	private static final String ARMOR	= "armor";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( ARMOR, armor );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		armor = (Armor)bundle.get( ARMOR );
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange( armor.DRMin(this), armor.DRMax(this));
	}

	//used in some glyph calculations
	public Armor armor(){
		return armor;
	}

	@Override
	public int defenseProc(Char enemy, int damage) {
		damage = armor.proc(enemy, this, damage);
		return super.defenseProc(enemy, damage);
	}

	@Override
	public void damage(int dmg, @NotNull DamageCause source) {
		Object src = source.getCause();
		//TODO improve this when I have proper damage source logic
		if (armor != null && armor.hasGlyph(AntiMagic.class, this)
				&& AntiMagic.RESISTS.contains(src.getClass())){
			dmg -= AntiMagic.drRoll(this, armor.buffedLvl());
			dmg = Math.max(dmg, 0);
        }
		super.damage( dmg, source );

    }
	public int glyphLevel(Class<? extends Armor.Glyph> cls) {
		if (armor != null && armor.hasGlyph(cls, this)){
			return Math.max(super.glyphLevel(cls), armor.buffedLvl());
		} else {
			return super.glyphLevel(cls);
		}

		//for the rose status indicator
	}

	@Override
	public CharSprite sprite() {
		CharSprite sprite = super.sprite();
		if (armor != null) {
			((StatueSprite) sprite).setArmor(armor.tier);
		} else {
			((StatueSprite) sprite).setArmor(3);
		}
		return sprite;
	}

	@Override
	public int defenseSkill(Char enemy) {
		return Math.round(armor.evasionFactor(this, super.defenseSkill(enemy)));
	}

	@Override
	public void die(@NotNull DamageCause cause ) {
		armor.identify(null);
		Dungeon.level.drop( armor, pos ).sprite.drop();
		super.die( cause );
	}

	@Override
	public LocalizedString description() {
		LocalizedString desc = Messages.get(this, "desc");
		if (weapon != null && armor != null){
			desc = LocalizedString.concat(desc, LocalizedString.concat("\n\n", Messages.get(this, "desc_arm_wep", weapon.name(), armor.name())));
		}
		return desc;
	}

}
