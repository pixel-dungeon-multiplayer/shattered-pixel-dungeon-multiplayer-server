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
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Document;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.DeviceCompat;

public class MenuPane extends Component {

	private Image bg;

	private Image depthIcon;
	private BitmapText depthText;
	private Button depthButton;

	private Image challengeIcon;
	private BitmapText challengeText;
	private Button challengeButton;


	private BitmapText version;
	private NinePatch versionOverflowBG;

	private DangerIndicator danger;

	public static final int WIDTH = 31;

	@Override
	protected void createChildren() {
		super.createChildren();

		bg = new Image(Assets.Interfaces.MENU, 1, 0, 31, 21);
		add(bg);

		versionOverflowBG = new NinePatch(bg.texture, 1, 22, 6, 8, 3, 0, 2, 0);
		add(versionOverflowBG);

		version = new BitmapText( "v" + Game.version , PixelScene.pixelFont);
		version.hardlight( 0xCACFC2 );
		add(version);

		depthIcon = Icons.get(Dungeon.level.feeling);
		add(depthIcon);

		depthText = new BitmapText( Integer.toString( Dungeon.depth ), PixelScene.pixelFont);
		depthText.hardlight( 0xCACFC2 );
		depthText.measure();
		add( depthText );

		depthButton = new Button(){
			@Override
			protected LocalizedString hoverText() {
				if (Dungeon.level.feeling != Level.Feeling.NONE){
					return Dungeon.level.feeling.desc();
				} else {
					return null;
				}
			}


		};
		add(depthButton);

		danger = new DangerIndicator();
		add( danger );
	}

	@Override
	protected void layout() {
		super.layout();

		bg.x = x;
		bg.y = y;

		version.scale.set(PixelScene.align(0.5f));
		version.measure();

		float rightMargin = DeviceCompat.isDesktop() ? 1 : 8;
		if (DeviceCompat.isDebug()) rightMargin = 1; //don't care about hiding 'indev'
		float overFlow = version.width()-(bg.width()-4-rightMargin);
		if (overFlow >= 1){
			version.x = x + 2 - overFlow;
			versionOverflowBG.size(overFlow+3, 8);
			versionOverflowBG.x = version.x-3;
			versionOverflowBG.y = y;
		} else {
			version.x = x + 3;
			versionOverflowBG.visible = false;
		}
		version.y = y + 3 - (version.baseLine()*version.scale.y)/2f;
		version.y -= .001f;
		PixelScene.align(version);

		depthIcon.y = y+8;
		PixelScene.align(depthIcon);

		depthText.scale.set(PixelScene.align(0.67f));
		depthText.x = depthIcon.x + (depthIcon.width() - depthText.width())/2f;
		depthText.y = depthIcon.y + depthIcon.height();
		PixelScene.align(depthText);

		depthButton.setRect(depthIcon.x, depthIcon.y, depthIcon.width(), depthIcon.height() + depthText.height());

		if (challengeIcon != null){
			challengeIcon.y = depthIcon.y;
			PixelScene.align(challengeIcon);

			challengeText.scale.set(PixelScene.align(0.67f));
			challengeText.x = challengeIcon.x + (challengeIcon.width() - challengeText.width())/2f;
			challengeText.y = challengeIcon.y + challengeIcon.height();
			PixelScene.align(challengeText);

			challengeButton.setRect(challengeIcon.x, challengeIcon.y, challengeIcon.width(), challengeIcon.height() + challengeText.height());
		}

		danger.setPos( x + WIDTH - danger.width(), y + bg.height + 1 );
		danger.setSize( camera.width - danger.width(), danger.height());
	}

	public void flashForPage( Document doc, String page ){
	}

	public void updateKeys(){
	}

}
