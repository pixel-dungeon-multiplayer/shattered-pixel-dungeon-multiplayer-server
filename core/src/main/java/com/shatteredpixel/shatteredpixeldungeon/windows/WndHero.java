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
import com.shatteredpixel.shatteredpixeldungeon.SPDAction;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.StatusPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentsPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;
import org.json.JSONObject;
import org.jetbrains.annotations.Nullable;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;

import java.util.ArrayList;
import java.util.Locale;

public class WndHero extends WndTabbed {

	public static class Stat {
		public final LocalizedString label;
		public final String value;
		public Stat(LocalizedString label, String value) {
			this.label = label;
			this.value = value;
		}
	}
	
	private static final int WIDTH		= 120;
	private static final int HEIGHT		= 120;
	
	private StatsTab stats;
	private TalentsTab talents;
	private BuffsTab buffs;

	public static int lastIdx = 0;

	public WndHero(Hero hero) {
		
		super(hero);
		
		resize( WIDTH, HEIGHT );
		
		stats = new StatsTab();
		add( stats );

		talents = new TalentsTab();
		add(talents);
		talents.setRect(0, 0, WIDTH, HEIGHT);

		buffs = new BuffsTab();
		add( buffs );
		buffs.setRect(0, 0, WIDTH, HEIGHT);
		buffs.setupList();
		
		add( new IconTab( Icons.get(Icons.RANKINGS) ) {
			protected void select( boolean value ) {
				super.select( value );
				if (selected) {
					lastIdx = 0;
					if (!stats.visible) {
						stats.initialize();
					}
				}
				stats.visible = stats.active = selected;
			}
		} );
		add( new IconTab( Icons.get(Icons.TALENT) ) {
			protected void select( boolean value ) {
				super.select( value );
				if (selected) lastIdx = 1;
				if (selected) StatusPane.talentBlink = 0;
				talents.visible = talents.active = selected;
			}
		} );
		add( new IconTab( Icons.get(Icons.BUFFS) ) {
			protected void select( boolean value ) {
				super.select( value );
				if (selected) lastIdx = 2;
				buffs.visible = buffs.active = selected;
			}
		} );

		layoutTabs();

		talents.setRect(0, 0, WIDTH, HEIGHT);
		talents.pane.scrollTo(0, talents.pane.content().height() - talents.pane.height());
		talents.layout();

		select( lastIdx );
	}

	@Override
	public boolean onSignal(KeyEvent event) {
		if (event.pressed && KeyBindings.getActionForKey( event ) == SPDAction.HERO_INFO) {
			onBackPressed();
			return true;
		} else {
			return super.onSignal(event);
		}
	}

	@Override
	public void offset(int xOffset, int yOffset) {
		super.offset(xOffset, yOffset);
		talents.layout();
		buffs.layout();
	}

	public ArrayList<Stat> stats() {
		ArrayList<Stat> list = new ArrayList<>();
		Hero hero = getOwnerHero();
		int strBonus = hero.STR() - hero.getSTR();
		if (strBonus > 0)           list.add(new Stat(Messages.get(StatsTab.class, "str"), hero.getSTR() + " + " + strBonus));
		else if (strBonus < 0)      list.add(new Stat(Messages.get(StatsTab.class, "str"), hero.getSTR() + " - " + -strBonus));
		else                        list.add(new Stat(Messages.get(StatsTab.class, "str"), String.valueOf(hero.STR())));

		if (hero.shielding() > 0)   list.add(new Stat(Messages.get(StatsTab.class, "health"), hero.getHP() + "+" + hero.shielding() + "/" + hero.getHT()));
		else                        list.add(new Stat(Messages.get(StatsTab.class, "health"), hero.getHP() + "/" + hero.getHT()));

		list.add(new Stat(Messages.get(StatsTab.class, "exp"), hero.exp + "/" + hero.maxExp()));
		list.add(null); //gap
		list.add(new Stat(Messages.get(StatsTab.class, "gold"), String.valueOf(Statistics.goldCollected)));
		list.add(new Stat(Messages.get(StatsTab.class, "depth"), String.valueOf(Statistics.deepestFloor)));

		if (Dungeon.daily){
			if (!Dungeon.dailyReplay) {
				list.add(new Stat(Messages.get(StatsTab.class, "daily_for"), "_" + Dungeon.customSeedText + "_"));
			} else {
				list.add(new Stat(Messages.get(StatsTab.class, "replay_for"), "_" + Dungeon.customSeedText + "_"));
			}
		} else if (!Dungeon.customSeedText.isEmpty()){
			list.add(new Stat(Messages.get(StatsTab.class, "custom_seed"), "_" + Dungeon.customSeedText + "_"));
		} else {
			list.add(new Stat(Messages.get(StatsTab.class, "dungeon_seed"), DungeonSeed.convertToCode(Dungeon.seed)));
		}
		return list;
	}

