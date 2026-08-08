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

package com.shatteredpixel.shatteredpixeldungeon.items.spells;

import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Shopkeeper;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.Runestone;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndEnergizeItem;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTradeItem;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUpgrade;
import io.github.pixeldungeonmultiplayer.shattered.server.noosa.audio.Sample;
import com.watabou.utils.Random;
import org.jetbrains.annotations.Contract;

import java.util.ArrayList;

public class Alchemize extends Spell {
	
	{
		image = ItemSpriteSheet.ALCHEMIZE;

		talentChance = 1/(float)Recipe.OUT_QUANTITY;
	}

	private static WndBag parentWnd;
	
	@Override
	protected void onCast(Hero hero) {
		parentWnd = GameScene.selectItem( itemSelector, hero );
	}
	
	@Contract(pure = true)
    @Override
	public int value() {
		//lower value, as it's very cheap to make (and also sold at shops)
		return (int)(20 * (quantity() /(float)Recipe.OUT_QUANTITY));
	}

	@Contract(pure = true)
    @Override
	public int energyVal() {
		return (int)(4 * (quantity() /(float)Recipe.OUT_QUANTITY));
	}

	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe {

		private static final int OUT_QUANTITY = 8;

		@Override
		public boolean testIngredients(ArrayList<Item> ingredients) {
			if (ingredients.size() != 2) return false;

			if (ingredients.get(0) instanceof Plant.Seed && ingredients.get(1) instanceof Runestone){
				return true;
			}

			if (ingredients.get(0) instanceof Runestone && ingredients.get(1) instanceof Plant.Seed){
				return true;
			}

			return false;
		}

		@Override
		public int cost(ArrayList<Item> ingredients) {
			return 2;
		}

		@Override
		public Item brew(ArrayList<Item> ingredients, Hero hero) {
			ingredients.get(0).quantity(ingredients.get(0).quantity()-1);
			ingredients.get(1).quantity(ingredients.get(1).quantity()-1);
			return sampleOutput(null, hero);
		}

		@Override
		public Item sampleOutput(ArrayList<Item> ingredients, Hero hero) {
			return new Alchemize().quantity(OUT_QUANTITY);
		}
	}

	private static WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {
		@Override
		public LocalizedString textPrompt() {
			return Messages.get(Alchemize.class, "prompt");
		}

		@Override
		public boolean itemSelectable(Item item) {
			return !(item instanceof Alchemize)
					&& (Shopkeeper.canSell(item, getOwner()) || item.energyVal() > 0) && !item.isBound();
		}

		@Override
		public void onSelect( Item item) {
			if (item != null) {
				if (parentWnd != null) {
					parentWnd = GameScene.selectItem(itemSelector, getOwner());
				}
				GameScene.show( new WndAlchemizeItem( item, parentWnd ) );
			}
		}
	};


	public static class WndAlchemizeItem extends WndInfoItem {

		private static final float GAP		= 2;
		private static final int BTN_HEIGHT	= 18;

		private WndBag owner;
		public ArrayList<RedButton> buttons = new ArrayList<>();

