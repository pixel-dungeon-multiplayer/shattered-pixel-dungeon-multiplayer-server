/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.nikita22007.multiplayer.utils.text.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.EnergyCrystal;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.LiquidMetal;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.AlchemistsToolkit;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.TrinketCatalyst;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.journal.Journal;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.network.SendData;
import com.shatteredpixel.shatteredpixeldungeon.network.actions.UpdateWindowAction;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.*;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndEnergizeItem;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem;
import com.watabou.input.GameAction;
import com.watabou.noosa.Game;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.WeakHashMap;

public class AlchemyScene extends Window {

	//buttons
	public final @NotNull IconButton cancel;
	public final @NotNull IconButton repeat;
	public final @NotNull IconButton energyAdd;
	public final @NotNull ExitButton btnExit;
	//max of 3 inputs, and 3 potential recipe outputs
	public final InputButton[] inputs = new InputButton[3];
	public final CombineButton[] combines = new CombineButton[3];
	public final OutputSlot[] outputs = new OutputSlot[3];

	//synchronized visual state
	public final ItemSprite energyIcon;
	public @NotNull LocalizedString energyText;
	public boolean energyAddBlinking = false;
	//triggers (hack)
	public boolean craftEffect = false;
	public boolean createEnergyEffect = false;
	public @Nullable IdentifyEffect identifyEffect = null;

	//logic
	private static ArrayList<Item> lastIngredients = new ArrayList<>();
	private static Recipe lastRecipe = null;
	private final @Nullable AlchemistsToolkit toolkit;

	//network key codes
	private static final int CONTROL_BUTTON_GROUP = 000;
	private static final int INPUT_BUTTON_GROUP = 100;
	private static final int COMBINE_BUTTON_GROUP = 200;
	private static final int OUTPUT_BUTTON_GROUP = 300;


	private static final int CANCEL_BUTTON = CONTROL_BUTTON_GROUP + 0;
	private static final int REPEAT_BUTTON = CONTROL_BUTTON_GROUP + 1;
	private static final int ENERGY_ADD_BUTTON = CONTROL_BUTTON_GROUP + 2;
	private static final int EXIT_BUTTON = CONTROL_BUTTON_GROUP + 3;

	@Override
	public void hide() {
		disableAlchemyScene(getOwnerHero());
		super.hide();
	}

	@Override
	public void onSelect(int button, @Nullable JSONObject args) {
		Button buttonObj = null;
		if (button < INPUT_BUTTON_GROUP) {
			switch (button) {
				case CANCEL_BUTTON: {
					buttonObj = cancel;
					break;
				}
				case REPEAT_BUTTON: {
					buttonObj = repeat;
					break;
				}
				case ENERGY_ADD_BUTTON: {
					buttonObj = energyAdd;
					break;
				}
				case EXIT_BUTTON: {
					buttonObj = btnExit;
					break;
				}
			}
		} else if (button < COMBINE_BUTTON_GROUP) {
			int index = button - INPUT_BUTTON_GROUP;
			if (index < inputs.length) {
				buttonObj = inputs[index].slot;
			}
		} else if  (button < OUTPUT_BUTTON_GROUP) {
			int index = button - COMBINE_BUTTON_GROUP;
			if (index < combines.length) {
				buttonObj = combines[index].button;
			}
		} else {
			int index = button - OUTPUT_BUTTON_GROUP;
			if (index < outputs.length) {
				buttonObj = outputs[index].slot;
			}
		}
		if (buttonObj != null) {
			if (args != null && args.optBoolean("long_click", false)) {
				buttonObj.onLongClickNetwork();
			} else {
				buttonObj.onClickNetwork();
			}
		}
	}