	public LocalizedString title() {
		Hero hero = getOwnerHero();
		if (hero.name().equals(hero.className())) {
			return Messages.get(StatsTab.class, "title", hero.lvl, hero.className()).toUpperCase(Locale.ENGLISH);
		} else {
			return LocalizedString.concat(hero.name(), "\n", Messages.get(StatsTab.class, "title", hero.lvl, hero.className())).toUpperCase(Locale.ENGLISH);
		}
	}

	@Override
	public void onSelect(int button, @Nullable JSONObject args) {
		if (args != null && args.has("action")) {
			String action = args.getString("action");
			if ("tab".equals(action)) {
				int tabIdx = args.getInt("index");
				select(tabIdx);
			} else if ("info".equals(action)) {
				Hero hero = getOwnerHero();
				if (ShatteredPixelDungeon.scene() instanceof GameScene){
					GameScene.show(new WndHeroInfo(hero.heroClass, getOwnerHero()));
				} else {
					ShatteredPixelDungeon.scene().addToFront(new WndHeroInfo(hero.heroClass, getOwnerHero()));
				}
			} else if ("click_buff".equals(action)) {
				String buffClassName = args.getString("class");
				Hero hero = getOwnerHero();
				Buff foundBuff = null;
				for (Buff buff : hero.buffs()) {
					if (buff.getClass().getName().equals(buffClassName)) {
						foundBuff = buff;
						break;
					}
				}
				if (foundBuff != null) {
					if (ShatteredPixelDungeon.scene() instanceof GameScene){
						GameScene.show(new WndInfoBuff(hero, foundBuff));
					} else {
						ShatteredPixelDungeon.scene().addToFront(new WndInfoBuff(hero, foundBuff));
					}
				}
			} else if ("click_talent".equals(action)) {
				String talentId = args.getString("id");
				Hero hero = getOwnerHero();
				Talent talent = null;
				int pointsInTalent = 0;
				int tier = 0;
				for (int i = 0; i < hero.talents.size(); i++) {
					for (Talent t : hero.talents.get(i).keySet()) {
						if (t.name().equals(talentId)) {
							talent = t;
							pointsInTalent = hero.talents.get(i).get(t);
							tier = i + 1;
							break;
						}
					}
					if (talent != null) break;
				}
				if (talent != null && hero.isAlive()) {
					final Talent fTalent = talent;
					if (hero.talentPointsAvailable(tier) > 0 && pointsInTalent < talent.maxPoints()) {
						WndInfoTalent toAdd = new WndInfoTalent(hero, talent, pointsInTalent, new WndInfoTalent.TalentButtonCallback() {
							@Override
							public LocalizedString prompt() {
								return Messages.titleCase(Messages.get(WndInfoTalent.class, "upgrade"));
							}
							@Override
							public void call() {
								TalentButton.upgradeTalent(getOwnerHero(), fTalent);
								Statistics.qualifiedForRandomVictoryBadge = false;
							}
						});
						ShatteredPixelDungeon.scene().addToFront(toAdd);
					} else {
						WndInfoTalent toAdd = new WndInfoTalent(hero, talent, pointsInTalent, null);
						ShatteredPixelDungeon.scene().addToFront(toAdd);
					}
				}
			}
		} else {
			super.onSelect(button, args);
		}
	}

	private class StatsTab extends Group {
		
		private static final int GAP = 6;
		
		private float pos;
		
		public StatsTab() {
			initialize();
		}

		public void initialize(){

			for (Gizmo g : members){
				if (g != null) g.destroy();
			}
			clear();
			
			Hero hero = getOwnerHero();

			IconTitle title = new IconTitle();
			title.icon( HeroSprite.avatar(hero) );
			if (hero.name().equals(hero.className()))
				title.label( Messages.get(this, "title", hero.lvl, hero.className() ).toUpperCase( Locale.ENGLISH ) );
			else
				title.label((LocalizedString.concat(hero.name(), "\n", Messages.get(this, "title", hero.lvl, hero.className()))).toUpperCase(Locale.ENGLISH));
			title.color(Window.TITLE_COLOR);
			title.setRect( 0, 0, WIDTH-16, 0 );
			add(title);

			IconButton infoButton = new IconButton(Icons.get(Icons.INFO)){
				@Override
				protected void onClick() {
					super.onClick();
					if (ShatteredPixelDungeon.scene() instanceof GameScene){
						GameScene.show(new WndHeroInfo(hero.heroClass, getOwnerHero()));
					} else {
						ShatteredPixelDungeon.scene().addToFront(new WndHeroInfo(hero.heroClass, getOwnerHero()));
					}
				}

				@Override
				protected LocalizedString hoverText() {
					return Messages.titleCase(Messages.get(WndKeyBindings.class, "hero_info"));
				}

			};
			infoButton.setRect(title.right(), 0, 16, 16);
			add(infoButton);

			pos = title.bottom() + 2*GAP;

			for (Stat stat : stats()) {
				if (stat != null) {
					statSlot(stat.label, stat.value);
				} else {
					pos += GAP;
				}
			}
		}

