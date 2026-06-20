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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.Image;
import com.nikita22007.multiplayer.noosa.audio.Sample;
import org.jetbrains.annotations.Nullable;

public class IconButton extends Button {

	@Nullable protected Icons icon;
	@Nullable protected Image image;
	
	public IconButton(){
		super();
	}
	
	public IconButton( @Nullable Icons icon ){
		super();
		icon( icon );
	}

	public IconButton( @Nullable Image image ){
		super();
		icon( image );
	}
	
	@Override
	protected void layout() {
		super.layout();
		
		if (image != null) {
			image.x = x + (width - image.width()) / 2f;
			image.y = y + (height - image.height()) / 2f;
			PixelScene.align(image);
		}
	}
	
	@Override
	protected void onPointerDown() {
		if (image != null) image.brightness( 1.5f );
		Sample.INSTANCE.play( Assets.Sounds.CLICK );
	}
	
	@Override
	protected void onPointerUp() {
		if (image != null) image.resetColor();
	}
	
	public void enable( boolean value ) {
		active = value;
		if (image != null) image.alpha( value ? 1.0f : 0.3f );
	}
	
	public void icon( @Nullable Icons icon ) {
		if (this.image != null) {
			remove( this.image);
		}
		this.icon = icon;
		this.image = icon == null? null: icon.get();
		if (this.image != null) {
			add( this.image);
			layout();
		}
	}

	public void icon( @Nullable Image image ) {
		if (this.image != null) {
			remove( this.image);
		}
		this.icon = null;
		this.image = image;
		if (this.image != null) {
			add( this.image);
			layout();
		}
	}
	
	public Image icon(){
		return image;
	}

	public @Nullable Icons iconType() {
		return icon;
	}
}
