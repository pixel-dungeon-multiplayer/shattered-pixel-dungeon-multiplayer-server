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

package com.shatteredpixel.shatteredpixeldungeon.items.stones;

import com.nikita22007.multiplayer.utils.text.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Identification;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.ExoticPotion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ExoticScroll;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;
import com.watabou.noosa.Image;
import com.watabou.utils.Reflection;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class StoneOfIntuition extends InventoryStone {
	
	{
		image = ItemSpriteSheet.STONE_INTUITION;
	}

	@Override
	protected boolean usableOnItem(Item item) {
		if (item instanceof Ring){
			return !((Ring) item).isKnown();
		} else if (item instanceof Potion){
			return !((Potion) item).isKnown();
		} else if (item instanceof Scroll){
			return !((Scroll) item).isKnown();
		}
		return false;
	}
	
	@Override
	protected void onItemSelected(Item item, Hero hero) {

		//GameScene.show( new WndGuess(item));
		WndGuess wndGuess = new WndGuess(item, hero);
		GameScene.show(wndGuess);
	}

	@Override
	public LocalizedString desc(Hero hero) {
		LocalizedString text = super.desc(hero);
		if (hero != null){
			if (hero.buff(IntuitionUseTracker.class) == null){
				text = LocalizedString.concat(text, LocalizedString.concat("\n\n", Messages.get(this, "break_info")));
			} else {
				text = LocalizedString.concat(text, LocalizedString.concat("\n\n", Messages.get(this, "break_warn")));
			}
		}
		return text;
	}

	public static class IntuitionUseTracker extends Buff {{ revivePersists = true; }};
	
	private static Class curGuess = null;

	public class WndGuess extends Window {
		
		private static final int WIDTH = 120;
		private static final int BTN_SIZE = 20;
		ArrayList<Integer> icons = new ArrayList<>();
		ArrayList<Class<? extends Item>> guessOptions = new ArrayList<>();
		Item item;
		public WndGuess(final Item item, Hero hero){
			super(hero);
			this.item = item;
			IconTitle titlebar = new IconTitle();
			titlebar.icon( new ItemSprite(item) );
			titlebar.label( Messages.titleCase(item.name()) );
			titlebar.setRect( 0, 0, WIDTH, 0 );
			add( titlebar );
			
			RenderedTextBlock text = PixelScene.renderTextBlock(6);
			text.text( Messages.get(this, "text") );
			text.setPos(0, titlebar.bottom()+2);
			text.maxWidth( WIDTH );
			add(text);
			
			final RedButton guess = new RedButton(""){
				@Override
				public void onClick() {
					super.onClick();
				}
			};
			guess.visible = false;
			guess.icon( new ItemSprite(item) );
			guess.enable(false);
			guess.setRect(0, 80, WIDTH, 20);
			add(guess);
			
			float left;
			float top = text.bottom() + 5;
			int rows;
			int placed = 0;
			
			final ArrayList<Class<?extends Item>> unIDed = new ArrayList<>();
			if (item.isIdentified()){
				hide();
				return;
			} else if (item instanceof Potion){
				if (item instanceof ExoticPotion) {
					for (Class<?extends Item> i : Potion.getUnknown()){
						unIDed.add(ExoticPotion.regToExo.get(i));
					}
				} else {
					unIDed.addAll(Potion.getUnknown());
				}
			} else if (item instanceof Scroll){
				if (item instanceof ExoticScroll) {
					for (Class<?extends Item> i : Scroll.getUnknown()){
						unIDed.add(ExoticScroll.regToExo.get(i));
					}
				} else {
					unIDed.addAll(Scroll.getUnknown());
				}
			} else if (item instanceof Ring) {
				unIDed.addAll(Ring.getUnknown());
			} else {
				hide();
				return;
			}
			
			if (unIDed.size() <= 5){
				rows = 1;
				top += BTN_SIZE/2f;
				left = (WIDTH - BTN_SIZE*unIDed.size())/2f;
			} else {
				rows = 2;
				left = (WIDTH - BTN_SIZE*((unIDed.size()+1)/2))/2f;
			}
			
			for (final Class<?extends Item> i : unIDed){

				IconButton btn = new IconButton(){
					@Override
					protected void onClick() {
						curGuess = i;
						guess.visible = true;
						guess.text( Messages.titleCase(Messages.get(curGuess, "name")) );
						guess.enable(true);
						super.onClick();
					}
				};
				Image im = new Image(Assets.Sprites.ITEM_ICONS);
				int icon = Reflection.newInstance(i).icon;
				icons.add(icon);
				guessOptions.add(i);
				im.frame(ItemSpriteSheet.Icons.film.get(icon));
				im.scale.set(2f);
				btn.icon(im);
				btn.setRect(left + placed*BTN_SIZE, top, BTN_SIZE, BTN_SIZE);
				add(btn);
				
				placed++;
				if (rows == 2 && placed == ((unIDed.size()+1)/2)){
					placed = 0;
					if (unIDed.size() % 2 == 1){
						left += BTN_SIZE/2f;
					}
					top += BTN_SIZE;
				}
			}
			
			resize(WIDTH, 100);
			
		}

		public Item item() {
			return item;
		}

		public ArrayList<Integer> icons() {
			return icons;
		}

		public ArrayList<Class<? extends Item>> guessOptions() {
			return guessOptions;
		}

		@Override
		protected void onSelect(int button) {
			super.onSelect(button);
			if (button < 100 && button > 0 && button < guessOptions.size()){
				curGuess = guessOptions.get(button);
			}
			if (button == 100) {
				useAnimation();
				Catalog.countUse(StoneOfIntuition.class);
				if (item.getClass() == curGuess){
					if (item instanceof Ring){
						((Ring) item).setKnown();
					} else {
						item.identify(null);
					}
					GLog.p( Messages.get(WndGuess.class, "correct") );
					curUser.getSprite().parent.add( new Identification( curUser.getSprite().center().offset( 0, -16 ) ) );
				} else {
					GLog.w( Messages.get(WndGuess.class, "incorrect") );
				}
				if (!anonymous) {
					Catalog.countUse(StoneOfIntuition.class);
					if (curUser.buff(IntuitionUseTracker.class) == null) {
						Buff.affect(curUser, IntuitionUseTracker.class);
					} else {
						curItem.detach(curUser.belongings.backpack);
						curUser.buff(IntuitionUseTracker.class).detach();
					}
					Talent.onRunestoneUsed(curUser, curUser.pos, StoneOfIntuition.class);
				}
				curGuess = null;
				hide();
			}
		}


	}
}