		public WndAlchemizeItem(Item item, WndBag owner) {
			super(owner.getOwnerHero(), item);
			this.owner = owner;

			float pos = height;

			if (Shopkeeper.canSell(item, getOwnerHero())) {
				if (item.quantity() == 1 || (item instanceof MissileWeapon && item.isUpgradable())) {

					if (item instanceof MissileWeapon && ((MissileWeapon) item).extraThrownLeft){
						RenderedTextBlock warn = PixelScene.renderTextBlock(Messages.get(WndUpgrade.class, "thrown_dust"), 6);
						warn.hardlight(CharSprite.WARNING);
						warn.maxWidth(this.width);
						warn.setPos(0, pos + GAP);
						add(warn);
						pos = warn.bottom();
					}

					RedButton btnSell = new RedButton(Messages.get(this, "sell", item.value())) {
						@Override
						protected void onClick() {
							WndTradeItem.sell(item, owner.getOwnerHero());
							hide();
							consumeAlchemize(owner.getOwnerHero());
						}
					};
					btnSell.setRect(0, pos + GAP, width, BTN_HEIGHT);
					btnSell.icon(new ItemSprite(ItemSpriteSheet.GOLD));
					add(btnSell);
					buttons.add(btnSell);

					pos = btnSell.bottom();

				} else {

					int priceAll = item.value();
					RedButton btnSell1 = new RedButton(Messages.get(this, "sell_1", priceAll / item.quantity())) {
						@Override
						protected void onClick() {
							WndTradeItem.sellOne(item, owner.getOwnerHero());
							hide();
							consumeAlchemize(owner.getOwnerHero());
						}
					};
					btnSell1.setRect(0, pos + GAP, width, BTN_HEIGHT);
					btnSell1.icon(new ItemSprite(ItemSpriteSheet.GOLD));
					add(btnSell1);
					buttons.add(btnSell1);
					RedButton btnSellAll = new RedButton(Messages.get(this, "sell_all", priceAll)) {
						@Override
						protected void onClick() {
							WndTradeItem.sell(item, owner.getOwnerHero());
							hide();
							consumeAlchemize(owner.getOwnerHero());
						}
					};
					btnSellAll.setRect(0, btnSell1.bottom() + 1, width, BTN_HEIGHT);
					btnSellAll.icon(new ItemSprite(ItemSpriteSheet.GOLD));
					add(btnSellAll);
					buttons.add(btnSellAll);

					pos = btnSellAll.bottom();

				}
			}

			if (item.energyVal() > 0 && !item.isBound()) {
				if (item.quantity() == 1) {

					RedButton btnEnergize = new RedButton(Messages.get(this, "energize", item.energyVal())) {
						@Override
						protected void onClick() {
							if (item instanceof Trinket){
								GameScene.show(new WndOptions(owner.getOwnerHero(), new ItemSprite(item), Messages.titleCase(item.name()),
										Messages.get(WndEnergizeItem.class, "trinket_warn"),
										Messages.get(WndEnergizeItem.class, "trinket_yes"),
										Messages.get(WndEnergizeItem.class, "trinket_no")){

									@Override
									protected void onSelect(int index) {
										if (index == 0) {
											WndEnergizeItem.energizeAll(item, getOwnerHero());
										}
									}

									@Override
									public void hide() {
										super.hide();
										WndAlchemizeItem.this.hide();
									}
								});
							} else {
								WndEnergizeItem.energizeAll(item, owner.getOwnerHero());
								hide();
							consumeAlchemize(owner.getOwnerHero());
							}
						}
					};
					btnEnergize.setRect(0, pos + GAP, width, BTN_HEIGHT);
					btnEnergize.icon(new ItemSprite(ItemSpriteSheet.ENERGY));
					add(btnEnergize);
					buttons.add(btnEnergize);

					pos = btnEnergize.bottom();

				} else {

					int energyAll = item.energyVal();
					RedButton btnEnergize1 = new RedButton(Messages.get(this, "energize_1", energyAll / item.quantity())) {
						@Override
						protected void onClick() {
							WndEnergizeItem.energizeOne(item, getOwnerHero());
							hide();
							consumeAlchemize(owner.getOwnerHero());
						}
					};
					btnEnergize1.setRect(0, pos + GAP, width, BTN_HEIGHT);
					btnEnergize1.icon(new ItemSprite(ItemSpriteSheet.ENERGY));
					add(btnEnergize1);
					buttons.add(btnEnergize1);
					RedButton btnEnergizeAll = new RedButton(Messages.get(this, "energize_all", energyAll)) {
						@Override
						protected void onClick() {
							WndEnergizeItem.energizeAll(item, owner.getOwnerHero());
							hide();
							consumeAlchemize(owner.getOwnerHero());
						}
					};
					btnEnergizeAll.setRect(0, btnEnergize1.bottom() + 1, width, BTN_HEIGHT);
					btnEnergizeAll.icon(new ItemSprite(ItemSpriteSheet.ENERGY));
					add(btnEnergizeAll);
					buttons.add(btnEnergizeAll);

					pos = btnEnergizeAll.bottom();

				}
			}

			resize( width, (int)pos );

		}

		@Override
		protected void onSelect(int button) {
			if (button >= 0 && button < buttons.size()) {
				buttons.get(button).onClickNetwork();
			}
		}

		private void consumeAlchemize(Hero hero){
			Sample.INSTANCE.play(Assets.Sounds.TELEPORT);
			if (curItem.quantity() <= 1){
				curItem.detachAll(hero.belongings.backpack);
				if (owner != null) {
					owner.hide();
				}
			} else {
				curItem.detach(hero.belongings.backpack);
				if (owner != null){
					owner.hide();
				}
				GameScene.selectItem(itemSelector, getOwnerHero());
			}
			Catalog.countUse(getClass());
			if (curItem instanceof Alchemize && Random.Float() < ((Alchemize)curItem).talentChance){
				Talent.onScrollUsed(curUser, curUser.pos, ((Alchemize) curItem).talentFactor, curItem.getClass());
			}
		}

	}
}
