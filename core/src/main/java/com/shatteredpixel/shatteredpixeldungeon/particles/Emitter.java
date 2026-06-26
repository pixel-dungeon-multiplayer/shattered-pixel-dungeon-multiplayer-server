/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015  Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2016 Evan Debenham
 *
 * Shattered Pixel Dungeon Multiplayer
 * Copyright (C) 2021-2026 Shaposhnikov Nikita
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

package com.shatteredpixel.shatteredpixeldungeon.particles;

import io.github.pixeldungeonmultiplayer.shattered.server.network.SendData;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.*;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.dtos.emitters.EmitterAnchor;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.noosa.Group;
import com.watabou.noosa.particles.SerializableParticleFactory;
import com.watabou.utils.PointF;
import com.watabou.utils.SparseArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Emitter emits visual particles from random place in its area.
 * <p>
 * Emitter's area is Rect(x, y, width, height).
 * <p>
 * Emitter emits {@link #quantity} count of particles with {@link #interval} delay.
 * If {@code quantity == 0} emitter should be stopped manually */
public class Emitter /*this is temporary ->*/extends Group {
	int id = -1;
	private enum AnchorType {
		WORLD, CELL, TARGET
	}

	protected boolean lightMode = false;
	/**
	 * X position of left-top angle of emitter's area
	 */
	public float x;
	/**
	 * Y position of left-top angle of emitter's area
	 */
	public float y;
	/**
	 * Width of emitter's area
	 */
	public float width;
	/**
	 * Height of emitter's area
	 */
	public float height;

	/**
	 * If target != null, emitter's will use target's position instead of itself
	 */
	protected CharSprite target;
	/**
	 * If target != null, emitter's will use target's size instead of itself
	 */
	public boolean fillTarget = true;
	
	protected float interval;
	protected int quantity;

	private boolean on = false;
	private LiveStateNetworkAction pendingStartAction;
	private boolean startActionSent;

	/**
	 * Factory which producing particles
	 */
	protected Factory factory;
	private Integer cell = null;
	private PointF shift = new PointF(0, 0);
	private AnchorType anchorType = AnchorType.WORLD;
	public boolean visible = true;
	public static boolean freezeEmitters = false;

	public void cellPos(int cell) {
		cellPos(cell, 0, 0);
	}

	public void cellPos(int cell, float width, float height){
		clearAnchor();
		this.cell = cell;
		this.width = width;
		this.height = height;
		this.anchorType = AnchorType.CELL;
	}

	public void cellPosWithShift(int cell, float shiftX, float shiftY) {
		cellPosWithShift(cell, new PointF(shiftX, shiftY));
	}

	public void cellPosWithShift(int cell, PointF shift) {
		cellPosWithShift(cell, shift, 0,0);
	}

	public void cellPosWithShift(int cell, float shiftX,float shiftY, float width, float height) {
		cellPosWithShift(cell, new PointF(shiftX, shiftY), width, height);
	}

	public void cellPosWithShift(int cell, PointF shift, float width, float height) {
		clearAnchor();
		this.cell = cell;
		this.shift = shift;
		this.width = width;
		this.height = height;
		this.anchorType = AnchorType.CELL;
	}

	public void pos( PointF p ) {
		pos( p.x, p.y, 0, 0 );
	}
	
	public void pos( float x, float y, float width, float height ) {
		clearAnchor();
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.anchorType = AnchorType.WORLD;
	}
	public void pos(CharSprite target, float x, float y, float width, float height){
		clearAnchor();
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.target = target;
		this.anchorType = AnchorType.TARGET;
	}
	public void pos( CharSprite target ) {
		clearAnchor();
		this.target = target;
		this.anchorType = AnchorType.TARGET;
	}

	public void pos( CharSprite target, PointF shift ) {
		clearAnchor();
		this.target = target;
		this.shift = shift;
		this.fillTarget = false;
		this.anchorType = AnchorType.TARGET;
	}

	/**
	 * Emits {@code quantity} particles in one time
	 * @param factory factory of particles
	 * @param quantity count of particles
	 */
	public void burst( Factory factory, int quantity ) {
		start( factory, 0, quantity );
	}
	//This is temporary
	public void burst(Object factory, int quantity){}

	/**
	 * Emits particles each {@code interval} seconds. Should be stopped manually
	 * @param factory factory of particles
	 * @param interval interval between emitting
	 */
	public void pour( Factory factory, float interval ) {
		start( factory, interval, 0 );
	}
	//This is temporary
	public void pour(Object factory, float interval){

	}

	/**
	 * Emits {@code quantity} particles each {@code interval} seconds
	 * @param factory factory of particles
	 * @param interval interval between emitting
	 */
	static SparseArray<Emitter> infiniteEmitters = new SparseArray<>();
	static int idCounter = 0;
	static void assignEmitterId(Emitter emitter){
		emitter.id = idCounter;
		idCounter++;
	}
	public void start( Factory factory, float interval, int quantity ) {

		if (id != -1 && quantity > 0) {
			stopInfinite();
		}

		this.factory = factory;
		this.lightMode = factory.lightMode();
		
		this.interval = interval;
		this.quantity = quantity;

		pendingStartAction = createStartAction();
		startActionSent = false;

		if (sendSelf()) {
			on(true);
			if (quantity == 0) {
				infiniteEmitters.put(id, this);
			}
		}
	}
	//TODO: remove all uses of this
	public void update(){}

	public void revive(){
		clearAnchor();
		id = -1;
		factory = null;
		interval = 0;
		quantity = 0;
		on = false;
		pendingStartAction = null;
		startActionSent = false;
		visible = true;
		fillTarget = true;
	}
	//TODO: remove all uses of this
	public boolean autoKill = false;
	//TODO: remove all uses of this
	public void killAndErase(){
		stopInfinite();
	}
	@Override
	public void kill(){
		killAndErase();
	}

	//TODO: check this
	protected boolean isFrozen(){return false;}

	private void clearAnchor() {
		x = 0;
		y = 0;
		width = 0;
		height = 0;
		target = null;
		cell = null;
		shift = new PointF(0, 0);
		anchorType = AnchorType.WORLD;
	}

	@NotNull
	private EmitterAnchor buildAnchor() {
		if (anchorType == AnchorType.TARGET) {
			return EmitterAnchor.target(Objects.requireNonNull(target), x, y, width, height, shift.x, shift.y, fillTarget);
		}
		if (anchorType == AnchorType.CELL) {
			return EmitterAnchor.cell(Objects.requireNonNull(cell), x, y, width, height, shift.x, shift.y);
		}
		return EmitterAnchor.world(x, y, width, height, shift.x, shift.y);
	}

	private void stopInfinite() {
		if (id != -1) {
			SendData.sendActionForAll(new EmitterStopAction(id));
			infiniteEmitters.remove(id);
			id = -1;
		}
	}

	protected boolean sendSelf() {
		if (parent == null || pendingStartAction == null || startActionSent) {
			return false;
		}
		if (quantity == 0 && id == -1) {
			assignEmitterId(this);
		}
		SendData.packAndSendActionForAll(pendingStartAction);
		startActionSent = true;
		return true;
	}

	@NotNull
	private LiveStateNetworkAction createStartAction() {
		if (quantity == 0) {
			return new EmitterPourAction(this);
		} else if (interval == 0) {
			return new EmitterBurstAction(this);
		} else {
			return new EmitterStartAction(this);
		}
	}

	@Override
	public void onAdd() {
		super.onAdd();
		if (sendSelf()) {
			on(true);
			if (quantity == 0) {
				infiniteEmitters.put(id, this);
			}
		}
	}

	public int networkId() {
		return id;
	}

	@NotNull
	public EmitterAnchor anchor() {
		return buildAnchor();
	}

	public Factory networkFactory() {
		return factory;
	}

	public float networkInterval() {
		return interval;
	}

	public int networkQuantity() {
		return quantity;
	}

	public boolean networkFillTarget() {
		return fillTarget;
	}

	@Nullable
	public LiveStateNetworkAction networkStartAction() {
		return pendingStartAction;
	}

	public void pos(float x, float y) {
		pos(x, y, 0,0);
	}

	/**
	 * if {@code on == false}, Emitter stops emitting
	 */
	public boolean on() {
		return on;
	}

	public void on(boolean on) {
		this.on = on;
		if (!on){
			killAndErase();
		}
	}

	abstract public static class Factory implements SerializableParticleFactory {

		public boolean lightMode() {
			return false;
		}

		//TODO: remove all overrides of this
		public void emit(Emitter emitter, int index, float x, float y ){};
	}
	protected void emit(int index){}
}