	public AlchemyScene(final @NotNull Hero hero, final @Nullable AlchemistsToolkit toolkit) {
		super(hero);
		this.toolkit = toolkit;
		enableAlchemyScene(hero);

	//public void create() {

		btnExit = new ExitButton(){
			@Override
			protected void onClick() {
				hide();
			}
		};


		synchronized (inputs) {
			for (int i = 0; i < inputs.length; i++) {
				if (inputs[i] == null) {
					inputs[i] = new InputButton();
				} else {
					//in case the scene was reset without calling destroy() for some reason
					Item item = inputs[i].item();
					inputs[i] = new InputButton();
					if (item != null){
						inputs[i].item(item);
					}
				}
			}
		}

		cancel = new IconButton(Icons.CLOSE.get()){
			@Override
			protected void onClick() {
				super.onClick();
				clearSlots();
				updateState();
			}

			@Override
			public GameAction keyAction() {
				return SPDAction.BACK;
			}

			@Override
			protected LocalizedString hoverText() {
				return Messages.get(AlchemyScene.class, "cancel");
			}
		};
		cancel.enable(false);
		add(cancel);

		repeat = new IconButton(Icons.REPEAT.get()){
			@Override
			protected void onClick() {
				super.onClick();
				if (lastRecipe != null){
					populate(lastIngredients, hero.belongings);
				}
			}

			@Override
			public GameAction keyAction() {
				return SPDAction.TAG_RESUME;
			}

			@Override
			protected LocalizedString hoverText() {
				return Messages.get(AlchemyScene.class, "repeat");
			}
		};


		lastIngredients.clear();
		lastRecipe = null;

		for (int i = 0; i < inputs.length; i++){
			combines[i] = new CombineButton(i);
			combines[i].enable(false);

			outputs[i] = new OutputSlot();
			outputs[i].item(null);

			if (i == 0){
				//first ones are always visible
				combines[i].visible = true;
				outputs[i].visible = true;
			} else {
				combines[i].visible = false;
				outputs[i].visible = false;
			}

			add(combines[i]);
			add(outputs[i]);
		}

		updateEnergyText();

		energyIcon = new ItemSprite( toolkit != null ? ItemSpriteSheet.ARTIFACT_TOOLKIT : ItemSpriteSheet.ENERGY);

		energyAdd = new IconButton(Icons.get(Icons.PLUS)){

			@Override
			protected void onClick() {
				WndEnergizeItem.openItemSelector(getOwnerHero());
			}

			@Override
			public GameAction keyAction() {
				return SPDAction.TAG_ACTION;
			}

			@Override
			protected LocalizedString hoverText() {
				return Messages.get(AlchemyScene.class, "energize");
			}
		};

		//StyledButton btnGuide = new StyledButton( ...

		TrinketCatalyst cata = hero.belongings.getItem(TrinketCatalyst.class);
		if (cata != null && cata.hasRolledTrinkets()){
			GameScene.show(new TrinketCatalyst.WndTrinket(hero, cata));
		}

		saveNeeded = false;
		try {
			Dungeon.saveAll();
			Badges.saveGlobal();
			Journal.saveGlobal();
		} catch (IOException e) {
			ShatteredPixelDungeon.reportException(e);
		}
		//sendUpdateThis()/updateState() not needed, it will be sent inside GameScene.show();
	}


	@Override
	public void onBackPressed() {
		hide();
	}

	private void sendUpdateThis() {
		SendData.sendLateLiveStateAction(getOwnerHero(), new UpdateWindowAction(this));
		SendData.forceFlush(getOwnerHero());
	}

	protected WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {

		@Override
		public LocalizedString textPrompt() {
			return Messages.get(AlchemyScene.class, "select");
		}

		@Override
		public boolean itemSelectable(Item item) {
			return Recipe.usableInRecipe(item);
		}

		@Override
		public void onSelect( Item item ) {
			synchronized (inputs) {
				if (item != null && inputs[0] != null) {
					for (int i = 0; i < inputs.length; i++) {
						if (inputs[i].item() == null) {
							if (item instanceof LiquidMetal || item instanceof MissileWeapon){
								inputs[i].item(item.detachAll(getOwner().belongings.backpack));
							} else {
								inputs[i].item(item.detach(getOwner().belongings.backpack));
							}
							break;
						}
					}
					updateState();
				}
			}
		}
	};

	@SuppressWarnings("unchecked")
	private<T extends Item> ArrayList<T> filterInput(Class<? extends T> itemClass){
		ArrayList<T> filtered = new ArrayList<>();
		for (int i = 0; i < inputs.length; i++){
			Item item = inputs[i].item();
			if (item != null && itemClass.isInstance(item)){
				filtered.add((T)item);
			}
		}
		return filtered;
	}

