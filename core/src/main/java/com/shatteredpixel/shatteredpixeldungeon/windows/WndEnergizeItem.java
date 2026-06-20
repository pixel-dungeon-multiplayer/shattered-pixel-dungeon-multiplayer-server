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
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.EnergyCrystal;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.AlchemyScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;

import java.util.ArrayList;

public class WndEnergizeItem extends WndInfoItem {

	private static final float GAP = 2;
	private static final int BTN_HEIGHT = 18;

	private WndBag owner;
	private final Item item;
	public final ArrayList<RedButton> buttons = new ArrayList<>();

	public WndEnergizeItem(Hero hero, Item item, WndBag ownerWnd) {
		super(hero, item);

		this.owner = ownerWnd;
		this.item = item;

		float pos = height;

		if (item.quantity() == 1) {

			RedButton btnEnergize = new RedButton(Messages.get(this, "energize", item.energyVal())) {
				@Override
				protected void onClick() {
					if (item instanceof Trinket){
						Game.scene().addToFront(new WndOptions(hero, new ItemSprite(item), Messages.titleCase(item.name()),
								Messages.get(WndEnergizeItem.class, "trinket_warn"),
								Messages.get(WndEnergizeItem.class, "trinket_yes"),
								Messages.get(WndEnergizeItem.class, "trinket_no")){

							@Override
							protected void onSelect(int index) {
								if (index == 0) {
									energizeAll(item, getOwnerHero());
								}
								openItemSelector(getOwnerHero());
							}

							@Override
							public void hide() {
								super.hide();
								WndEnergizeItem.this.hide();
							}
						});
					} else {energizeAll(item, getOwnerHero());
						hide();
					}
				}
			};
			btnEnergize.setRect(0, pos + GAP, width, BTN_HEIGHT);
			btnEnergize.icon(new ItemSprite(ItemSpriteSheet.ENERGY));
			buttons.add(btnEnergize);
			add(btnEnergize);

			pos = btnEnergize.bottom();

		} else {

			int energyAll = item.energyVal();
			RedButton btnEnergize1 = new RedButton(Messages.get(this, "energize_1", energyAll / item.quantity())) {
				@Override
				protected void onClick() {
					energizeOne(item, getOwnerHero());
					hide();
				}
			};
			btnEnergize1.setRect(0, pos + GAP, width, BTN_HEIGHT);
			btnEnergize1.icon(new ItemSprite(ItemSpriteSheet.ENERGY));
			buttons.add(btnEnergize1);
			add(btnEnergize1);
			RedButton btnEnergizeAll = new RedButton(Messages.get(this, "energize_all", energyAll)) {
				@Override
				protected void onClick() {
					energizeAll(item, getOwnerHero());
					hide();
				}
			};
			btnEnergizeAll.setRect(0, btnEnergize1.bottom() + 1, width, BTN_HEIGHT);
			btnEnergizeAll.icon(new ItemSprite(ItemSpriteSheet.ENERGY));
			buttons.add(btnEnergizeAll);
			add(btnEnergizeAll);

			pos = btnEnergizeAll.bottom();

		}

		resize(width, (int) pos);

	}

	public Item item() {
		return item;
	}

	@Override
	public void hide() {

		super.hide();

		if (owner != null) {
			owner.hide();
			openItemSelector(getOwnerHero());
		}
	}


	public static void energizeAll(Item item, Hero hero ) {


		if (item.isEquipped( hero ) && !((EquipableItem)item).doUnequip( hero, false )) {
			return;
		}
		item.detachAll( hero.belongings.backpack );

		AlchemyScene alchemyScene = AlchemyScene.getActiveAlchemyScene(hero);
		if (alchemyScene != null){

			Dungeon.energy += item.energyVal();
			alchemyScene.createEnergy();

		} else {

			//selling items in the sell interface doesn't spend time
			hero.spend(-hero.cooldown());

			new EnergyCrystal(item.energyVal()).doPickUp(hero);

		}
	}

	public static void energizeOne( Item item, Hero hero ) {

		if (item.quantity() <= 1) {
			energizeAll(item, hero);
		} else {
			energize(item.detach(hero.belongings.backpack), hero);
		}


		item = item.detach(hero.belongings.backpack);

		AlchemyScene alchemyScene = AlchemyScene.getActiveAlchemyScene(hero);
		if (alchemyScene != null) {

			Dungeon.energy += item.energyVal();
			alchemyScene.createEnergy();
			if (!item.isIdentified()) {
				//TODO: check this
				//AlchemyScene.showIdentify(item);
			}

		} else {

			//energizing items doesn't spend time
			hero.spend(-hero.cooldown());
			new EnergyCrystal(item.energyVal()).doPickUp(hero);
			item.identify(hero);
			GLog.h(LocalizedString.concat("You energized: ", item.name()));

		}
	}
	private static void energize(Item item, Hero hero) {
		if (!item.isBound()) {
			//energizing items doesn't spend time
			hero.spend(-hero.cooldown());
			new EnergyCrystal(item.energyVal()).doPickUp(hero);
			item.identify(hero);
			GLog.h(LocalizedString.concat("You energized: ", item.name()));
		}
	}

	public static WndBag openItemSelector(Hero hero){
		if (ShatteredPixelDungeon.scene() instanceof GameScene) {
			return GameScene.selectItem( selector, hero );
		} else {
			WndBag window = WndBag.getBag( selector, hero );
			ShatteredPixelDungeon.scene().addToFront(window);
			return window;
		}
	}

	public static WndBag.ItemSelector selector = new WndBag.ItemSelector() {
		@Override
		public LocalizedString textPrompt() {
			return Messages.get(WndEnergizeItem.class, "prompt");
		}

		@Override
		public boolean itemSelectable(Item item) {
			return item.energyVal() > 0;
		}

		@Override
		public void onSelect(Item item) {
			if (item != null) {
				WndBag parentWnd = openItemSelector(getOwner());
				if (ShatteredPixelDungeon.scene() instanceof GameScene) {
					GameScene.show(new WndEnergizeItem(owner, item, parentWnd));
				} else {
					ShatteredPixelDungeon.scene().addToFront(new WndEnergizeItem(owner, item, parentWnd));
				}
			}
		}
	};
}
