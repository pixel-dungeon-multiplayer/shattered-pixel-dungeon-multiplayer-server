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

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.nikita22007.multiplayer.utils.text.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.*;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.Game;
import com.watabou.utils.Reflection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

//this is contained in its own class as custom notes have a lot of messy window UI logic
//TODO: check this
public class CustomNoteButton extends IconButton {

	@NotNull
	final Hero hero;
	public CustomNoteButton (@NotNull Hero hero) {
		super(Icons.PLUS.get());
		this.hero = hero;

		width = 11;
		height = 11;
	}

	@Override
	protected void onClick() {
		super.onClick();

		if (Notes.getRecords(Notes.CustomRecord.class).size() >= Notes.customRecordLimit()){
			GameScene.show(new WndTitledMessage(hero, Icons.INFO.get(),
					Messages.get(this, "limit_title"),
					Messages.get(this, "limit_text")));
			return;
		}

		GameScene.show(new WndOptions(hero, Icons.SCROLL_COLOR.get(),
				Messages.get(CustomNoteButton.class, "title"),
				Messages.get(CustomNoteButton.class, "desc"),
				Messages.get(CustomNoteButton.class, "new_text"),
				Messages.get(CustomNoteButton.class, "new_floor"),
				Messages.get(CustomNoteButton.class, "new_inv"),
				Messages.get(CustomNoteButton.class, "new_type")){
			@Override
			protected void onSelect(int index) {
				if (index == 0){
					Notes.CustomRecord custom = new Notes.CustomRecord("", "");
					addNote(hero, custom,
							Messages.get(CustomNoteButton.class, "new_text"),
							Messages.get(CustomNoteButton.class, "new_text_title"));
				} else if (index == 1){
					GameScene.show(new WndDepthSelect());
				} else if (index == 2){
					GameScene.selectItem(itemSelector, getOwnerHero());
				} else {
					GameScene.show(new WndItemtypeSelect(hero));
				}
			}

			@Override
			public void hide() {
				//do nothing, prevents window closing when user steps back in note creation process
			}

			@Override
			public void onBackPressed() {
				super.hide(); //actually hide in this case
			}
		});
	}

	@Override
	protected LocalizedString hoverText() {
		return Messages.get(this, "title");
	}

	private class WndDepthSelect extends WndTitledMessage {

		public WndDepthSelect(){
			super(hero, Icons.STAIRS.get(),
					Messages.get(CustomNoteButton.class, "new_floor"),
					Messages.get(CustomNoteButton.class, "new_floor_prompt"));

			int top = height+2;
			int left = 0;

			for (int i = Statistics.deepestFloor; i > 0; i --){
				if (i % 5 == 0 && left > 0){
					left = 0;
					top += 17;
				}
				int finalI = i;
				RedButton btnDepth = new RedButton(Integer.toString(finalI)){
					@Override
					protected void onClick() {
						addNote(hero, new Notes.CustomRecord(finalI, "", ""),
								Messages.get(CustomNoteButton.class, "new_floor"),
								Messages.get(CustomNoteButton.class, "new_floor_title", finalI));
					}
				};
				btnDepth.setRect(left, top, 23, 16);
				left += 24;
				add(btnDepth);
			}

			resize(width, top + (left == 0 ? 0 : 16));

		}

	}

