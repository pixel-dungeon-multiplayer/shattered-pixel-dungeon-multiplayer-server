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

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.glwrap.Blending;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import io.github.pixeldungeonmultiplayer.shattered.server.network.SendData;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.BeamVisualAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.dtos.BeamAnchor;
import io.github.pixeldungeonmultiplayer.shattered.server.noosa.audio.Sample;
import com.watabou.utils.PointF;

public class Beam extends Image {
	
	private static final double A = 180 / Math.PI;
	
	private  float duration;
	
	private float timeLeft;

	private BeamAnchor from;

	private BeamAnchor to;

	private Beam(PointF s, PointF e, Effects.Type asset, float duration) {
		super( Effects.get( asset ) );
		
		origin.set( 0, height / 2 );
		
		x = s.x - origin.x;
		y = s.y - origin.y;
		
		float dx = e.x - s.x;
		float dy = e.y - s.y;
		angle = (float)(Math.atan2( dy, dx ) * A);
		scale.x = (float)Math.sqrt( dx * dx + dy * dy ) / width;
		
		Sample.INSTANCE.play( Assets.Sounds.RAY );
		
		timeLeft = this.duration = duration;
	}

	private Beam(BeamAnchor s, BeamAnchor e, Effects.Type asset, float duration) {
		this(s.toPointF(), e.toPointF(), asset, duration);
		from = s;
		to = e;
	}

	public static class DeathRay extends Beam{
		public DeathRay(PointF s, PointF e){
			super(s, e, Effects.Type.DEATH_RAY, 0.5f);
		}
		public DeathRay(CharSprite s, CharSprite e){
			super(BeamAnchor.target(s), BeamAnchor.target(e), Effects.Type.DEATH_RAY, 0.5f);
		}
		public DeathRay(CharSprite s, int raisedCell){
			super(BeamAnchor.target(s), BeamAnchor.raisedCell(raisedCell), Effects.Type.DEATH_RAY, 0.5f);
		}
		public DeathRay(int cell, CharSprite e){
			super(BeamAnchor.cell(cell), BeamAnchor.target(e), Effects.Type.DEATH_RAY, 0.5f);
		}
	}

	public static class LightRay extends Beam{
		public LightRay(PointF s, PointF e){
			super(s, e, Effects.Type.LIGHT_RAY, 1f);
		}
		public LightRay(CharSprite s, int raisedCell){
			super(BeamAnchor.target(s), BeamAnchor.raisedCell(raisedCell), Effects.Type.LIGHT_RAY, 1f);
		}
	}

	public static class SunRay extends Beam{
		public SunRay(PointF s, PointF e){
			super(s, e, Effects.Type.LIGHT_RAY, 1f);
			tint(1, 1, 0.25f, 1);
		}
		public SunRay(CharSprite s, int raisedCell){
			super(BeamAnchor.target(s), BeamAnchor.raisedCell(raisedCell), Effects.Type.LIGHT_RAY, 1f);
			tint(1, 1, 0.25f, 1);
		}
	}

	public static class HealthRay extends Beam{
		public HealthRay(PointF s, PointF e){
			super(s, e, Effects.Type.HEALTH_RAY, 0.75f);
		}
		public HealthRay(CharSprite s, CharSprite e){
			super(BeamAnchor.target(s), BeamAnchor.target(e), Effects.Type.HEALTH_RAY, 0.75f);
		}
		public HealthRay(CharSprite s, CharSprite e, boolean useDestination){
			super(useDestination ? BeamAnchor.targetDestination(s) : BeamAnchor.target(s),
					useDestination ? BeamAnchor.targetDestination(e) : BeamAnchor.target(e),
					Effects.Type.HEALTH_RAY, 0.75f);
		}
		public HealthRay(CharSprite s, int raisedCell){
			super(BeamAnchor.target(s), BeamAnchor.raisedCell(raisedCell), Effects.Type.HEALTH_RAY, 0.75f);
		}
		public HealthRay(int cell, CharSprite e){
			super(BeamAnchor.cell(cell), BeamAnchor.target(e), Effects.Type.HEALTH_RAY, 0.75f);
		}
	}

	@Override
	public void onAdd() {
		super.onAdd();
		if (from != null && to != null) {
			SendData.sendActionForAll(new BeamVisualAction(this, from, to, duration));
		}
	}
	
	@Override
	public void update() {
		super.update();
		
		float p = timeLeft / duration;
		alpha( p );
		scale.set( scale.x, p );
		
		if ((timeLeft -= Game.elapsed) <= 0) {
			killAndErase();
		}
	}
	
	@Override
	public void draw() {
		Blending.setLightMode();
		super.draw();
		Blending.setNormalMode();
	}
}