	private void updateState(){

		repeat.enable(false);

		ArrayList<Item> ingredients = filterInput(Item.class);
		ArrayList<Recipe> recipes = Recipe.findRecipes(ingredients);

		//disables / hides unneeded buttons
		for (int i = recipes.size(); i < combines.length; i++){
			combines[i].enable(false);
			outputs[i].item(null);

			if (i != 0){
				combines[i].visible = false;
				outputs[i].visible = false;
			}
		}

		cancel.enable(!ingredients.isEmpty());

		if (recipes.isEmpty()){
			combines[0].setPos(combines[0].left(), inputs[1].top()+5);
			outputs[0].setPos(outputs[0].left(), inputs[1].top());
			energyAddBlinking = false;
		} else {

		//positions and enables active buttons
		boolean promptToAddEnergy = false;
		for (int i = 0; i < recipes.size(); i++){

			Recipe recipe = recipes.get(i);

			int cost = recipe.cost(ingredients);

			outputs[i].visible = true;
			outputs[i].item(recipe.sampleOutput(ingredients, getOwnerHero()));

			int availableEnergy = Dungeon.energy;
			if (toolkit != null){
				availableEnergy += toolkit.availableEnergy();
			}

			combines[i].visible = true;
			combines[i].enable(cost <= availableEnergy, cost);

			if (cost > availableEnergy && recipe instanceof TrinketCatalyst.Recipe){
				promptToAddEnergy = true;
			}

		}

		energyAddBlinking = promptToAddEnergy;

		}
		sendUpdateThis();
		craftEffect = false;
		createEnergyEffect = false;
		identifyEffect = null;
	}

	private void combine( int slot ){

		ArrayList<Item> ingredients = filterInput(Item.class);
		if (ingredients.isEmpty()) return;

		lastIngredients.clear();
		for (Item i : ingredients){
			lastIngredients.add(i.duplicate());
		}

		ArrayList<Recipe> recipes = Recipe.findRecipes(ingredients);
		if (recipes.size() <= slot) return;

		Recipe recipe = recipes.get(slot);

		Item result = null;

		if (recipe != null){
			int cost = recipe.cost(ingredients);
			if (toolkit != null){
				cost = toolkit.consumeEnergy(cost, getOwnerHero());
			}
			Catalog.countUses(EnergyCrystal.class, cost);
			Dungeon.energy -= cost;

			updateEnergyText();

			result = recipe.brew(ingredients, getOwnerHero());
		}

		if (result != null){

			craftItem(ingredients, result);

		}

		boolean foundItems = true;
		for (Item i : lastIngredients){
			Item found = getOwnerHero().belongings.getSimilar(i);
			if (found == null){ //atm no quantity check as items are always loaded individually
				//currently found can be true if we need, say, 3x of an item but only have 2x of it
				foundItems = false;
			}
		}

		lastRecipe = recipe;
		repeat.enable(foundItems);

		cancel.enable(false);
		synchronized (inputs) {
			for (int i = 0; i < inputs.length; i++) {
				if (inputs[i] != null && inputs[i].item() != null) {
					cancel.enable(true);
					break;
				}
			}
		}
	}

	public void craftItem( ArrayList<Item> ingredients, Item result ){
		craftEffect = true;

		int resultQuantity = result.quantity();
		if (!result.collect(getOwnerHero())){
			Dungeon.level.drop(result, getOwnerHero().pos);
		}

		Statistics.itemsCrafted++;
		Badges.validateItemsCrafted();

		saveNeeded = false;
		try {
			Dungeon.saveAll();
			Badges.saveGlobal();
			Journal.saveGlobal();
		} catch (IOException e) {
			ShatteredPixelDungeon.reportException(e);
		}

		synchronized (inputs) {
			for (int i = 0; i < inputs.length; i++) {
				if (inputs[i] != null && inputs[i].item() != null) {
					Item item = inputs[i].item();
					if (item.quantity() <= 0) {
						inputs[i].item(null);
					} else {
						inputs[i].slot.updateText();
					}
				}
			}
		}

		//we reset the quantity in case the result was merged into another stack in the backpack
		result.quantity(resultQuantity);
		outputs[0].item(result);
	}

	public void populate(ArrayList<Item> toFind, Belongings inventory){
		clearSlots();

		int curslot = 0;
		for (Item finding : toFind){
			int needed = finding.quantity();
			ArrayList<Item> found = inventory.getAllSimilar(finding);
			while (!found.isEmpty() && needed > 0){
				Item detached;
				if (finding instanceof LiquidMetal || finding instanceof MissileWeapon) {
					detached = found.get(0).detachAll(inventory.backpack);
				} else {
					detached = found.get(0).detach(inventory.backpack);
				}
				inputs[curslot].item(detached);
				curslot++;
				needed -= detached.quantity();
				if (detached == found.get(0)) {
					found.remove(0);
				}
			}
		}
		updateState();
	}

