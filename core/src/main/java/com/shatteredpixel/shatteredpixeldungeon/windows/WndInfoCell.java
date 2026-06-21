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
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTerrainTilemap;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;

public class WndInfoCell extends Window {

	
	private static final float GAP	= 2;
	
	private static final int WIDTH = 120;
	//used for toJson
	private LocalizedString desc;
	private IconTitle titlebar;

	public static Image cellImage( int cell ){
		int tile = Dungeon.level.map[cell];
		if (Dungeon.level.water[cell]) {
			tile = Terrain.WATER;
		} else if (Dungeon.level.pit[cell]) {
			tile = Terrain.CHASM;
		}

		Image customImage = null;
		int x = cell % Dungeon.level.width();
		int y = cell / Dungeon.level.width();
		int tilemapIndex = -1;
		CustomTilemap matchingTilemap = null;
		for (int idx = 0; idx < Dungeon.level.customTiles.size(); idx++){
			CustomTilemap i = Dungeon.level.customTiles.get(idx);
			if ((x >= i.tileX && x < i.tileX+i.tileW) &&
					(y >= i.tileY && y < i.tileY+i.tileH)){
				if ((customImage = i.image(x - i.tileX, y - i.tileY)) != null) {
					tilemapIndex = idx;
					matchingTilemap = i;
					break;
				}
			}
		}

		if (customImage != null){
			return new CustomTilemap.CustomTileImage(customImage, tilemapIndex, x - matchingTilemap.tileX, y - matchingTilemap.tileY);
		} else {

			if (tile == Terrain.WATER) {
				Image water = new Image(Dungeon.level.waterTex());
				water.frame(0, 0, DungeonTilemap.SIZE, DungeonTilemap.SIZE);
				return water;
			} else {
				return DungeonTerrainTilemap.tile(cell, tile);
			}
		}
	}

	public static LocalizedString cellName(int cell ){

		CustomTilemap customTile = null;
		int x = cell % Dungeon.level.width();
		int y = cell / Dungeon.level.width();
		for (CustomTilemap i : Dungeon.level.customTiles){
			if ((x >= i.tileX && x < i.tileX+i.tileW) &&
					(y >= i.tileY && y < i.tileY+i.tileH)){
				if (i.image(x - i.tileX, y - i.tileY) != null) {
					x -= i.tileX;
					y -= i.tileY;
					customTile = i;
					break;
				}
			}
		}

		if (customTile != null && customTile.name(x, y) != null){
			return customTile.name(x, y);
		} else {
			return Dungeon.level.tileName(Dungeon.level.map[cell]);
		}
	}

	public WndInfoCell(int cell, Hero hero) {

		super(hero);

		CustomTilemap customTile = null;
		int x = cell % Dungeon.level.width();
		int y = cell / Dungeon.level.width();
		for (CustomTilemap i : Dungeon.level.customTiles){
			if ((x >= i.tileX && x < i.tileX+i.tileW) &&
					(y >= i.tileY && y < i.tileY+i.tileH)){
				if (i.image(x - i.tileX, y - i.tileY) != null) {
					x -= i.tileX;
					y -= i.tileY;
					customTile = i;
					break;
				}
			}
		}


		LocalizedString desc = LocalizedString.EMPTY;

		IconTitle titlebar = new IconTitle();
		titlebar.icon(cellImage(cell));
		titlebar.label(cellName(cell));

		if (customTile != null){
			LocalizedString customDesc = customTile.desc(x, y);
			if (customDesc != null) {
				desc = LocalizedString.concat(desc, customDesc);
			} else {
				desc = LocalizedString.concat(desc, Dungeon.level.tileDesc(Dungeon.level.map[cell]));
			}

		} else {

			desc = LocalizedString.concat(desc, Dungeon.level.tileDesc(Dungeon.level.map[cell]));
		}
		this.titlebar = titlebar;

		if (hero.fieldOfView[cell]) {
			for (Blob blob : Dungeon.level.blobs.values()) {
				if (blob.volume > 0 && blob.cur[cell] > 0 && blob.tileDesc() != null) {
					if (!desc.equals(LocalizedString.EMPTY)) {
						desc = LocalizedString.concat(desc, "\n\n");
					}
					desc = LocalizedString.concat(desc, blob.tileDesc());
				}
			}
		}

		this.desc = desc.equals(LocalizedString.EMPTY) ? Messages.get(this, "nothing") : desc;

	}

	public LocalizedString desc() {
		return desc;
	}

	public IconTitle titlebar() {
		return titlebar;
	}
}
