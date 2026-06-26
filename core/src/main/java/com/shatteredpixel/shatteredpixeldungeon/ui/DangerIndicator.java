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

import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.SPDAction;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndKeyBindings;
import com.watabou.input.GameAction;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Image;

public class DangerIndicator extends Tag {
	
	public static final int COLOR	= 0xC03838;
	
	private BitmapText number;
	private Image icon;
	
	private int enemyIndex = 0;
	
	private int lastNumber = -1;

	public static int HEIGHT = 16;
	
	public DangerIndicator() {
		super( COLOR );
		
		setSize( SIZE, HEIGHT );
		
		visible = false;
	}
	
	@Override
	public GameAction keyAction() {
		return SPDAction.CYCLE;
	}
	
	@Override
	protected void createChildren() {
		super.createChildren();
		
		number = new BitmapText( PixelScene.pixelFont);
		add( number );
		
		icon = Icons.SKULL.get();
		add( icon );
	}
	
	@Override
	protected void layout() {
		super.layout();
		
		icon.x = left() + 14;
		icon.y = y + (height - icon.height) / 2;
		
		placeNumber();
	}
	
	private void placeNumber() {
		number.x = left() + 13 - number.width();
		number.y = y + (height - number.baseLine()) / 2f;
		PixelScene.align(number);
	}
	
	@Override
	public void update() {
//		//FIXME
//		if (Dungeon.heroes.isAlive()) {
//			int v =  Dungeon.heroes.visibleEnemies();
//			if (v != lastNumber) {
//				lastNumber = v;
//				if (visible = lastNumber > 0) {
//					number.text( Integer.toString( lastNumber ) );
//					number.measure();
//					placeNumber();
//
//					flash();
//				}
//			}
//		} else {
//			visible = false;
//		}
//
		super.update();
	}
	
	@Override
	protected void onClick() {
//		super.onClick();
//		if (Dungeon.heroes.visibleEnemies() > 0) {
//
//			Mob target = Dungeon.heroes.visibleEnemy(++enemyIndex);
//
//			QuickSlotButton.target(target);
//			if (Dungeon.heroes.canAttack(target)) AttackIndicator.target(target);
//
//			if (Dungeon.heroes.curAction == null && target.sprite != null) {
//				Camera.main.panFollow(target.sprite, 5f);
//			}
//		}
	}

	@Override
	protected LocalizedString hoverText() {
		return Messages.titleCase(Messages.get(WndKeyBindings.class, "tag_danger"));
	}
}