	private boolean saveNeeded = false;

	@Override
	public void destroy() {
		synchronized ( inputs ) {
			clearSlots();
            Arrays.fill(inputs, null);
		}

		saveNeeded = false;
		try {
			Dungeon.saveAll();
			Badges.saveGlobal();
			Journal.saveGlobal();
		} catch (IOException e) {
			ShatteredPixelDungeon.reportException(e);
		}
		super.destroy();
	}

	public void clearSlots(){
		synchronized ( inputs ) {
			for (int i = 0; i < inputs.length; i++) {
				if (inputs[i] != null && inputs[i].item() != null) {
					Item item = inputs[i].item();
					if (!item.collect(getOwnerHero())) {
						Dungeon.level.drop(item, getOwnerHero().pos);
					}
					inputs[i].item(null);
				}
			}
		}
		cancel.enable(false);
		repeat.enable(lastRecipe != null);
    }

	public void createEnergy(){
		updateEnergyText();

		//todo send this as visual action

		//queue a save here, as items may be in the input windows and we don't want to clear them
		// but if the game becomes paused we do this to prevent exploits
		saveNeeded = true;
		updateState();
	}

	private void updateEnergyText() {
		energyText = LocalizedString.concat(Messages.get(AlchemyScene.class, "energy"),  " " , Dungeon.energy);
		if (toolkit != null){
			energyText = LocalizedString.concat(energyText, "+", toolkit.availableEnergy());
		}
	}

	public void showIdentify(Item item){
		if (item.isIdentified()) return;

		NinePatch BG = Chrome.get(Chrome.Type.TOAST);

		IconTitle oldName = new IconTitle(item){
			@Override
			public synchronized void update() {
				super.update();
				alpha(this.alpha()-Game.elapsed);
				if (this.alpha() <= 0){
					killAndErase();
				}
			}
		};
		item.identify();
		IconTitle newName = new IconTitle(item){

			boolean fading;

			@Override
			public synchronized void update() {
				super.update();
				if (!fading) {
					alpha(this.alpha() + Game.elapsed);
					if (this.alpha() >= 1) {
						fading = true;
					}
				} else {
					alpha(this.alpha() - Game.elapsed);
					BG.alpha(this.alpha());
					if (this.alpha() <= 0){
						killAndErase();
						BG.killAndErase();
					}
				}
			}
		};
		identifyEffect = new  IdentifyEffect(oldName, newName);
	}

	//----- active scene management -----
	private static final WeakHashMap<Hero, AlchemyScene> activeAlchemyScenes = new WeakHashMap<>();

	protected void enableAlchemyScene(Hero hero){
		activeAlchemyScenes.put(hero, this);
	}

	protected static void disableAlchemyScene(Hero hero){
		activeAlchemyScenes.remove(hero);
	}

	@Contract(pure = true)
	public static boolean isAlchemySceneEnabled(Hero hero) {
		return activeAlchemyScenes.containsKey(hero);
	}

	@Contract(pure = true)
	public static @Nullable AlchemyScene getActiveAlchemyScene(Hero hero) {
		return activeAlchemyScenes.get(hero);
	}



	//----- helper subclasses -----

	public class InputButton extends Component {

		protected NinePatch bg;
		protected ItemSlot slot;

		private Item item = null;

