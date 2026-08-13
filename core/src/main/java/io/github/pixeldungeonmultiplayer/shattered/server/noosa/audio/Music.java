/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
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

package io.github.pixeldungeonmultiplayer.shattered.server.noosa.audio;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import io.github.pixeldungeonmultiplayer.shattered.server.network.SendData;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.MusicAction;
@SuppressWarnings("NewApi")
//TODO: add possibility to play music for a specific hero
public enum Music {

	INSTANCE;
	public MusicAction currentAction;

	public synchronized void play( String assetName, boolean looping ) {
		currentAction = new MusicAction.PlayAction(assetName, looping);
		SendData.sendActionForAll( currentAction );
	}

	public synchronized void playTracks( String[] tracks, float[] chances, boolean shuffle){
		currentAction = new MusicAction.PlayTracksAction(tracks, chances, shuffle);
		SendData.sendActionForAll( currentAction );
	}

	public synchronized void fadeOut(float duration, MusicAction onComplete){
		currentAction = new MusicAction.FadeOutAction(duration, onComplete);
		SendData.sendActionForAll( currentAction );
	}


	public synchronized void end() {
		currentAction = new MusicAction.EndAction();
		SendData.sendActionForAll( currentAction );
	}
	public synchronized void sendLastAction(Hero hero) {
		if (currentAction != null) {
			SendData.sendAction(hero, currentAction);
		}
	}

}
