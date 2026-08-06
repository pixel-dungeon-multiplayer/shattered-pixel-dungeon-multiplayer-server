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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.ClericSpell;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.NinePatch;
import com.watabou.utils.DeviceCompat;
import org.json.JSONObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class WndClericSpells extends Window {

	protected static final int WIDTH    = 120;

	public static int BTN_SIZE = 20;

	private HolyTome tome;
	private boolean info;
	public final IconTitle title;
	public ArrayList<SpellButton> spellButtons = new ArrayList<>();

	public WndClericSpells(HolyTome tome, Hero cleric, boolean info){
		super(cleric);
		this.tome = tome;
		this.info = info;
		if (!info){
			title = new IconTitle(new ItemSprite(tome), Messages.titleCase(Messages.get(this, "cast_title")));
		} else {
			title = new IconTitle(Icons.INFO, Messages.titleCase(Messages.get(this, "info_title")));
		}

		title.setRect(0, 0, WIDTH, 0);
		add(title);

		IconButton btnInfo = new IconButton(info ? new ItemSprite(tome) : Icons.INFO.get()){
			@Override
			protected void onClick() {
				GameScene.show(new WndClericSpells(tome, cleric, !info));
				hide();
			}
		};
		btnInfo.setRect(WIDTH-16, 0, 16, 16);
		add(btnInfo);

		RenderedTextBlock msg = PixelScene.renderTextBlock(desc(), 6);
		msg.maxWidth(WIDTH);
		msg.setPos(0, title.bottom()+4);
		add(msg);

		int top = (int)msg.bottom()+4;

		for (int i = 1; i <= Talent.MAX_TALENT_TIERS; i++) {

			ArrayList<ClericSpell> spells = ClericSpell.getSpellList(cleric, i);

			if (!spells.isEmpty() && i != 1){
				top += BTN_SIZE + 2;
				ColorBlock sep = new ColorBlock(WIDTH, 1, 0xFF000000);
				sep.y = top;
				add(sep);
				top += 3;
			}

			ArrayList<IconButton> spellBtns = new ArrayList<>();

			for (ClericSpell spell : spells) {
				SpellButton spellBtn = new SpellButton(spell, tome, info);
				add(spellBtn);
				spellBtns.add(spellBtn);
				spellButtons.add(spellBtn);
			}

			int left = 2 + (WIDTH - spellBtns.size() * (BTN_SIZE + 4)) / 2;
			for (IconButton btn : spellBtns) {
				btn.setRect(left, top, BTN_SIZE, BTN_SIZE);
				left += btn.width() + 4;
			}

		}

		resize(WIDTH, top + BTN_SIZE);

		//if we are on mobile, offset the window down to just above the toolbar
		if (SPDSettings.interfaceSize() != 2){
			offset(0, (int) (GameScene.uiCamera.height/2 - 30 - height/2));
		}

	}

	public HolyTome tome() {
		return tome;
	}

	public boolean infoMode() {
		return info;
	}

	@Override
	public void onSelect(int button, @Nullable JSONObject args) {
		if (button == spellButtons.size()) {
			GameScene.show(new WndClericSpells(tome, getOwnerHero(), !info));
			hide();
		} else if (button >= 0 && button < spellButtons.size()) {
			ClericSpell spell = spellButtons.get(button).spell;
			boolean isAlternateClick = args != null && (args.optBoolean("is_long_click", false)
					|| args.optBoolean("is_right_click", false));
			if (info) {
				GameScene.show(new WndTitledMessage(getOwnerHero(), new HeroIcon(spell), Messages.titleCase(spell.name()), spell.desc(getOwnerHero())));
			} else {
				hide();
				if (isAlternateClick) {
					tome.setQuickSpell(spell, getOwnerHero());
				} else if (!tome.canCast(getOwnerHero(), spell)) {
					GLog.w(Messages.get(HolyTome.class, "no_spell"));
				} else {
					spell.onCast(tome, getOwnerHero());
					if (spell.targetingFlags() != -1 && Dungeon.quickslot.contains(tome)) {
						tome.targetingSpell = spell;
					}
				}
			}
		} else {
			super.onSelect(button, args);
		}
	}

	public LocalizedString desc() {
		if (info) {
			return Messages.get(WndClericSpells.class, "info_desc");
		} else if (DeviceCompat.isDesktop()) {
			return Messages.get(WndClericSpells.class, "cast_desc_desktop");
		} else {
			return Messages.get(WndClericSpells.class, "cast_desc_mobile");
		}
	}

	public class SpellButton extends IconButton {

		public ClericSpell spell;
		public HolyTome tome;
		public boolean info;

		NinePatch bg;

		public SpellButton(ClericSpell spell, HolyTome tome, boolean info){
			super(new HeroIcon(spell));

			this.spell = spell;
			this.tome = tome;
			this.info = info;
			if (!tome.canCast(getOwnerHero(), spell)){
				image.alpha( 0.3f );
			}

			bg = Chrome.get(Chrome.Type.TOAST);
			addToBack(bg);
		}

		@Override
		protected void onPointerUp() {
			super.onPointerUp();
			if (!tome.canCast(getOwnerHero(), spell)){
				image.alpha( 0.3f );
			}
		}

		@Override
		protected void layout() {
			super.layout();

			if (bg != null) {
				bg.size(width, height);
				bg.x = x;
				bg.y = y;
			}
		}

		@Override
		protected void onClick() {
			if (info){
				GameScene.show(new WndTitledMessage(getOwnerHero(), new HeroIcon(spell), Messages.titleCase(spell.name()), spell.desc(getOwnerHero())));
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
		public LocalizedString hoverText() {
			return LocalizedString.concat("_", Messages.titleCase(spell.name()), "_\n" + spell.shortDesc(getOwnerHero()));
		}
	}

}