		@Override
		protected void createChildren() {
			super.createChildren();

			bg = Chrome.get( Chrome.Type.RED_BUTTON);
			add( bg );

			slot = new ItemSlot() {
				@Override
				protected void onPointerDown() {
					bg.brightness( 1.2f );
					Sample.INSTANCE.play( Assets.Sounds.CLICK );
				}
				@Override
				protected void onPointerUp() {
					bg.resetColor();
				}
				@Override
				protected void onClick() {
					super.onClick();
					Item item = InputButton.this.item;
					if (item != null) {
						if (!item.collect(getOwnerHero())) {
							Dungeon.level.drop(item, getOwnerHero().pos);
						}
						InputButton.this.item(null);
						updateState();
					}
					GameScene.show(WndBag.getBag( itemSelector, getOwnerHero() ));
				}

				@Override
				protected boolean onLongClick() {
					Item item = InputButton.this.item;
					if (item != null){
						GameScene.show(new WndInfoItem(getOwnerHero(), item));
						return true;
					}
					return false;
				}

				@Override
				//only the first empty button accepts key input
				public GameAction keyAction() {
					for (InputButton i : inputs){
						if (i.item == null || i.item instanceof WndBag.Placeholder) {
							if (i == InputButton.this) {
								return SPDAction.INVENTORY;
							} else {
								return super.keyAction();
							}
						}
					}
					return super.keyAction();
				}

				@Override
				protected LocalizedString hoverText() {
					if (item == null || item instanceof WndBag.Placeholder){
						return Messages.get(AlchemyScene.class, "add");
					}
					return super.hoverText();
				}

				@Override
				public GameAction secondaryTooltipAction() {
					return SPDAction.INVENTORY_SELECTOR;
				}
			};
			slot.enable(true);
			add( slot );
		}

		@Override
		protected void layout() {
			super.layout();

			bg.x = x;
			bg.y = y;
			bg.size( width, height );

			slot.setRect( x + 2, y + 2, width - 4, height - 4 );
		}

		public Item item(){
			return item;
		}

		public void item( Item item ) {
			if (item == null){
				this.item = null;
				slot.item(new WndBag.Placeholder(ItemSpriteSheet.SOMETHING));
			} else {
				slot.item(this.item = item);
			}
		}
	}

	public class CombineButton extends Component {

		protected int slot;

		protected RedButton button;
		protected RenderedTextBlock costText;
		public int cost;

		private CombineButton(int slot){
			super();

			this.slot = slot;
		}

		@Override
		protected void createChildren() {
			super.createChildren();

			button = new RedButton(""){
				@Override
				protected void onClick() {
					super.onClick();
					combine(slot);
					updateState();
				}

				@Override
				protected LocalizedString hoverText() {
					return Messages.get(AlchemyScene.class, "craft");
				}

				@Override
				public GameAction keyAction() {
					if (slot == 0 && !combines[1].active && !combines[2].active){
						return SPDAction.TAG_LOOT;
					}
					return super.keyAction();
				}
			};
			button.icon(Icons.get(Icons.ARROW));
			add(button);

			costText = PixelScene.renderTextBlock(6);
			add(costText);
		}

		@Override
		protected void layout() {
			super.layout();

			button.setRect(x, y, width(), height());

			costText.setPos(
					left() + (width() - costText.width())/2,
					top() - costText.height()
			);
		}

		public void enable( boolean enabled ){
			enable(enabled, 0);
		}

		public void enable( boolean enabled, int cost ){
			this.cost = cost;
			button.enable(enabled);
			if (enabled) {
				button.icon().tint(1, 1, 0, 1);
				button.alpha(1f);
				costText.hardlight(0x44CCFF);
			} else {
				button.icon().color(0, 0, 0);
				button.alpha(0.6f);
				costText.hardlight(0xFF0000);
			}

			if (cost == 0){
				costText.visible = false;
			} else {
				costText.visible = true;
				costText.text(Messages.get(AlchemyScene.class, "energy") + " " + cost);
			}

			layout();
			active = enabled;
		}

	}

	public class OutputSlot extends Component {

		protected NinePatch bg;
		protected ItemSlot slot;

		@Override
		protected void createChildren() {

			bg = Chrome.get(Chrome.Type.TOAST_TR);
			add(bg);

			slot = new ItemSlot() {
				@Override
				protected void onClick() {
					super.onClick();
					if (visible && item != null && item.trueName() != null){
						GameScene.show(new WndInfoItem(getOwnerHero(), item));
					}
				}
			};
			slot.item(null);
			add( slot );
		}

		@Override
		protected void layout() {
			super.layout();

			bg.x = x;
			bg.y = y;
			bg.size(width(), height());

			slot.setRect(x+2, y+2, width()-4, height()-4);
		}

		public void item( Item item ) {
			slot.item(item);
		}
		public Item item() {
			return slot.item();
		}
	}

	public static class IdentifyEffect {
		public final @NotNull IconTitle oldName;
		public final @NotNull IconTitle newName;

		@Contract(pure = true)
        private IdentifyEffect(@NotNull IconTitle oldName, @NotNull IconTitle newName) {
            this.oldName = oldName;
            this.newName = newName;
        }
    }
}
