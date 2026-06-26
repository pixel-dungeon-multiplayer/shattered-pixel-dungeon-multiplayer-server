/* Pixel Dungeon Multiplayer
 * Copyright (C) 2021-2023 Shaposhnikov Nikita
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

package io.github.pixeldungeonmultiplayer.shattered.server.noosa.tweeners;

import io.github.pixeldungeonmultiplayer.shattered.server.network.SendData;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.AlphaTweenerAction;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.tweeners.Tweener;
import com.watabou.utils.DeviceCompat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class AlphaTweener extends Tweener {

	@Nullable
	private final AlphaTweenerAction action;

	public AlphaTweener(@NotNull CharSprite sprite, float target_alpha, float interval) {
		super(sprite, interval);
		if (DeviceCompat.isDebug()) {
			Objects.requireNonNull(sprite);
		}
		if (sprite.ch == null) {
			GLog.n("Can't add alpha tweener to unknown character");
			this.action = null;
		} else {
			this.action = new AlphaTweenerAction(sprite.ch.id(), target_alpha, interval);
		}
	}


	@Override
	public void onAdd() {
		if (action != null) {
			SendData.sendActionForAll(action);
		}
	}

	@Override
	protected void updateValues(float progress) {

	}
}
