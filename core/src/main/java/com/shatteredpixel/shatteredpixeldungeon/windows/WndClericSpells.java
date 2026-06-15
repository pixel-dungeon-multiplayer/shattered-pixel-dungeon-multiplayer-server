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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.nikita22007.multiplayer.utils.text.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.ClericSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.GuidingLight;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.ArrayList;

public class WndClericSpells extends Window {

	protected static final int WIDTH = 120;

	public static int BTN_SIZE = 20;
	public ArrayList<SpellButton> spellBtns = new ArrayList<>();
	private final HolyTome tome;
	private final boolean infoMode;


	public WndClericSpells(HolyTome tome, Hero cleric, boolean info) {
		super(cleric);
		this.tome = tome;
		this.infoMode = info;
		for (int i = 1; i <= Talent.MAX_TALENT_TIERS; i++) {

			ArrayList<ClericSpell> spells = ClericSpell.getSpellList(cleric, i);


			for (ClericSpell spell : spells) {
				SpellButton spellBtn = new SpellButton(spell, tome, info, i, ClericSpell.getSpellID(spell));
				spellBtns.add(spellBtn);
			}
		}
	}

	public HolyTome tome() {
		return tome;
	}

	public boolean infoMode() {
		return infoMode;
	}

	@Override
	public void onSelect(int button, @Nullable JSONObject args) {

		for (SpellButton spellButton: spellBtns) {
			if (spellButton.spellID == button) {
				if (args != null) {
					spellButton.info = args.optBoolean("info", false);
				} else {
					spellButton.info = false;
				}
				spellButton.onClick();
				hide();
				break;
			}
		}
	}

	public class SpellButton extends IconButton {

		public ClericSpell spell;
		public HolyTome tome;
		public boolean info;
		public int tier;
		public int spellID;
		public SpellButton(ClericSpell spell, HolyTome tome, boolean info, int tier, int spellID){
			super(new HeroIcon(spell));
			this.spellID = spellID;
			this.tier = tier;
			this.spell = spell;
			this.tome = tome;
			this.info = info;
			if (!tome.canCast(getOwnerHero(), spell)){
				icon.alpha( 0.3f );
			} else if (spell == GuidingLight.INSTANCE && spell.chargeUse(getOwnerHero()) == 0){
				icon.brightness(3);
			}

		}


		@Override
		protected void layout() {
		}


		@Override
        public void onClick() {
			if (info){
				GameScene.show(new WndTitledMessage(new HeroIcon(spell), Messages.titleCase(spell.name()), spell.desc(getOwnerHero())));
			} else {
				hide();

				if(!tome.canCast(getOwnerHero(), spell)){
					GLog.w(Messages.get(HolyTome.class, "no_spell"));
				} else {
					spell.onCast(tome, getOwnerHero());

					if (spell.targetingFlags() != -1 && Dungeon.quickslot.contains(tome)){
						tome.targetingSpell = spell;
						//todo: check this
						//QuickSlotButton.useTargeting(Dungeon.quickslot.getSlot(tome));
					}
				}

			}
		}

		@Override
		protected boolean onLongClick() {
			hide();
			tome.setQuickSpell(spell, getOwnerHero());
			return true;
		}


		@Override
		protected LocalizedString hoverText() {
			return LocalizedString.concat("_", Messages.titleCase(spell.name()), "_\n", spell.shortDesc(getOwnerHero()));
		}
	}

}
