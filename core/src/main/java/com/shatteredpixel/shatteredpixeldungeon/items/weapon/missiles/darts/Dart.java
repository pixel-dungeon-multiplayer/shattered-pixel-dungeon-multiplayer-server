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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts;

import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.VelvetPouch;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Crossbow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import io.github.pixeldungeonmultiplayer.shattered.server.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Dart extends MissileWeapon {

	{
		levelKnown = true;

		image = ItemSpriteSheet.DART;
		hitSound = Assets.Sounds.HIT_ARROW;
		hitSoundPitch = 1.3f;
		
		tier = 1;
		
		//infinite, even with penalties
		baseUses = 1000;

		//all darts share a set ID
		setID = 0L;
	}
	
	protected static final String AC_TIP = "TIP";
	
	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_TIP );
		return actions;
	}
	
	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (action.equals(AC_TIP)){
			GameScene.selectItem(itemSelector, hero);
		}
	}
	
	@Override
	public int min(int lvl, Char owner) {
		if (bow != null){
			if (!(this instanceof TippedDart) && owner.buff(Crossbow.ChargedShot.class) != null){
				return bow.dartMin()            //crossbow dart damage
						+ 4 + bow.buffedLvl()   //ability increases base dmg by 50%, scaling by 50%
						+ lvl;                  //another +1 per level (ring of sharpshooting)
			} else {
				return bow.dartMin()            //crossbow dart damage
						+ lvl;                  //another +1 per level (ring of sharpshooting)
			}
		} else {
			return  1 +     //1 base, down from 2
					lvl;    //scaling unchanged
		}
	}

	@Override
	public int max(int lvl, Char owner) {
		if (bow != null){
			if (!(this instanceof TippedDart) && owner.buff(Crossbow.ChargedShot.class) != null){
				return bow.dartMax()            //crossbow dart damage
						+ 4 + bow.buffedLvl()   //ability increases base dmg by 50%, scaling by 50%
						+ 2*lvl;                //another +2 per level (ring of sharpshooting)
			} else {
				return bow.dartMax()            //crossbow dart damage
						+ 2*lvl;                //another +2 per level (ring of sharpshooting)
			}
		} else {
			return  2 +     //2 base, down from 5
					2*lvl;  //scaling unchanged
		}
	}
	
	protected static Crossbow bow;
	
	private void updateCrossbow(Hero hero){
		if (hero.belongings.weapon() instanceof Crossbow){
			bow = (Crossbow) hero.belongings.weapon();
		} else if (hero.belongings.secondWep() instanceof Crossbow) {
			//player can instant swap anyway, so this is just QoL
			bow = (Crossbow) hero.belongings.secondWep();
		} else {
			bow = null;
		}
	}

	public boolean crossbowHasEnchant( Char owner ){
		return bow != null && bow.enchantment != null && owner.buff(MagicImmune.class) == null;
	}
	
	@Override
	public boolean hasEnchant(Class<? extends Enchantment> type, Char owner) {
		if (bow != null && bow.hasEnchant(type, owner)){
			return true;
		} else {
			return super.hasEnchant(type, owner);
		}
	}

	@Override
	public float accuracyFactor(Char owner, Char target) {
		//don't update xbow here, as dart is the active weapon atm
		if (bow != null && owner.buff(Crossbow.ChargedShot.class) != null){
			return Char.INFINITE_ACCURACY;
		} else {
			return super.accuracyFactor(owner, target);
		}
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		if (bow != null && !processingChargedShot){
			damage = bow.proc(attacker, defender, damage);
		}

		int dmg = super.proc(attacker, defender, damage);
		if (!processingChargedShot) {
			processChargedShot(defender, damage, (Hero) attacker);
		}
		return dmg;
	}

	@Override
	public int throwPos(@NotNull Hero user, int dst) {
		updateCrossbow(user);
		return super.throwPos(user, dst);
	}

	@Override
	protected void onThrow(int cell, Hero hero) {
		updateCrossbow(hero);
		//we have to set this here, as on-hit effects can move the target we aim at
		chargedShotPos = cell;
		super.onThrow(cell, hero);
	}

	protected boolean processingChargedShot = false;
	private int chargedShotPos;
	protected void processChargedShot( Char target, int dmg, Hero hero ){
		//don't update xbow here, as dart may be the active weapon atm
		processingChargedShot = true;
		if (chargedShotPos != -1 && bow != null && hero.buff(Crossbow.ChargedShot.class) != null) {
			PathFinder.buildDistanceMap(chargedShotPos, Dungeon.level.passable, 3);
			//necessary to clone as some on-hit effects use Pathfinder
			int[] distance = PathFinder.distance.clone();
			for (Char ch : Actor.chars()){
				if (ch == target){
					Actor.add(new Actor() {
						{ actPriority = VFX_PRIO; }
						@Override
						protected boolean act() {
							if (!ch.isAlive()){
								bow.onAbilityKill(hero, ch);
							}
							Actor.remove(this);
							return true;
						}
					});
				} else if (distance[ch.pos] != Integer.MAX_VALUE){
					proc(hero, ch, dmg);
				}
			}
		}
		chargedShotPos = -1;
		processingChargedShot = false;
	}

	@Override
	protected void decrementDurability(Hero hero) {
		super.decrementDurability(hero);
		if (hero.buff(Crossbow.ChargedShot.class) != null) {
			hero.buff(Crossbow.ChargedShot.class).detach();
		}
	}

	@Override
	public void throwSound(Hero hero) {
		updateCrossbow(hero);
		if (bow != null) {
			Sample.INSTANCE.play(Assets.Sounds.ATK_CROSSBOW, 1, Random.Float(0.87f, 1.15f));
		} else {
			super.throwSound();
		}
	}
	
	@Override
	public LocalizedString info(Hero hero) {
		updateCrossbow(hero);
		if (bow != null && !bow.isIdentified()){
			Crossbow realBow = bow;
			//create a temporary bow for IDing purposes
			bow = new Crossbow();
			LocalizedString info = super.info();
			bow = realBow;
			return info;
		} else {
			return super.info();
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

	@Override
	public int defaultQuantity() {
		return 2;
	}

	@Contract(pure = true)
    @Override
	public int value() {
		return Math.round(super.value()/2f); //half normal value
	}
	
	private final WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {

		@Override
		public LocalizedString textPrompt() {
			return Messages.get(Dart.class, "prompt");
		}

		@Override
		public Class<?extends Bag> preferredBag(){
			return VelvetPouch.class;
		}

		@Override
		public boolean itemSelectable(Item item) {
			return item instanceof Plant.Seed;
		}

		@Override
		public void onSelect(final Item item) {
			
			if (item == null) return;
			
			final int maxToTip = Math.min(curItem.quantity(), item.quantity()*2);
			final int maxSeedsToUse = (maxToTip+1)/2;
			
			final int singleSeedDarts;
			
			final LocalizedString[] options;
			
			if (curItem.quantity() == 1){
				singleSeedDarts = 1;
				options = new LocalizedString[]{
						Messages.get(Dart.class, "tip_one"),
						Messages.get(Dart.class, "tip_cancel")};
			} else {
				singleSeedDarts = 2;
				if (maxToTip <= 2){
					options = new LocalizedString[]{
							Messages.get(Dart.class, "tip_two"),
							Messages.get(Dart.class, "tip_cancel")};
				} else {
					options = new LocalizedString[]{
							Messages.get(Dart.class, "tip_all", maxToTip, maxSeedsToUse),
							Messages.get(Dart.class, "tip_two"),
							Messages.get(Dart.class, "tip_cancel")};
				}
			}
			
			TippedDart tipResult = TippedDart.getTipped((Plant.Seed) item, 1);
			
			GameScene.show(new WndOptions(getOwner(), new ItemSprite(item),
					Messages.titleCase(item.name()),
					LocalizedString.concat(Messages.get(Dart.class, "tip_desc", tipResult.name()), "\n\n", tipResult.desc()),
					options){
				
				@Override
				protected void onSelect(int index) {
					super.onSelect(index);
					
					if (index == 0 && options.length == 3){
						if (item.quantity() <= maxSeedsToUse){
							item.detachAll( curUser.belongings.backpack );
						} else {
							item.quantity(item.quantity() - maxSeedsToUse);
						}
						
						if (maxToTip < curItem.quantity()){
							curItem.quantity(curItem.quantity() - maxToTip);
						} else {
							curItem.detachAll(curUser.belongings.backpack);
						}
						
						TippedDart newDart = TippedDart.getTipped((Plant.Seed) item, maxToTip);
						if (!newDart.collect(getOwnerHero())) Dungeon.level.drop(newDart, curUser.pos).sprite.drop();
						
						curUser.spend( 1f );
						curUser.busy();
						curUser.getSprite().operate(curUser.pos);
						
					} else if ((index == 1 && options.length == 3) || (index == 0 && options.length == 2)){
						item.detach( curUser.belongings.backpack );
						
						if (curItem.quantity() <= singleSeedDarts){
							curItem.detachAll( curUser.belongings.backpack );
						} else {
							curItem.quantity(curItem.quantity() - singleSeedDarts);
						}
						
						TippedDart newDart = TippedDart.getTipped((Plant.Seed) item, singleSeedDarts);
						if (!newDart.collect(getOwnerHero())) Dungeon.level.drop(newDart, curUser.pos).sprite.drop();
						
						curUser.spend( 1f );
						curUser.busy();
						curUser.getSprite().operate(curUser.pos);
					}
				}
			});
			
		}
		
	};
}
