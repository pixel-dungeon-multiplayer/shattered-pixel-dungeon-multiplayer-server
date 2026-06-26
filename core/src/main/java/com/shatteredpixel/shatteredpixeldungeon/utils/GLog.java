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

package com.shatteredpixel.shatteredpixeldungeon.utils;

import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import io.github.pixeldungeonmultiplayer.shattered.server.network.SendData;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.ChatMessageAction;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.Signal;

public class GLog {

	public static final String TAG = "GAME";
	
	public static final String POSITIVE		= "++ ";
	public static final String NEGATIVE		= "-- ";
	public static final String WARNING		= "** ";
	public static final String CUSTOM = "&&"; //&& + custom hex color
	public static final String HIGHLIGHT	= "@@ ";

	public static final String NEW_LINE	    = "\n";
	
	public static Signal<String> update = new Signal<>();

	public static void newLine(){
		update.dispatch( NEW_LINE );
	}
	
	public static void i( String text, Object... args ) {
		LocalizedString str;
		if (args != null && args.length > 0) {
			str = Messages.format(text, args);
		}
		else {
			str = LocalizedString.raw(text, args);
		}

		DeviceCompat.log(TAG, str.toString());
		update.dispatch(str.toString());
		SendData.sendActionForAll(new ChatMessageAction(text));
	}

	public static void i(LocalizedString text) {
		DeviceCompat.log(TAG, text.toString());
		update.dispatch(text.toString());
		SendData.sendActionForAll(new ChatMessageAction(text));
	}

	public static void iWithTarget(Hero hero, LocalizedString text ) {
		SendData.sendAction(hero, new ChatMessageAction(text));
	}

	@SuppressWarnings("unused")
	public static void withColor(String text, int color, Object... args) {
		i(CUSTOM+Integer.toHexString(color), text, args);
	}
	
	public static void p( String text, Object... args ) {
		i( POSITIVE + text, args );
	}

	public static void p( LocalizedString text ) {
		i( LocalizedString.raw(POSITIVE + "%s", text) );
	}
	
	public static void n( String text, Object... args ) {
		i( NEGATIVE + text, args );
	}

	public static void n( LocalizedString text ) {
		i( LocalizedString.raw(NEGATIVE + "%s", text) );
	}
	
	public static void w( String text, Object... args ) {
		i( WARNING + text, args );
	}

	public static void w( LocalizedString text ) {
		i( LocalizedString.raw(WARNING + "%s", text) );
	}
	
	public static void h( String text, Object... args ) {
		i( HIGHLIGHT + text, args );
	}

	public static void h( LocalizedString text ) {
		i( LocalizedString.raw(HIGHLIGHT + "%s", text) );
	}
}