	private WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {

		@Override
		public LocalizedString textPrompt() {
			return	Messages.get(CustomNoteButton.class, "new_inv_prompt");
		}

		@Override
		public boolean hideAfterSelecting() {
			return false;
		}

		@Override
		public boolean itemSelectable(Item item) {
			if (item instanceof EquipableItem){
				if (item instanceof Ring && Notes.findCustomRecord(item.getClass()) != null){
					return false;
				}
				return item.customNoteID == -1
						|| Notes.findCustomRecord(item.customNoteID) == null;
			} else {
				return Notes.findCustomRecord(item.getClass()) == null;
			}
		}

		@Override
		public void onSelect( Item item ) {
			if (item != null){
				Notes.CustomRecord custom;
				if (item instanceof EquipableItem || item instanceof Wand || item instanceof Trinket) {
					custom = new Notes.CustomRecord(item, "", "");
					custom.assignID();
					item.customNoteID = custom.ID();
				} else {
					custom = new Notes.CustomRecord(item.getClass(), "", "");
					custom.assignID();
				}

				addNote(hero, custom,
						Messages.get(CustomNoteButton.class, "new_inv"),
						Messages.get(CustomNoteButton.class, "new_item_title", Messages.titleCase(item.name())));
			}
		}
	};

	private static class WndItemtypeSelect extends WndTitledMessage {

		public WndItemtypeSelect(@NotNull Hero hero) {
			super(hero, Icons.SCROLL_COLOR.get(),
					Messages.get(CustomNoteButton.class, "new_type"),
					Messages.get(CustomNoteButton.class, "new_type_prompt"));

			int top = height + 2;
			int left = 0;

			ArrayList<Item> items = new ArrayList<>();
			for (Class<?> potionCls : Generator.Category.POTION.classes) {
				items.add((Item) Reflection.newInstance(potionCls));
			}
			for (Class<?> potionCls : Generator.Category.SCROLL.classes) {
				items.add((Item) Reflection.newInstance(potionCls));
			}
			for (Class<?> potionCls : Generator.Category.RING.classes) {
				items.add((Item) Reflection.newInstance(potionCls));
			}
			Collections.sort(items, itemVisualcomparator);
			for (Item item : items) {
				ItemButton itemButton = new ItemButton(){
					@Override
					protected void onClick() {
						addNote(hero, new Notes.CustomRecord(item.getClass(), "", ""),
								Messages.get(CustomNoteButton.class, "new_type"),
								Messages.get(CustomNoteButton.class, "new_item_title", Messages.titleCase(item.name())));
					}
				};
				itemButton.item(item);
				itemButton.setRect(left, top, 19, 19);
				add(itemButton);

				if (Notes.findCustomRecord(item.getClass()) != null){
					//TODO: check this
					//itemButton.slot.enable(false);
				}

				left += 20;
				if (left >= width - 19){
					top += 20;
					left = 0;
				}
			}
			if (left > 0){
				top += 20;
				left = 0;
			}

			resize(width, top);
		}
	}

	//items are sorted first sorted potions -> scrolls -> rings, and then based on their sprites.
	private static Comparator<Item> itemVisualcomparator = new Comparator<Item>() {
		@Override
		public int compare(Item i1, Item i2) {
			int i1Idx = i1.image();
			int i2Idx = i2.image();

			if (i1 instanceof Scroll)   i1Idx += 1000;
			if (i1 instanceof Ring)     i1Idx += 2000;

			if (i2 instanceof Scroll)   i2Idx += 1000;
			if (i2 instanceof Ring)     i2Idx += 2000;

			return i1Idx - i2Idx;
		}
	};

	public static class CustomNoteWindow extends WndJournalItem {

