/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 * Copyright (C) 2021-2023 Nikita SHaposhnikov
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
package com.shatteredpixel.shatteredpixeldungeon.effects;

import io.github.pixeldungeonmultiplayer.shattered.server.network.SendData;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.FlareVisualAction;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.noosa.Visual;
import com.watabou.utils.PointF;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public final class Flare extends Visual {

	private float duration = 0;

	private boolean lightMode = true;

	private int color;
	private final int nRays;
	private final float radius;
	private final float angle;

	public float angularSpeed;
	private float lifespan;

	@Nullable
	private PointF position = null;
	private int pos = -1;

	public Flare( int nRays, float radius ) {
		super( 0, 0, 0, 0 );

        color = 0;

		this.nRays = nRays;
		this.radius = radius;

		angle = 45;
		angularSpeed = 180;

	}

//	public Flare hardlight(int color ) {
//		super();
//		hardlight(color);
//		this.color = color;
//		return this;
//
//	}

	@Contract("_, _ -> this")
	public Flare color(int color, boolean lightMode ) {
		this.lightMode = lightMode;
		hardlight( color );

		return this;
	}

	@Contract(mutates = "this")
	@Deprecated
	public PointF point(PointF pos){
		this.position = pos;
		return position;
	}

	public void show(int pos, float duration ) {
		this.pos = pos;
		this.duration = duration;

		SendThis();
	}

	public void SendThis(){
		if (position != null) {
			SendData.sendActionForAll(new FlareVisualAction(position.x, position.y, color, duration, lightMode, nRays, radius, angle, angularSpeed));
		} else {
			SendData.sendActionForAll(new FlareVisualAction(pos, color, duration, lightMode, nRays, radius, angle, angularSpeed));
		}
	}

	@Contract ("_ -> this")
	public Flare setAngularSpeed(float angularSpeed) {
		this.angularSpeed = angularSpeed;
		return this;
	}

	@Contract("_, _ -> this")
	public Flare show(Visual visual, float duration ) {
		if (visual instanceof CharSprite && ((CharSprite) visual).ch != null) {
			pos = ((CharSprite) visual).ch.pos;
		} else {
			point(visual.center());
		}
		visual.parent.addToBack( this );

		lifespan = this.duration = duration;
		SendThis();
		return this;
	}
}
