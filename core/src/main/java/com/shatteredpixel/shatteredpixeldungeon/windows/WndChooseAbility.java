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

import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.items.KingsCrown;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Game;
import com.watabou.utils.Random;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class WndChooseAbility extends Window {

	private static final int WIDTH		= 130;
	private static final float GAP		= 2;
	private final KingsCrown crown;
	private final Armor armor;
	public IconTitle titlebar;
	public RenderedTextBlock message;
	public IconButton randomButton;
	public RedButton[] abilityButtons;
	public IconButton[] abilityInfoButtons;
	public RedButton cancelButton;

	public WndChooseAbility(final KingsCrown crown, final Armor armor, final Hero hero){

		super(hero);
		this.crown = crown;
		this.armor = armor;

		//crown can be null if hero is choosing from armor
		titlebar = new IconTitle();
		titlebar.icon( new ItemSprite( crown == null ? armor.image() : crown.image(), null ) );
		titlebar.label( Messages.titleCase(crown == null ? armor.name() : crown.name()) );
		titlebar.setRect( 0, 0, WIDTH, 0 );
		add( titlebar );

		randomButton = new IconButton(Icons.SHUFFLE){
			@Override
			protected void onClick() {
				super.onClick();
				showRandomConfirmation();
			}

			@Override
			public void update() {
				if (Statistics.qualifiedForRandomVictoryBadge){
					image.tint(1, 1, 1, (float)Math.abs(Math.cos(1.5f*Math.PI* Game.timeTotal)/2f));
				}
				super.update();
			}

			@Override
			protected LocalizedString hoverText() {
				return Messages.get(WndChooseAbility.class, "random_title");
			}
		};
		randomButton.setRect(WIDTH-16, 0, 16, 16);
		if (crown != null) add(randomButton);

		message = PixelScene.renderTextBlock( 6 );
		if (crown != null) {
			message.text(Messages.get(this, "message"), WIDTH);
		} else {
			message.text(Messages.get(this, "message_no_crown"), WIDTH);
		}
		message.setPos( titlebar.left(), titlebar.bottom() + GAP );
		add( message );

		float pos = message.bottom() + 3*GAP;
		ArmorAbility[] abilities = hero.heroClass.armorAbilities();
		abilityButtons = new RedButton[abilities.length];
		abilityInfoButtons = new IconButton[abilities.length];
		for (int i = 0; i < abilities.length; i++) {
			ArmorAbility ability = abilities[i];

			RedButton abilityButton = new RedButton(ability.shortDesc(), 6){
				@Override
				protected void onClick() {
					showAbilityConfirmation(ability);
				}
			};
			abilityButton.leftJustify = true;
			abilityButton.multiline = true;
			abilityButton.setSize(WIDTH-20, abilityButton.reqHeight()+2);
			abilityButton.setRect(0, pos, WIDTH-20, abilityButton.reqHeight()+2);
			add(abilityButton);
			abilityButtons[i] = abilityButton;

			IconButton abilityInfo = new IconButton(Icons.INFO){
				@Override
				protected void onClick() {
					GameScene.show(new WndInfoArmorAbility(getOwnerHero().heroClass, ability, getOwnerHero()));
				}
			};
			abilityInfo.setRect(WIDTH-20, abilityButton.top() + (abilityButton.height()-20)/2, 20, 20);
			add(abilityInfo);
			abilityInfoButtons[i] = abilityInfo;

			pos = abilityButton.bottom() + GAP;
		}

		cancelButton = new RedButton(Messages.get(this, "cancel")){
			@Override
			protected void onClick() {
				hide();
			}
		};
		cancelButton.setRect(0, pos, WIDTH, 18);
		add(cancelButton);
		pos = cancelButton.bottom() + GAP;

		resize(WIDTH, (int)pos);

	}

	public KingsCrown crown() {
		return crown;
	}

	public Armor armor() {
		return armor;
	}

	@Override
	protected void onSelect(int button) {
		if (button >= 0 && button < abilityButtons.length) {
			abilityButtons[button].onClickNetwork();
		} else if (button == abilityButtons.length) {
			cancelButton.onClickNetwork();
		} else if (button == abilityButtons.length + 1 && crown != null) {
			randomButton.onClickNetwork();
		}
	}

	@Override
	public void onSelect(int button, @Nullable JSONObject args) {
		if (args != null && args.optBoolean("info", false) && button >= 0 && button < abilityInfoButtons.length) {
			abilityInfoButtons[button].onClickNetwork();
		} else {
			onSelect(button);
		}
	}

	private void showRandomConfirmation() {
		Hero hero = getOwnerHero();
		GameScene.show(new WndOptions(hero, Icons.SHUFFLE.get(),
				Messages.get(WndChooseAbility.class, "random_title"),
				Messages.get(WndChooseAbility.class, "random_sure"),
				Messages.get(WndChooseAbility.class, "yes"),
				Messages.get(WndChooseAbility.class, "no")){
			@Override
			protected void onSelect(int index) {
				super.onSelect(index);
				if (index == 0){
					WndChooseAbility.this.hide();
					ArmorAbility abil = Random.oneOf(hero.heroClass.armorAbilities());
					crown.upgradeArmor(hero, armor, abil);
					GameScene.show(new WndInfoArmorAbility(hero.heroClass, abil, hero));
				}
			}
		});
	}

	private void showAbilityConfirmation(ArmorAbility ability) {
		Hero hero = getOwnerHero();
		GameScene.show(new WndOptions(hero, new HeroIcon( ability ),
				Messages.titleCase(ability.name()),
				Messages.get(WndChooseAbility.this, "are_you_sure"),
				Messages.get(WndChooseAbility.this, "yes"),
				Messages.get(WndChooseAbility.this, "no")){

			@Override
			protected void onSelect(int index) {
				hide();
				if (index == 0){
					WndChooseAbility.this.hide();
					if (crown != null) {
						crown.upgradeArmor(hero, armor, ability);
					} else {
						new KingsCrown().upgradeArmor(hero, null, ability);
					}
					Statistics.qualifiedForRandomVictoryBadge = false;
				}
			}
		});
	}

}
