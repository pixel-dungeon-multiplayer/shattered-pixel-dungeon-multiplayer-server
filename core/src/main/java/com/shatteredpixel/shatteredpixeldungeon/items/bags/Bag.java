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

package com.shatteredpixel.shatteredpixeldungeon.items.bags;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LostInventory;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.DeviceCompat;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

public abstract class Bag extends Item implements Iterable<Item> {
	public Item getItemInSlot(List<Integer> slot) {
		int slot_id = slot.get(0);
		slot.remove(0);
		if (slot.isEmpty()){
			return items.get(slot_id);
		}
		else
		{
			return ((Bag) items.get(slot_id)).getItemInSlot(slot);
		}
	}
	public List<Integer> pathOfItem(Item item) {
		assert (item != null) : "path of null item";
		for (int i = 0; i < items.size(); i++) {
			Item cur_item = items.get(i);
			if (cur_item == null) {
				continue;
			}
			if (cur_item == item) {
				List<Integer> path = new ArrayList<>(2);
				path.add(i);
				return path;
			}
			if (cur_item instanceof Bag) {
				List<Integer> path = ((Bag) cur_item).pathOfItem(item);
				if (path != null) {
					path.add(0, i);
					return path;
				}
			}
		}
		return null;
	}

	//make sure that it is client-only action
	public static final String AC_OPEN	= "OPEN";
	
	{
		image = 11;
		
		defaultAction = AC_OPEN;

		unique = true;
	}
	
	public Hero owner;

	@NotNull
	private final ArrayList<@NotNull Item> items = new ArrayList<>();

	@Contract(pure = true)
	public final @UnmodifiableView @NotNull List<@NotNull Item> items() {
		return Collections.unmodifiableList(items);
	}

	@Contract(pure = true)
	public int capacity(){
		return 20; // default container size
	}

	//if an item is being quick-used from the bag, the bag should take on its targeting properties
	public Item quickUseItem;

	@Override
	public int targetingPos(@NotNull Hero user, int dst) {
		if (quickUseItem != null){
			return quickUseItem.targetingPos(user, dst);
		} else {
			return super.targetingPos(user, dst);
		}
	}

	@Override
	public void execute( Hero hero, String action ) {
		quickUseItem = null;

		super.execute( hero, action );

		if (action.equals( AC_OPEN ) && !items.isEmpty()) {
			assert DeviceCompat.isDebug();
			//GameScene.show( new WndQuickBag( this, hero ) );
			GameScene.show( new WndBag( this,  null, hero ) );
		}
	}
	
	@Override
	public boolean collect( Bag container ) {
		Hero previousOwner = owner;
		owner = container.owner;
		if (!super.collect( container )) {
			owner = previousOwner;
			return false;
		}

		grabItems(container);

		//if there are any quickslot placeholders that match items in this bag, assign them
		for (Item item : items) {
			Dungeon.quickslot.replacePlaceholder(item);
		}

		Badges.validateAllBagsBought( this );

		return true;
	}

	@Override
	public void onDetach( ) {
		this.owner = null;
		for (Item item : items) {
			Dungeon.quickslot.clearItem(item);
		}
		updateQuickslot();
	}

	public void grabItems(){
		if (owner != null && owner instanceof Hero && this != ((Hero) owner).belongings.backpack) {
			grabItems(((Hero) owner).belongings.backpack);
		}
	}

	public void grabItems( Bag container ){
		if (owner == null){
			owner = container.owner;
		}
		for (Item item : container.items.toArray( new Item[0] )) {
			if (canHold( item )) {
				int slot = Dungeon.quickslot.getSlot(item);
				item.detachAll(container);
				List<Integer> bagPath = owner instanceof Hero
						? ((Hero) owner).belongings.pathOfItem(this)
						: null;
				boolean collected = bagPath == null
						? item.collect(this)
						: item.collect(this, new ArrayList<>(bagPath)) != null;
				if (!collected) {
					item.collect(container);
				}
				if (slot != -1) {
					Dungeon.quickslot.setSlot(slot, item);
				}
			}
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
	
	public void clear() {
		items.clear();
	}
	
	public void resurrect() {
        items.removeIf(item -> !item.unique);
	}
	
	private static final String ITEMS	= "inventory";
	
	@Override
	public void storeInBundle(@NotNull Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( ITEMS, items );
	}

	//temp variable so that bags can load contents even with lost inventory debuff
	private boolean loading;

	@Override
	public void restoreFromBundle(@NotNull Bundle bundle ) {
		super.restoreFromBundle( bundle );

		loading = true;
		for (Bundlable item : bundle.getCollection( ITEMS )) {
			if (item != null){
				if (!((Item)item).collect( this )){
					//force-add the item if necessary, such as if its item category changed after an update
					items.add((Item) item);
				}
			}
		}
		loading = false;
	}
	
	public boolean contains( Item item ) {
		for (Item i : items) {
			if (i == item) {
				return true;
			} else if (i instanceof Bag && ((Bag)i).contains( item )) {
				return true;
			}
		}
		return false;
	}

	public boolean canHold( Item item ){
		if (!loading && owner != null && owner.buff(LostInventory.class) != null
			&& !item.keptThroughLostInventory()){
			return false;
		}

		if (items.contains(item) || item instanceof Bag || items.size() < capacity()){
			return true;
		} else if (item.stackable) {
			for (Item i : items) {
				if (item.isSimilar( i )) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public @NotNull Iterator<Item> iterator() {
		return new ItemIterator();
	}

	public abstract @NotNull Icons getBagIcon();

	public boolean addItemDirect(Item item) {
		if (items.contains(item)) {
			return false;
		}
		items.add(item);
		items.sort(itemComparator);
		return true;
	}

	public boolean removeItemDirect(Item item) {
		return items.remove(item);
	}

	private class ItemIterator implements Iterator<Item> {

		private int index = 0;
		private Iterator<Item> nested = null;
		
		@Override
		public boolean hasNext() {
			if (nested != null) {
				return nested.hasNext() || index < items.size();
			} else {
				return index < items.size();
			}
		}

		@Override
		public Item next() {
			if (nested != null && nested.hasNext()) {
				
				return nested.next();
				
			} else {
				
				nested = null;
				
				Item item = items.get( index++ );
				if (item instanceof Bag) {
					nested = ((Bag)item).iterator();
				}
				
				return item;
			}
		}

		@Override
		public void remove() {
			if (nested != null) {
				nested.remove();
			} else {
				items.remove( index );
			}
		}
	}
}