		public CustomNoteWindow(@NotNull Hero hero, @NotNull Notes.CustomRecord rec, Window parentWindow) {
			super(hero, rec.icon(), rec.title(), rec.desc());

			RedButton title = new RedButton( Messages.get(CustomNoteWindow.class, "edit_title") ){
				@Override
				protected void onClick() {
					GameScene.show(new WndTextInput(hero, Messages.get(CustomNoteWindow.class, "edit_title"),
							LocalizedString.EMPTY,
							rec.title(),
							50,
							false,
							Messages.get(CustomNoteWindow.class, "confirm"),
							Messages.get(CustomNoteWindow.class, "cancel")){
						@Override
						public void onSelect(boolean positive, String text) {
							if (positive && !text.isEmpty()){
								rec.editText(text, rec.desc().toString());
								CustomNoteWindow.this.hide();
								if (parentWindow instanceof WndUseItem){
									WndUseItem newParent = new WndUseItem(getOwnerHero(), ((WndUseItem) parentWindow).owner, ((WndUseItem) parentWindow).item);
									GameScene.show(newParent);
									GameScene.show(new CustomNoteWindow(hero, rec, newParent));
								} else {
									GameScene.show(new CustomNoteWindow(hero, rec, parentWindow));
								}
							}
						}
					});
				}
			};
			add(title);
			title.setRect(0, Math.min(height+2, PixelScene.uiCamera.height-50), width/2-1, 16);

			LocalizedString editBodyText = rec.desc().isEmpty() ? Messages.get(CustomNoteWindow.class, "add_text") : Messages.get(CustomNoteWindow.class, "edit_text");
			RedButton body = new RedButton(editBodyText){
				@Override
				protected void onClick() {
					GameScene.show(new WndTextInput(hero, editBodyText,
							LocalizedString.EMPTY,
							rec.desc(),
							500,
							true,
							Messages.get(CustomNoteWindow.class, "confirm"),
							Messages.get(CustomNoteWindow.class, "cancel")){
						@Override
						public void onSelect(boolean positive, String text) {
							if (positive){
								rec.editText(rec.title().toString(), text);
								CustomNoteWindow.this.hide();
								GameScene.show(new CustomNoteWindow(hero, rec, parentWindow));
							}
						}
					});
				}
			};
			add(body);
			body.setRect(title.right()+2, title.top(), width/2-1, 16);

			RedButton delete = new RedButton( Messages.get(CustomNoteWindow.class, "delete") ){
				@Override
				protected void onClick() {
					GameScene.show(new WndOptions(hero, Icons.WARNING.get(),
							Messages.get(CustomNoteWindow.class, "delete"),
							Messages.get(CustomNoteWindow.class, "delete_warn"),
							Messages.get(CustomNoteWindow.class, "confirm"),
							Messages.get(CustomNoteWindow.class, "cancel")){
						@Override
						protected void onSelect(int index) {
							if (index == 0){
								Notes.remove(rec);
								CustomNoteWindow.this.hide();
								if ( parentWindow == null){
								} else if (parentWindow instanceof WndUseItem){
									GameScene.show(new WndUseItem(getOwnerHero(), ((WndUseItem) parentWindow).owner, ((WndUseItem) parentWindow).item));
								}
							}
						}
					});
				}
			};
			add(delete);
			delete.setRect(0, title.bottom()+1, width, 16);

			resize(width, (int)delete.bottom());
		}

		@Override
		protected boolean useHighlighting() {
			return false;
		}
	}

	private static void addNote(Hero hero, Notes.CustomRecord note, LocalizedString promptTitle, LocalizedString prompttext){
		GameScene.show(new WndTextInput(hero, promptTitle,
				prompttext,
				LocalizedString.EMPTY,
				50,
				false,
				Messages.get(CustomNoteWindow.class, "confirm"),
				Messages.get(CustomNoteWindow.class, "cancel")){
			@Override
			public void onSelect(boolean positive, String text) {
				if (positive && !text.isEmpty()){
					Notes.add(note);
					note.editText(text, "");
					refreshScene(null);
				}
			}
		});
	}

	private static void refreshScene(Notes.CustomRecord recToShow){
		if (recToShow == null){
			ShatteredPixelDungeon.seamlessResetScene();
		} else {
			ShatteredPixelDungeon.seamlessResetScene(new Game.SceneChangeCallback() {
				@Override
				public void beforeCreate() {
                    //WndJournal wnd = new WndJournal();
                    //ShatteredPixelDungeon.scene().addToFront(wnd);
                    //ShatteredPixelDungeon.scene().addToFront(new CustomNoteWindow(note, wnd));

                }
                @Override
                public void afterCreate() {

                }}
					);
				}
			};
		}


