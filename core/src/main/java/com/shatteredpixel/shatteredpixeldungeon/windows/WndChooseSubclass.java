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

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.items.TengusMask;
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
import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WndChooseSubclass extends Window {

	private static final int WIDTH		= 130;
	private static final float GAP		= 2;
	public final @NotNull TengusMask tome;
	public final IconTitle titlebar;
	public final RenderedTextBlock message;
	public final List<RedButton> subclassButtons = new ArrayList<>();
	public final List<IconButton> subclassInfoButtons = new ArrayList<>();
	public final IconButton randomButton;
	public final RedButton cancelButton;

	public WndChooseSubclass(final @NotNull TengusMask tome, final Hero hero ) {
		super(hero);
		this.tome = tome;

		titlebar = new IconTitle();
		titlebar.icon( new ItemSprite( tome.image(), null ) );
		titlebar.label( tome.name() );
		titlebar.setRect( 0, 0, WIDTH, 0 );
		add( titlebar );

		randomButton = new IconButton(Icons.SHUFFLE) {
			@Override
			protected void onClick() {
				super.onClick();
				showRandomConfirmation();
			}

			@Override
			public void update() {
				if (Statistics.qualifiedForRandomVictoryBadge) {
					image.tint(1, 1, 1, (float)Math.abs(Math.cos(1.5f * Math.PI * Game.timeTotal) / 2f));
				}
				super.update();
			}

			@Override
			protected LocalizedString hoverText() {
				return Messages.get(WndChooseSubclass.class, "random_title");
			}
		};
		randomButton.setRect(WIDTH - 16, 0, 16, 16);
		add(randomButton);

		message = PixelScene.renderTextBlock( 6 );
		message.text( Messages.get(this, "message"), WIDTH );
		message.setPos( titlebar.left(), titlebar.bottom() + GAP );
		add( message );

		float pos = message.bottom() + 3*GAP;

		for (HeroSubClass subCls : hero.heroClass.subClasses()){
			RedButton btnCls = new RedButton( subCls.shortDesc(), 6 ) {
				@Override
				protected void onClick() {
					GameScene.show(new WndOptions(hero, new HeroIcon(subCls),
							Messages.titleCase(subCls.title()),
							Messages.get(WndChooseSubclass.this, "are_you_sure"),
							Messages.get(WndChooseSubclass.this, "yes"),
							Messages.get(WndChooseSubclass.this, "no")){
						@Override
						protected void onSelect(int index) {
							hide();
							if (index == 0){
								if(WndChooseSubclass.this.parent != null) {
									WndChooseSubclass.this.hide();
								}
								tome.choose( subCls );
								hide();
								WndChooseSubclass.this.hide();
							}
						}
					});
				}
			};
			btnCls.leftJustify = true;
			btnCls.multiline = true;
			btnCls.setSize(WIDTH-20, btnCls.reqHeight()+2);
			btnCls.setRect( 0, pos, WIDTH-20, btnCls.reqHeight()+2);
			subclassButtons.add(btnCls);
			add( btnCls );

			IconButton clsInfo = new IconButton(Icons.INFO){
				@Override
				protected void onClick() {
					GameScene.show(new WndInfoSubclass(getOwnerHero().heroClass, subCls, getOwnerHero()));
				}
			};
			clsInfo.setRect(WIDTH-20, btnCls.top() + (btnCls.height()-20)/2, 20, 20);
			subclassInfoButtons.add(clsInfo);
			add(clsInfo);

			pos = btnCls.bottom() + GAP;
		}

		cancelButton = new RedButton( Messages.get(this, "cancel") ) {
			@Override
			protected void onClick() {
				hide();
			}
		};
		cancelButton.setRect( 0, pos, WIDTH, 18 );
		add( cancelButton );

		resize( WIDTH, (int)cancelButton.bottom() );
	}

	@Override
	protected void onSelect(int button) {
		if (button >= 0 && button < subclassButtons.size()) {
			subclassButtons.get(button).onClickNetwork();
		} else if (button == subclassButtons.size()) {
			cancelButton.onClickNetwork();
		} else if (button == subclassButtons.size() + 1) {
			randomButton.onClickNetwork();
		}
	}

	@Override
	public void onSelect(int button, @Nullable JSONObject args) {
		if (args != null && args.optBoolean("info", false)
				&& button >= 0 && button < subclassInfoButtons.size()) {
			subclassInfoButtons.get(button).onClickNetwork();
		} else {
			onSelect(button);
		}
	}

	private void showRandomConfirmation() {
		Hero hero = getOwnerHero();
		GameScene.show(new WndOptions(hero, Icons.SHUFFLE.get(),
				Messages.get(WndChooseSubclass.class, "random_title"),
				Messages.get(WndChooseSubclass.class, "random_sure"),
				Messages.get(WndChooseSubclass.class, "yes"),
				Messages.get(WndChooseSubclass.class, "no")) {
			@Override
			protected void onSelect(int index) {
				super.onSelect(index);
				if (index == 0) {
					WndChooseSubclass.this.hide();
					HeroSubClass subClass = Random.oneOf(hero.heroClass.subClasses());
					tome.choose(subClass);
					GameScene.show(new WndInfoSubclass(hero.heroClass, subClass, hero));
				}
			}
		});
	}
}
