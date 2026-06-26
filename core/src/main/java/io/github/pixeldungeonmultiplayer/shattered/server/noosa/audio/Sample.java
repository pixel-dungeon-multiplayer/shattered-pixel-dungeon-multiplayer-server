/*
 * Pixel Dungeon Multiplayer
 * Copyright (C) 2021-2023  Nikita Shaposhnikov
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

import io.github.pixeldungeonmultiplayer.shattered.server.utils.Log;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import io.github.pixeldungeonmultiplayer.shattered.server.network.SendData;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.SampleAction;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Provides player for sounds. Singleton
 */
public enum Sample {

	INSTANCE;
	/**
	 * stores sounds loaded on clients
	 */
	protected final HashSet<String> ids =
			new HashSet<>();

	public void load( String... assets ) {
		ids.addAll(Arrays.asList(assets));
		SendData.sendActionForAll(new SampleAction.LoadAction(assets));
	}

	/**
	 * Unloads unsound on clients. Removes sound to list
	 * @param src sound asset
	 */
	public void unload( String src ) {
		SendData.sendActionForAll(new SampleAction.UnloadAction(src));
		ids.remove( src );
	}

	/**
	 * Sends list of loaded sounds to client
	 */
	public void reload(){
		SendData.sendActionForAll(new SampleAction.ReloadAction(ids.toArray(new String[0])));
	}

	public void play( String id ) {
		play( id, null );
	}
	public void play( String id, Hero hero ) {
		play( id, 1, hero );
	}
	public void play( String id, float volume ) {
		play( id, volume, null );
	}
	public void play( String id, float volume, Hero hero ) {
		play( id, volume, volume, 1, 1, hero );
	}
	public void play( String id, float volume, float pitch, Hero hero ) {
		play( id, volume, volume, 1, pitch, hero );
	}
	public void play( String id, float leftVolume, float rightVolume, float rate ) {
		play( id, leftVolume, rightVolume, rate, 1, null );
	}

	public void playDelayed(String id, float delay){
		playDelayed(id, delay, 1.0f, 1.0f);

	}
	public void playDelayed( String id, float delay, float volume, float pitch ) {
		play( id, volume, volume, 1.0f, pitch, delay,null );
	}
	public void play( String id, float leftVolume, float rightVolume, float rate, float pitch, @Nullable Hero hero) {
		play(id, leftVolume, rightVolume, rate, pitch, null, hero);
	}
	public void play( String id, float leftVolume, float rightVolume, float rate, float pitch, @Nullable Float delay, @Nullable Hero hero) {
		if (!ids.contains( id )) {
			//assert !DeviceCompat.isDebug(): "playing unloaded sample: " + id; todo
			Log.e("Sound", "Sound  playing unloaded sample: %s", id);
			load(id);
		}

		SampleAction.PlayAction action = new SampleAction.PlayAction(id, leftVolume, rightVolume, rate, pitch, delay);
		if (hero != null){
			SendData.sendAction(hero, action);
		} else {
			SendData.sendActionForAll(action);
		}
	}
	public void play(String id, float volume, float pitch){
		play(id, volume, volume, 1, pitch, null);
	}
	public void playDelayed( Object id, float delay ){
		playDelayed( id, delay, 1 );
	}

	public void playDelayed( Object id, float delay, float volume ) {
		playDelayed( id, delay, volume, volume, 1 );
	}
	public void playDelayed( Object id, float delay, float volume, float pitch ) {
		playDelayed( id, delay, volume, volume, pitch );
	}

	public void playDelayed( Object id, float delay, float leftVolume, float rightVolume, float pitch ) {}

}
