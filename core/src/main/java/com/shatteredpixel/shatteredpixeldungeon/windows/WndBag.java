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
import com.shatteredpixel.shatteredpixeldungeon.HeroHelp;
import com.shatteredpixel.shatteredpixeldungeon.SPDAction;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.MagicalHolster;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.PotionBandolier;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.ScrollHolder;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.VelvetPouch;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.input.GameAction;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;

import io.github.pixeldungeonmultiplayer.shattered.server.utils.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WndBag extends WndTabbed {
	
	//only one bag window can appear at a time
	private static final Map<Integer,WndBag> INSTANCE = new HashMap<>();

	private final ItemSelector selector;
	private final LocalizedString title;
	private final List<List<Integer>> allowedItems;
	private final boolean hasListener;

	private static Bag lastBag;

	public WndBag( Bag bag, ItemSelector selector, Hero hero ) {

		super(hero);

		setInstance(hero, this);

		this.selector = selector;

		lastBag = bag;

		int i = 1;
		for (Bag b : getOwnerHero().belongings.getBags()) {
			if (b != null) {
				BagTab tab = new BagTab( b, i++ );
				add( tab );
				tab.select( b == bag );
				if  (b == bag){
					selected = tab;
				}
			}
		}

		layoutTabs();


		title = selector != null ? selector.textPrompt() : null;
		allowedItems = allowedItems(hero);
		hasListener = selector != null;
		//title =	title != null ? Messages.titleCase(title) : Messages.titleCase( bag.name() );
	}

	private static WndBag getInstance(@NotNull Hero hero) {
		if (!INSTANCE.containsKey(HeroHelp.getHeroID(hero))) {
			return null;
		}
		return INSTANCE.get(HeroHelp.getHeroID(hero));
	}
	private static void setInstance(@NotNull Hero hero, @Nullable WndBag instance){
		INSTANCE.put(HeroHelp.getHeroID(hero), instance);
	}
	public ItemSelector getSelector() {
		return selector;
	}

	public LocalizedString title() {
		return title;
	}

	public List<List<Integer>> allowedItems() {
		return allowedItems;
	}

	public boolean hasListener() {
		return hasListener;
	}


	public static WndBag lastBag( ItemSelector selector, Hero hero ) {

		if (lastBag != null && hero.belongings.backpack.contains( lastBag )) {

			return new WndBag( lastBag, selector, hero );
			
		} else {
			
			return new WndBag( hero.belongings.backpack, selector, hero );

		}
	}

	public static WndBag getBag( ItemSelector selector, Hero hero ) {
		if (selector.preferredBag() == Belongings.Backpack.class){
			return new WndBag( hero.belongings.backpack, selector, hero );

		} else if (selector.preferredBag() != null){
			Bag bag = hero.belongings.getItem( selector.preferredBag() );
			if (bag != null)    return new WndBag( bag, selector, hero );
			//if a specific preferred bag isn't present, then the relevant items will be in backpack
			else                return new WndBag( hero.belongings.backpack, selector, hero );
		}

		return lastBag( selector, hero );
	}

	protected List<List<Integer>> allowedItems(Hero hero) {
		List<List<Integer>> result = new ArrayList<List<Integer>>(3);
		for (Item item : hero.belongings) {
			if (item == null) {
				continue;
			}
			if (selector == null || selector.itemSelectable(item)) {
				result.add(item.getSlot(hero));
			}
		}
		return result;
	}
	
	@Override
	public void onBackPressed() {
		if (selector != null) {
			selector.onSelect( null );
		}
		super.onBackPressed();
	}
	
	@Override
	protected void onClick( Tab tab ) {
		hide();
		Window w = new WndBag(((BagTab) tab).bag, selector, getOwnerHero());
		if (Game.scene() instanceof GameScene){
			GameScene.show(w);
		} else {
			Game.scene().addToFront(w);
		}
	}
	
	@Override
	public void hide() {
		super.hide();
		setInstance(getOwnerHero(), null);
	}
	
	@Override
	protected int tabHeight() {
		return 20;
	}
	
	private Image icon( Bag bag ) {
		if (bag instanceof VelvetPouch) {
			return Icons.get( Icons.SEED_POUCH );
		} else if (bag instanceof ScrollHolder) {
			return Icons.get( Icons.SCROLL_HOLDER );
		} else if (bag instanceof MagicalHolster) {
			return Icons.get( Icons.WAND_HOLSTER );
		} else if (bag instanceof PotionBandolier) {
			return Icons.get( Icons.POTION_BANDOLIER );
		} else {
			return Icons.get( Icons.BACKPACK );
		}
	}
	public void onSelect(int button, @Nullable JSONObject args) {
		selector.owner = getOwnerHero();
		if (button == -1) {
			selector.onSelect(null);
			hide();
		}  else {
			selector.onSelect(getOwnerHero().belongings.getItemInSlot(Utils.JsonArrayToListInteger(args.getJSONArray("item_path"))));
			hide();
		}
	}
	public static JSONArray listToJsonArray(List<List<Integer>> arg) {
		JSONArray result = new JSONArray();
		for (int i = 0; i < arg.size(); i++) {
			List<Integer> curr_arr = arg.get(i);
			JSONArray cur_json_arr = new JSONArray();
			for (int j = 0; j < curr_arr.size(); j++) {
				cur_json_arr.put(curr_arr.get(j));
			}
			result.put(cur_json_arr);
		}
		return result;
	}

	private class BagTab extends IconTab {

		private Bag bag;
		private int index;
		
		public BagTab( Bag bag, int index ) {
			super( icon(bag) );
			
			this.bag = bag;
			this.index = index;
		}

		@Override
		public GameAction keyAction() {
			switch (index){
				case 1: default:
					return SPDAction.BAG_1;
				case 2:
					return SPDAction.BAG_2;
				case 3:
					return SPDAction.BAG_3;
				case 4:
					return SPDAction.BAG_4;
				case 5:
					return SPDAction.BAG_5;
			}
		}

		@Override
		protected LocalizedString hoverText() {
			return Messages.titleCase(bag.name());
		}
	}
	
	public static class Placeholder extends Item {

		public Placeholder(int image ) {
			this.image = image;
		}

		@Override
		public LocalizedString name() {
			return null;
		}

		@Override
		public boolean isIdentified() {
			return true;
		}
		
		@Override
		public boolean isEquipped( Hero hero ) {
			return true;
		}
	}

	public abstract static class ItemSelector {
		public Hero owner = null;
		public abstract LocalizedString textPrompt();
		public Class<?extends Bag> preferredBag(){
			return null; //defaults to last bag opened
		}
		public boolean hideAfterSelecting(){
			return true; //defaults to hiding the window when an item is picked
		}
		public abstract boolean itemSelectable( Item item );
		public void onSelect(Item item){
			onSelect(item, owner);
		};
		@Deprecated
		public void onSelect( Item item, Hero hero ){
		};

		public ItemSelector() {
		}

		public Hero getOwner() {
			return owner;
		}

		public void setOwner(Hero owner) {
			this.owner = owner;
		}

		public ItemSelector(Hero owner) {
			setOwner(owner);
		}
	}
}