		private void statSlot( LocalizedString label, String value ) {
			statSlot(label, LocalizedString.raw(value));
		}

		private void statSlot( String label, LocalizedString value ) {
			statSlot(LocalizedString.raw(label), value);
		}
		private void statSlot( LocalizedString label, LocalizedString value ) {

			int size = 8;
			RenderedTextBlock txt;
			do {
				txt = PixelScene.renderTextBlock( label, size );
				size--;
			} while (txt.width() >= WIDTH * 0.55f);
			txt.setPos(0, pos + (6 - txt.height())/2);
			PixelScene.align(txt);
			add( txt );

			size = 8;
			do {
				txt = PixelScene.renderTextBlock( value, size );
				size--;
			} while (txt.width() >= WIDTH * 0.45f);
			txt.setPos(WIDTH * 0.55f, pos + (6 - txt.height())/2);
			PixelScene.align(txt);
			add( txt );
			
			pos += GAP + txt.height();
		}

		private void statSlot( LocalizedString label, int value ) {
			statSlot( label, Integer.toString(value) );
		}

		private void statSlot( String label, int value ) {
			statSlot( LocalizedString.raw(label), Integer.toString( value ) );
		}
		
		public float height() {
			return pos;
		}
	}

	public class TalentsTab extends Component {

		TalentsPane pane;

		@Override
		protected void createChildren() {
			super.createChildren();
			pane = new TalentsPane(TalentButton.Mode.UPGRADE, getOwnerHero());
			add(pane);
		}

		@Override
		protected void layout() {
			super.layout();
			pane.setRect(x, y, width, height);
		}

	}
	
	private class BuffsTab extends Component {
		
		private static final int GAP = 2;
		
		private float pos;
		private ScrollPane buffList;
		private ArrayList<BuffSlot> slots = new ArrayList<>();

		@Override
		protected void createChildren() {

			super.createChildren();

			buffList = new ScrollPane( new Component() ){
				@Override
				public void onClick( float x, float y ) {
					int size = slots.size();
					for (int i=0; i < size; i++) {
						if (slots.get( i ).onClick( x, y )) {
							break;
						}
					}
				}
			};
			add(buffList);
		}
		
		@Override
		protected void layout() {
			super.layout();
			buffList.setRect(0, 0, width, height);
		}
		
		private void setupList() {
			Component content = buffList.content();
			for (Buff buff : getOwnerHero().buffs()) {
				if (buff.icon() != BuffIndicator.NONE) {
					BuffSlot slot = new BuffSlot(buff);
					slot.setRect(0, pos, WIDTH, slot.icon.height());
					content.add(slot);
					slots.add(slot);
					pos += GAP + slot.height();
				}
			}
			content.setSize(buffList.width(), pos);
			buffList.setSize(buffList.width(), buffList.height());
		}

		private class BuffSlot extends Component {

			private Buff buff;

			Image icon;
			RenderedTextBlock txt;

			public BuffSlot( Buff buff ){
				super();
				this.buff = buff;

				icon = new BuffIcon(buff, true);
				icon.y = this.y;
				add( icon );

				txt = PixelScene.renderTextBlock( Messages.titleCase(buff.name()), 8 );
				txt.setPos(
						icon.width + GAP,
						this.y + (icon.height - txt.height()) / 2
				);
				PixelScene.align(txt);
				add( txt );

			}

			@Override
			protected void layout() {
				super.layout();
				icon.y = this.y;
				txt.maxWidth((int)(width - icon.width()));
				txt.setPos(
						icon.width + GAP,
						this.y + (icon.height - txt.height()) / 2
				);
				PixelScene.align(txt);
			}
			
			protected boolean onClick ( float x, float y ) {
				if (inside( x, y )) {
					GameScene.show(new WndInfoBuff(getOwnerHero(), buff));
					return true;
				} else {
					return false;
				}
			}
		}
	}
}
