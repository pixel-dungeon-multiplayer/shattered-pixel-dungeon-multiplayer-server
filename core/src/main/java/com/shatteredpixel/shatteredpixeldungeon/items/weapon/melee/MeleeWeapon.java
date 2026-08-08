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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArtifactRecharge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.GreaterHaste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MonkEnergy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Recharging;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Regeneration;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.HolyWeapon;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfForce;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.Visual;
import io.github.pixeldungeonmultiplayer.shattered.server.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class MeleeWeapon extends Weapon {

    public static String AC_ABILITY = "ABILITY";

    @Override
    public void activate(Char ch) {
        super.activate(ch);
        if (ch instanceof Hero && ((Hero) ch).heroClass == HeroClass.DUELIST) {
            Buff.affect(ch, Charger.class);
        }
    }

    @Override
    public String defaultAction(Hero hero) {
        if (hero != null && (hero.heroClass == HeroClass.DUELIST
                || hero.hasTalent(Talent.SWIFT_EQUIP))) {
            return AC_ABILITY;
        } else {
            return super.defaultAction(hero);
        }
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        if (isEquipped(hero) && hero.heroClass == HeroClass.DUELIST) {
            actions.add(AC_ABILITY);
        }
        return actions;
    }

    @Override
    public LocalizedString actionName(String action, Hero hero) {
        if (action.equals(AC_ABILITY)) {
            return Messages.upperCase(Messages.get(this, "ability_name"));
        } else {
            return super.actionName(action, hero);
        }
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);

        if (action.equals(AC_ABILITY)) {
            usesTargeting = false;
            if (!isEquipped(hero)) {
                if (hero.hasTalent(Talent.SWIFT_EQUIP)) {
                    if (hero.buff(Talent.SwiftEquipCooldown.class) == null
                            || hero.buff(Talent.SwiftEquipCooldown.class).hasSecondUse()) {
                        execute(hero, AC_EQUIP);
                    } else if (hero.heroClass == HeroClass.DUELIST) {
                        GLog.w(Messages.get(this, "ability_need_equip"));
                    }
                } else if (hero.heroClass == HeroClass.DUELIST) {
                    GLog.w(Messages.get(this, "ability_need_equip"));
                }
            } else if (hero.heroClass != HeroClass.DUELIST) {
                //do nothing
            } else if (STRReq() > hero.STR()) {
                GLog.w(Messages.get(this, "ability_low_str"));
            } else if ((Buff.affect(hero, Charger.class).charges + Buff.affect(hero, Charger.class).partialCharge) < abilityChargeUse(hero, null)) {
                GLog.w(Messages.get(this, "ability_no_charge"));
            } else {

                if (targetingPrompt() == null) {
                    duelistAbility(hero, hero.pos);
                    updateQuickslot();
                } else {
                    usesTargeting = useTargeting();
                    GameScene.selectCell(hero, new CellSelector.Listener() {
                        @Override
                        public void onSelect(Integer cell) {
                            if (cell != null) {
                                duelistAbility(hero, cell);
                                updateQuickslot();
                            }
                        }

                        @Override
                        public LocalizedString prompt() {
                            return targetingPrompt();
                        }
                    });
                }
            }
        }
    }

    //leave null for no targeting
    public LocalizedString targetingPrompt() {
        return null;
    }

    public boolean useTargeting() {
        return targetingPrompt() != null;
    }

    @Override
    public int targetingPos(@NotNull Hero user, int dst) {
        return dst; //weapon abilities do not use projectile logic, no autoaim
    }

    protected void duelistAbility(Hero hero, Integer target) {
        //do nothing by default
    }

    protected void beforeAbilityUsed(Hero hero, Char target) {
        hero.belongings.abilityWeapon = this;
        Charger charger = Buff.affect(hero, Charger.class);

        charger.partialCharge -= abilityChargeUse(hero, target);
        while (charger.partialCharge < 0 && charger.charges > 0) {
            charger.charges--;
            charger.partialCharge++;
        }

        if (hero.heroClass == HeroClass.DUELIST
                && hero.hasTalent(Talent.AGGRESSIVE_BARRIER)
                && (hero.getHP() / (float) hero.getHT()) <= 0.5f) {
            int shieldAmt = 1 + 2 * hero.pointsInTalent(Talent.AGGRESSIVE_BARRIER);
            Buff.affect(hero, Barrier.class).setShield(shieldAmt);
            hero.getSprite().showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(shieldAmt), FloatingText.SHIELDING);
        }
        updateQuickslot();
    }

    protected void afterAbilityUsed(Hero hero) {
        hero.belongings.abilityWeapon = null;
        if (hero.hasTalent(Talent.PRECISE_ASSAULT)) {
            Buff.prolong(hero, Talent.PreciseAssaultTracker.class, hero.cooldown() + 4f);
        }
        if (hero.hasTalent(Talent.VARIED_CHARGE)) {
            Talent.VariedChargeTracker tracker = hero.buff(Talent.VariedChargeTracker.class);
            if (tracker == null || tracker.weapon == getClass() || tracker.weapon == null) {
                Buff.affect(hero, Talent.VariedChargeTracker.class).weapon = getClass();
            } else {
                tracker.detach();
                Charger charger = Buff.affect(hero, Charger.class);
                charger.gainCharge(hero.pointsInTalent(Talent.VARIED_CHARGE) / 6f, hero);
                ScrollOfRecharging.charge(hero);
            }
        }
        if (hero.hasTalent(Talent.COMBINED_LETHALITY)) {
            Talent.CombinedLethalityAbilityTracker tracker = hero.buff(Talent.CombinedLethalityAbilityTracker.class);
            if (tracker == null || tracker.weapon == this || tracker.weapon == null) {
                Buff.affect(hero, Talent.CombinedLethalityAbilityTracker.class, hero.cooldown()).weapon = this;
            } else {
                //we triggered the talent, so remove the tracker
                tracker.detach();
            }
        }
        if (hero.hasTalent(Talent.COMBINED_ENERGY)) {
            Talent.CombinedEnergyAbilityTracker tracker = hero.buff(Talent.CombinedEnergyAbilityTracker.class);
            if (tracker == null || !tracker.monkAbilused) {
                Buff.prolong(hero, Talent.CombinedEnergyAbilityTracker.class, 5f).wepAbilUsed = true;
            } else {
                tracker.wepAbilUsed = true;
                Buff.affect(hero, MonkEnergy.class).processCombinedEnergy(tracker);
            }
        }
        if (hero.buff(Talent.CounterAbilityTacker.class) != null) {
            Charger charger = Buff.affect(hero, Charger.class);
            charger.gainCharge(hero.pointsInTalent(Talent.COUNTER_ABILITY) * 0.375f, hero);
            hero.buff(Talent.CounterAbilityTacker.class).detach();
        }
    }

    public static void onAbilityKill(Hero hero, Char killed) {
        if (killed.alignment == Char.Alignment.ENEMY && hero.hasTalent(Talent.LETHAL_HASTE)) {
            //effectively 3/5 turns of greater haste
            Buff.affect(hero, GreaterHaste.class).set(2 + 2 * hero.pointsInTalent(Talent.LETHAL_HASTE));
        }
    }

    protected int baseChargeUse(Hero hero, Char target) {
        return 1; //abilities use 1 charge by default
    }

    public final float abilityChargeUse(Hero hero, Char target) {
        return baseChargeUse(hero, target);
    }

    public int tier;

    @Override
    public int min(int lvl) {
        return tier +  //base
                lvl;    //level scaling
    }

    @Override
    public int max(int lvl) {
        return 5 * (tier + 1) +    //base
                lvl * (tier + 1);   //level scaling
    }

    public int STRReq(int lvl) {
        int req = STRReq(tier, lvl);
        if (masteryPotionBonus) {
            req -= 2;
        }
        return req;
    }

    private static boolean evaluatingTwinUpgrades = false;

    @Override
    public int buffedLvl(Char owner) {
        if (owner instanceof Hero) {
            Hero hero = (Hero) owner;
            if (!evaluatingTwinUpgrades && hero != null && isEquipped(hero) && hero.hasTalent(Talent.TWIN_UPGRADES)) {
                KindOfWeapon other = null;
                if (hero.belongings.weapon() != this) other = hero.belongings.weapon();
                if (hero.belongings.secondWep() != this) other = hero.belongings.secondWep();

                if (other instanceof MeleeWeapon) {
                    evaluatingTwinUpgrades = true;
                    int otherLevel = other.buffedLvl();
                    evaluatingTwinUpgrades = false;

                    //weaker weapon needs to be 2/1/0 tiers lower, based on talent level
                    if ((tier + (3 - hero.pointsInTalent(Talent.TWIN_UPGRADES))) <= ((MeleeWeapon) other).tier
                            && otherLevel > super.buffedLvl()) {
                        return otherLevel;
                    }

                }
            }
        }
        return super.buffedLvl();
    }

    @Override
    public int damageRoll(Char owner) {
        int damage = augment.damageFactor(super.damageRoll(owner));

        if (owner instanceof Hero) {
            int exStr = ((Hero) owner).STR() - STRReq();
            if (exStr > 0) {
                damage += Hero.heroDamageIntRange(0, exStr);
            }
        }
        return damage;
    }

    @Override
    public LocalizedString info(Hero hero) {

        LocalizedString info = super.info();

        if (levelKnown) {
            info = LocalizedString.concat(info, LocalizedString.concat("\n\n", Messages.get(MeleeWeapon.class, "stats_known", tier, augment.damageFactor(min(hero)), augment.damageFactor(max(hero)), STRReq())));
            if (hero != null) {
                if (STRReq() > hero.STR()) {
                    info = LocalizedString.concat(info, LocalizedString.concat(" ", Messages.get(Weapon.class, "too_heavy")));
                } else if (hero.STR() > STRReq()) {
                    info = LocalizedString.concat(info, LocalizedString.concat(" ", Messages.get(Weapon.class, "excess_str", hero.STR() - STRReq())));
                }

            } else {
                info = LocalizedString.concat(info, LocalizedString.concat("\n\n", Messages.get(MeleeWeapon.class, "stats_unknown", tier, min(0, hero), max(0, hero), STRReq(0))));
                if (hero != null && STRReq(0) > hero.STR()) {
                    info = LocalizedString.concat(info, LocalizedString.concat(" ", Messages.get(MeleeWeapon.class, "probably_too_heavy")));
                }
            }

            LocalizedString statsInfo = statsInfo();
            if (!statsInfo.equals(LocalizedString.EMPTY)) {
                info = LocalizedString.concat(info,  "\n\n", statsInfo);
            }

            switch (augment) {
                case SPEED:
                    info = LocalizedString.concat(info, LocalizedString.concat(" ", Messages.get(Weapon.class, "faster")));
                    break;
                case DAMAGE:
                    info = LocalizedString.concat(info, LocalizedString.concat(" ", Messages.get(Weapon.class, "stronger")));
                    break;
                case NONE:
            }

            if (isEquipped(hero) && !hasCurseEnchant() && hero.buff(HolyWeapon.HolyWepBuff.class) != null
                    && (hero.subClass != HeroSubClass.PALADIN || enchantment == null)) {
                info = LocalizedString.concat(info, LocalizedString.concat("\n\n", Messages.capitalize(Messages.get(Weapon.class, "enchanted", Messages.get(HolyWeapon.class, "ench_name", Messages.get(Enchantment.class, "enchant"))))));
                info = LocalizedString.concat(info, LocalizedString.concat(" ", Messages.get(HolyWeapon.class, "ench_desc")));
            } else if (enchantment != null && (cursedKnown || !enchantment.curse())) {
                info = LocalizedString.concat(info, LocalizedString.concat("\n\n", Messages.capitalize(Messages.get(Weapon.class, "enchanted", enchantment.name()))));
                if (enchantHardened) info = LocalizedString.concat(info, LocalizedString.concat(" ", Messages.get(Weapon.class, "enchant_hardened")));
                info = LocalizedString.concat(info, LocalizedString.concat(" ", enchantment.desc()));
            } else if (enchantHardened) {
                info = LocalizedString.concat(info, LocalizedString.concat("\n\n", Messages.get(Weapon.class, "hardened_no_enchant")));
            }

            if (cursed && isEquipped(hero)) {
                info = LocalizedString.concat(info, LocalizedString.concat("\n\n", Messages.get(Weapon.class, "cursed_worn")));
            } else if (cursedKnown && cursed) {
                info = LocalizedString.concat(info, LocalizedString.concat("\n\n", Messages.get(Weapon.class, "cursed")));
            } else if (!isIdentified() && cursedKnown) {
                if (enchantment != null && enchantment.curse()) {
                    info = LocalizedString.concat(info, LocalizedString.concat("\n\n", Messages.get(Weapon.class, "weak_cursed")));
                } else {
                    info = LocalizedString.concat(info, LocalizedString.concat("\n\n", Messages.get(Weapon.class, "not_cursed")));
                }
            }

            //the mage's staff has no ability as it can only be gained by the mage
            if (hero.heroClass == HeroClass.DUELIST && !(this instanceof MagesStaff)) {
                info = LocalizedString.concat(info, "\n\n",  abilityInfo(hero));
            }

        }
        return info;
    }

    public LocalizedString statsInfo() {
        return Messages.get(this, "stats_desc");
    }

    public LocalizedString abilityInfo(Hero hero) {
        return Messages.get(this, "ability_desc");
    }

    public String upgradeAbilityStat(int level) {
        return null;
    }

    @Override
    public @Nullable LocalizedString status(Hero hero) {
        if (isEquipped(hero)
                && hero.buff(Charger.class) != null) {
            Charger buff = hero.buff(Charger.class);
            return LocalizedString.concat(buff.charges, "/", buff.chargeCap(hero));
        } else {
            return super.status();
        }
    }

    @Contract(pure = true)
    @Override
    public int value() {
        int price = 20 * tier;
        if (hasGoodEnchant()) {
            price *= 1.5;
        }
        if (cursedKnown && (cursed || hasCurseEnchant())) {
            price /= 2;
        }
        if (levelKnown && level() > 0) {
            price *= (level() + 1);
        }
        if (price < 1) {
            price = 1;
        }
        return price;
    }

    public static class Charger extends Buff implements ActionIndicator.Action {

		{
			//so that duelist keeps weapon charge on ankh revive
			revivePersists = true;
		}

        public int charges = 2;
        public float partialCharge;

        @Override
        public boolean act() {
            if (target instanceof Hero) {
                Hero hero = (Hero) target;
                if (charges < chargeCap(hero)) {
                    if (Regeneration.regenOn(hero)) {
                        //60 to 45 turns per charge
                        float chargeToGain = 1 / (60f - 1.5f * (chargeCap(hero) - charges));

                        //40 to 30 turns per charge for champion
                        if (hero.subClass == HeroSubClass.CHAMPION) {
                            chargeToGain *= 1.5f;
                        }

                        //50% slower charge gain with brawler's stance enabled, even if buff is inactive
                        if (hero.buff(RingOfForce.BrawlersStance.class) != null) {
                            chargeToGain *= 0.50f;
                        }

                        partialCharge += chargeToGain;
                    }

                    int points = ((Hero) target).pointsInTalent(Talent.WEAPON_RECHARGING);
                    if (points > 0 && target.buff(Recharging.class) != null || target.buff(ArtifactRecharge.class) != null) {
                        //1 every 15 turns at +1, 10 turns at +2
                        partialCharge += 1 / (20f - 5f * points);
                    }

                    if (partialCharge >= 1) {
                        charges++;
                        partialCharge--;
                        Item.updateQuickslot(hero, null);
                    }
                } else {
                    partialCharge = 0;
                }

                if (hero.actionIndicator.action != this && hero.subClass == HeroSubClass.CHAMPION) {
                    hero.actionIndicator.setAction(this);
                }

                spend(TICK);
                //				return true;
            }
            return true;

        }

        @Override
        public void fx(boolean on) {
            if (target instanceof Hero) {
                if (on && ((Hero) target).subClass == HeroSubClass.CHAMPION) {
                    ((Hero) target).actionIndicator.setAction(this);
                }
            }
        }

        @Override
        public void detach() {
            ((Hero) target).actionIndicator.clearAction(this);
            super.detach();
        }

        public int chargeCap(Hero hero) {
            //caps at level 19 with 8 or 10 charges
            if (hero.subClass == HeroSubClass.CHAMPION) {
                return Math.min(10, 4 + (hero.lvl - 1) / 3);
            } else {
                return Math.min(8, 2 + (hero.lvl - 1) / 3);
            }
        }

        public void gainCharge(float charge, Hero hero) {
            if (charges < chargeCap(hero)) {
                partialCharge += charge;
                while (partialCharge >= 1f) {
                    charges++;
                    partialCharge--;
                }
                if (charges >= chargeCap(hero)) {
                    partialCharge = 0;
                    charges = chargeCap(hero);
                }
                Item.updateQuickslot(target, null);
            }
        }

        public static final String CHARGES = "charges";
        private static final String PARTIALCHARGE = "partialCharge";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(CHARGES, charges);
            bundle.put(PARTIALCHARGE, partialCharge);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            charges = bundle.getInt(CHARGES);
            partialCharge = bundle.getFloat(PARTIALCHARGE);
        }

        @Override
        public LocalizedString actionName() {
            return Messages.get(MeleeWeapon.class, "swap");
        }

        @Override
        public int actionIcon() {
            return HeroIcon.WEAPON_SWAP;
        }

        @Override
        public Visual primaryVisual() {
            Image ico;
            if (((Hero) target).belongings.getRealWeapon() == null) {
                ico = new HeroIcon(this);
            } else {
                ico = new ItemSprite(((Hero) target).belongings.getRealWeapon());
            }
            ico.width += 4; //shift slightly to the left to separate from smaller icon
            return ico;
        }

        @Override
        public Visual secondaryVisual() {
            Image ico;
            if (((Hero) target).belongings.secondWep == null) {
                ico = new HeroIcon(this);
            } else {
                ico = new ItemSprite(((Hero) target).belongings.secondWep);
            }
            ico.scale.set(PixelScene.align(0.51f));
            ico.brightness(0.6f);
            return ico;
        }

        @Override
        public int indicatorColor() {
            return 0x5500BB;
        }


        @Override
        public void doAction(Hero hero) {
            if (hero.subClass != HeroSubClass.CHAMPION) {
                return;
            }

            if (hero.belongings.secondWep == null && hero.belongings.backpack.items().size() >= hero.belongings.backpack.capacity()) {
                GLog.w(Messages.get(MeleeWeapon.class, "swap_full"));
                return;
            }

            KindOfWeapon temp = hero.belongings.getRealWeapon();
            hero.belongings.setWeapon(hero.belongings.secondWep);
            hero.belongings.secondWep = temp;

            hero.getSprite().operate(hero.pos);
            Sample.INSTANCE.play(Assets.Sounds.UNLOCK);

            hero.actionIndicator.setAction(this);
            Item.updateQuickslot(hero, null);
            hero.attackIndicator.updateState();
        }
    }
}
