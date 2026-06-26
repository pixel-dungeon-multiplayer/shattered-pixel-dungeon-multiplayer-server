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

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import static io.github.pixeldungeonmultiplayer.shattered.server.network.SendData.sendActionForAll;
import io.github.pixeldungeonmultiplayer.shattered.server.utils.Log;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.CharSpriteAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.CharEmoAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.SpriteFlashAction;

import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.DarkBlock;
import com.shatteredpixel.shatteredpixeldungeon.effects.EmoIcon;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.IceBlock;
import com.shatteredpixel.shatteredpixeldungeon.effects.GlowBlock;
import com.shatteredpixel.shatteredpixeldungeon.effects.ShieldHalo;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.effects.TorchHalo;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SnowParticle;
import io.github.pixeldungeonmultiplayer.shattered.server.network.SendData;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.texturepack.TexturePackManager;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.CharHealthIndicator;
import com.watabou.glwrap.Matrix;
import com.watabou.glwrap.Vertexbuffer;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.NoosaScript;
import io.github.pixeldungeonmultiplayer.shattered.server.noosa.audio.Sample;
import com.shatteredpixel.shatteredpixeldungeon.particles.Emitter;
import io.github.pixeldungeonmultiplayer.shattered.server.noosa.tweeners.AlphaTweener;
import com.watabou.noosa.tweeners.PosTweener;
import com.watabou.noosa.tweeners.Tweener;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;
import org.jetbrains.annotations.Nullable;

import java.nio.Buffer;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.HashSet;

public class CharSprite extends MovieClip implements Tweener.Listener, MovieClip.Listener {
	protected final Set<State> states = new CopyOnWriteArraySet<State>();
	// Color constants for floating text
	public static final int DEFAULT		= 0xFFFFFF;
	public static final int POSITIVE	= 0x00FF00;
	public static final int NEGATIVE	= 0xFF0000;
	public static final int WARNING		= 0xFF8800;
	public static final int NEUTRAL		= 0xFFFF00;
	
	public static final float DEFAULT_MOVE_INTERVAL = 0.1f;
	private static float moveInterval = DEFAULT_MOVE_INTERVAL;
	private static final float FLASH_INTERVAL	= 0.05f;

	//the amount the sprite is raised from flat when viewed in a raised perspective
	protected float perspectiveRaise    = 6 / 16f; //6 pixels

	//the width and height of the shadow are a percentage of sprite size
	//offset is the number of pixels the shadow is moved down or up (handy for some animations)
	protected boolean renderShadow  = false;
	protected float shadowWidth     = 1.2f;
	protected float shadowHeight    = 0.25f;
	protected float shadowOffset    = 0.25f;

	public enum State {
		BURNING, LEVITATING, INVISIBLE, PARALYSED, FROZEN, ILLUMINATED, CHILLED, DARKENED, MARKED, HEALING, SHIELDED, HEARTS, GLOWING, AURA
	}
	
	protected Animation idle;
	protected Animation run;
	protected Animation attack;
	protected Animation operate;
	protected Animation zap;
	protected Animation die;
	
	protected Callback animCallback;
	
	protected PosTweener motion;
	
	protected Emitter burning;
	protected Emitter chilled;
	protected Emitter marked;
	protected Emitter levitation;
	protected Emitter healing;
	protected Emitter hearts;
	
	protected IceBlock iceBlock;
	protected DarkBlock darkBlock;
	protected GlowBlock glowBlock;
	protected TorchHalo light;
	protected ShieldHalo shield;
	protected AlphaTweener invisible;
	protected Flare aura;

	public void aura( int color ){
		add(State.AURA);
		auraColor = color;
	}


	private EmoIcon emo;
	protected CharHealthIndicator health;

	private Tweener jumpTweener;
	private Callback jumpCallback;

	protected float flashTime = 0;
	
	protected boolean sleeping = false;

	public Char ch;

	//used to prevent the actor associated with this sprite from acting until movement completes
	public volatile boolean isMoving = false;
	public Set<State> states(){
		return states;
	}

	protected EmoIcon getEmo() {
		return emo;
	}

	protected void setEmo(EmoIcon emo) {
		this.emo = emo;
		if (ch != null) {
			sendActionForAll(new CharEmoAction(ch.id(), emotionName(emo)));
		}
	}

	private String emotionName(EmoIcon emo) {
		if (emo instanceof EmoIcon.Alert) return "alert";
		if (emo instanceof EmoIcon.Sleep) return "sleep";
		if (emo instanceof EmoIcon.Lost) return "lost";
		if (emo instanceof EmoIcon.Investigate) return "investigate";
		return null;
	}


	@Nullable
	// needs to return spritename.json
	// any non-null return needs a texture to be loaded for that sprite
	public String getSpriteAsset() {
		return TexturePackManager.getMobAnimation(getClass());	}
	public String spriteName() {
		return this.getClass().getName();
	}
	public CharSprite() {
		super();
		listener = this;
		ShatteredPixelDungeon.scene().add(this);
	}
	
	@Override
	public void play(Animation anim) {
		//Shouldn't interrupt the dieing animation
		if (curAnim == null || curAnim != die) {
			super.play(anim);
		}
	}
	
	//intended to be used for placing a character in the game world
	public void link( Char ch ) {
		linkVisuals( ch );
		
		this.ch = ch;
		ch.setSprite(this);
		
		place( ch.pos );
		turnTo( ch.pos, Random.Int( Dungeon.level.length() ) );
		renderShadow = true;
		
		if (!(ch instanceof Hero)) {
			if (health == null) {
				health = new CharHealthIndicator(ch);
			} else {
				health.target(ch);
			}
		}

		ch.updateSpriteState();
		place(ch.pos);
	}

	@Override
	public void destroy() {
		super.destroy();
		if (ch != null && ch.getSprite() == this){
			ch.setSprite(null);
		}
	}

	//used for just updating a sprite based on a given character, not linking them or placing in the game
	public void linkVisuals( Char ch ){
		//do nothin by default
	}
	
	public PointF worldToCamera( int cell ) {
		
		final int csize = DungeonTilemap.SIZE;
		
		return new PointF(
			PixelScene.align(Camera.main, ((cell % Dungeon.level.width()) + 0.5f) * csize - width() * 0.5f),
			PixelScene.align(Camera.main, ((cell / Dungeon.level.width()) + 1.0f) * csize - height() - csize * perspectiveRaise)
		);
	}
	
	public void place( int cell ) {
		point( worldToCamera( cell ) );
		sendActionForAll(new CharSpriteAction(ch.id(), "place", null, cell));
	}
	
	public void showStatus(int color, String text, Object... args ) {
		showStatusWithIcon(color, LocalizedString.raw(text, args), FloatingText.NO_ICON);
	}
	public void showStatus(int color, LocalizedString text) {
		showStatusWithIcon(color, text, FloatingText.NO_ICON);
	}


	public void showStatusWithIcon( int color, String text, int icon, Object... args ) {
		LocalizedString localizedString = LocalizedString.raw(text, args);
		showStatusWithIcon(color, localizedString, icon);
	}

	public void showStatusWithIcon( int color, LocalizedString text, int icon ) {
		if (visible) {
			float x = destinationCenter().x;
			float y = destinationCenter().y - height()/2f;
			int pos = DungeonTilemap.worldToTile(x, y + height(), Dungeon.level.width());
			if (ch != null) {
				FloatingText.show( x, y, pos, text, color, icon, true );
			} else {
				FloatingText.show( x, y, -1, text, color, icon, true );
			}
		}
	}
	
	public void idle() {
		if (curAnim == idle) return;
		if (ch != null) {
			sendActionForAll(new CharSpriteAction(ch.id(), "idle", null, null));
		}
		play(idle);
	}
	
	public void move( int from, int to ) {
		if (ch != null) {
			sendActionForAll(new CharSpriteAction(ch.id(), "run", from, to));
		}
		turnTo( from , to );

		play( run );
		
		motion = new PosTweener( this, worldToCamera( to ), moveInterval );
		motion.listener = this;
		parent.add( motion );

		isMoving = true;
		
		if (visible && Dungeon.level.water[from] && !ch.flying) {
			GameScene.ripple( from );
		}

	}
	
	public static void setMoveInterval( float interval){
		moveInterval = interval;
	}
	
	//returns where the center of this sprite will be after it completes any motion in progress
	public PointF destinationCenter(){
		PosTweener motion = this.motion;
		if (motion != null && motion.elapsed >= 0){
			return new PointF(motion.end.x + width()/2f, motion.end.y + height()/2f);
		} else {
			return center();
		}
	}
	
	public void interruptMotion() {
		if (motion != null) {
			motion.stop(false);
		}
	}
	
	public void attack( int cell ) {
		attack( cell, null );
	}
	
	public synchronized void attack( int cell, Callback callback ) {
		animCallback = callback;
		turnTo( ch.pos, cell );
		if (ch != null) {
			sendActionForAll(new CharSpriteAction(ch.id(), "attack", null, cell));
		}
		play( attack );
	}
	
	public void operate( int cell ) {
		operate( cell, null );
	}
	
	public synchronized void operate( int cell, Callback callback ) {
		animCallback = callback;
		turnTo( ch.pos, cell );
		if (ch != null) {
			sendActionForAll(new CharSpriteAction(ch.id(), "operate", null, cell));
		}
		play( operate );
	}
	
	public void zap( int cell ) {
		zap( cell, null );
	}
	
	public synchronized void zap( int cell, Callback callback ) {
		animCallback = callback;
		turnTo( ch.pos, cell );
		if (ch != null) {
			sendActionForAll(new CharSpriteAction(ch.id(), "zap", null, cell));
		}
		play( zap );
	}
	
	public void turnTo( int from, int to ) {
		int fx = from % Dungeon.level.width();
		int tx = to % Dungeon.level.width();
		if (tx > fx) {
			flipHorizontal = false;
		} else if (tx < fx) {
			flipHorizontal = true;
		}
		sendActionForAll(new CharSpriteAction(ch.id(), "turn", from, to));
	}

	public void jump( int from, int to, Callback callback ) {
		float distance = Math.max( 1f, Dungeon.level.trueDistance( from, to ));
		jump( from, to, distance * 2, distance * 0.1f, callback );
	}

	public void jump( int from, int to, float height, float duration,  Callback callback ) {
		jumpCallback = callback;

		jumpTweener = new JumpTweener( this, worldToCamera( to ), height, duration );
		jumpTweener.listener = this;
		parent.add( jumpTweener );

		turnTo( from, to );
		if (ch != null) {
			sendActionForAll(new CharSpriteAction(ch.id(), "jump", from, to));
		}
	}

	public void die() {
		sleeping = false;
		processStateRemoval( State.PARALYSED );
		if (ch != null) {
			sendActionForAll(new CharSpriteAction(ch.id(), "die", null, null));
		}
		play( die );

		hideEmo();
		
		if (health != null){
			health.killAndErase();
		}
	}
	
	public Emitter emitter() {
		Emitter emitter = GameScene.emitter();
		if (emitter != null) emitter.pos( this );
		return emitter;
	}
	
	public Emitter centerEmitter() {
		Emitter emitter = GameScene.emitter();
		if (emitter != null) emitter.pos( center() );
		return emitter;
	}
	
	public Emitter bottomEmitter() {
		Emitter emitter = GameScene.emitter();
		if (emitter != null) emitter.pos( x, y + height, width, 0 );
		return emitter;
	}
	
	public void burst( final int color, int n ) {
		if (visible) {
			Splash.at( center(), color, n );
		}
	}
	
	public void bloodBurstA( PointF from, int damage ) {
		if (visible) {
			PointF c = center();
			int n = (int)Math.min( 9 * Math.sqrt( (double)damage / ch.getHT()), 9 );
			Splash.at( c, PointF.angle( from, c ), 3.1415926f / 2, blood(), n );
		}
	}

	public int blood() {
		return 0xFFBB0000;
	}
	
	public void flash() {
		ra = ba = ga = 1f;
		flashTime = FLASH_INTERVAL;
		if (ch == null) {
			Log.w("CharSprite", "char sprite has no owner. Ignored");
			return;
		}
		sendActionForAll(new SpriteFlashAction(ch.id(), FLASH_INTERVAL));
	}

	private final HashSet<State> stateAdditions = new HashSet<>();

	public void add( State state ) {
		//instant as it just changes an animation property that will get read later
		if (state == State.PARALYSED){
			processStateAddition(state);
		} else {
			synchronized (State.class) {
				stateRemovals.remove(state);
				stateAdditions.add(state);
			}
		}
	}

	private int auraColor = 0;
	private int auraRays = 0;

	//Aura needs color and ray count data too
	public void aura( int color, int nRays ){
		add(State.AURA);
		auraColor = color;
		auraRays = nRays;
	}

	protected synchronized void processStateAddition( State state ) {
		states.add(state);
		switch (state) {
			case BURNING:
				if (burning != null) burning.on(false);
				burning = emitter();
				burning.pour(FlameParticle.FACTORY, 0.06f);
				if (visible) {
					Sample.INSTANCE.play(Assets.Sounds.BURNING);
				}
				break;
			case LEVITATING:
				if (levitation != null) levitation.on(false);
				levitation = emitter();
				levitation.pour(Speck.factory(Speck.JET), 0.02f);
				break;
			case INVISIBLE:
				if (invisible != null) invisible.killAndErase();
				invisible = new AlphaTweener(this, 0.4f, 0.4f);
				if (parent != null) {
					parent.add(invisible);
				} else
					alpha(0.4f);
				break;
			case PARALYSED:
				paused = true;
				break;
			case FROZEN:
				if (iceBlock != null) iceBlock.killAndErase();
				iceBlock = IceBlock.freeze(this);
				break;
			case ILLUMINATED:
				if (light != null) light.putOut();
				GameScene.effect(light = new TorchHalo(this));
				break;
			case CHILLED:
				if (chilled != null) chilled.on(false);
				chilled = emitter();
				chilled.pour(SnowParticle.FACTORY, 0.1f);
				break;
			case DARKENED:
				if (darkBlock != null) darkBlock.killAndErase();
				darkBlock = DarkBlock.darken(this);
				break;
			case MARKED:
				if (marked != null) marked.on(false);
				marked = emitter();
				marked.pour(ShadowParticle.UP, 0.1f);
				break;
			case HEALING:
				if (healing != null) healing.on(false);
				healing = emitter();
				healing.pour(Speck.factory(Speck.HEALING), 0.5f);
				break;
			case SHIELDED:
				if (shield != null) shield.killAndErase();
				GameScene.effect(shield = new ShieldHalo(this));
				break;
			case HEARTS:
				if (hearts != null) hearts.on(false);
				hearts = emitter();
				hearts.pour(Speck.factory(Speck.HEART), 0.5f);
				break;
			case GLOWING:
				if (glowBlock != null) glowBlock.killAndErase();
				glowBlock = GlowBlock.lighten(this);
				break;
			case AURA:
				if (aura != null)   aura.killAndErase();
				float size = Math.max(width(), height());
				size = Math.max(size+4, 16);
				aura = new Flare(auraRays, size);
				aura.angularSpeed = 90;
				aura.color(auraColor, true);
				aura.visible = visible;

				if (parent != null) {
					aura.show(this, 0);
				}
				break;
		}
		if (ch != null) {
			SendData.sendAddCharSpriteState(ch, state);
		}
	}

	private final HashSet<State> stateRemovals = new HashSet<>();

	public void remove( State state ) {
		//instant as it just changes an animation property that will get read later
		if (state == State.PARALYSED){
			paused = false;
			processStateRemoval(state);
		} else {
			synchronized (State.class) {
				stateAdditions.remove(state);
				stateRemovals.add(state);
			}
		}
	}

	public void clearAura(){
		remove(State.AURA);
	}

	protected synchronized void processStateRemoval( State state ) {
		states.remove(state);
		switch (state) {
			case BURNING:
				if (burning != null) {
					burning.on(false);
					burning = null;
				}
				break;
			case LEVITATING:
				if (levitation != null) {
					levitation.on(false);
					levitation = null;
				}
				break;
			case INVISIBLE:
				if (invisible != null) {
					invisible.killAndErase();
					invisible = null;
				}
				alpha(1f);
				break;
			case PARALYSED:
				paused = false;
				break;
			case FROZEN:
				if (iceBlock != null) {
					iceBlock.melt();
					iceBlock = null;
				}
				break;
			case ILLUMINATED:
				if (light != null) {
					light.putOut();
					light = null;
				}
				break;
			case CHILLED:
				if (chilled != null) {
					chilled.on(false);
					chilled = null;
				}
				break;
			case DARKENED:
				if (darkBlock != null) {
					darkBlock.lighten();
					darkBlock = null;
				}
				break;
			case MARKED:
				if (marked != null) {
					marked.on(false);
					marked = null;
				}
				break;
			case HEALING:
				if (healing != null) {
					healing.on(false);
					healing = null;
				}
				break;
			case SHIELDED:
				if (shield != null) {
					shield.putOut();
				}
				break;
			case HEARTS:
				if (hearts != null) {
					hearts.on(false);
					hearts = null;
				}
				break;
			case GLOWING:
				if (glowBlock != null){
					glowBlock.darken();
					glowBlock = null;
				}
				break;
			case AURA:
				if (aura != null){
					aura.killAndErase();
					aura = null;
				}
				break;
		}
		if (ch != null) {
			SendData.sendRemoveCharSpriteState(ch, state);
		}
	}
	
	@Override
	public void update() {
		if (paused && ch != null && curAnim != null && !curAnim.looped && !finished){
			listener.onComplete(curAnim);
			finished = true;
		}
		
		super.update();
		
		if (flashTime > 0 && (flashTime -= Game.elapsed) <= 0) {
			resetColor();
		}

		synchronized (State.class) {
			for (State s : stateAdditions) {
				processStateAddition(s);
			}
			stateAdditions.clear();
			for (State s : stateRemovals) {
				processStateRemoval(s);
			}
			stateRemovals.clear();
		}

		if (burning != null) {
			burning.visible = visible;
		}
		if (levitation != null) {
			levitation.visible = visible;
		}
		if (iceBlock != null) {
			iceBlock.visible = visible;
		}
		if (light != null) {
			light.visible = visible;
		}
		if (chilled != null) {
			chilled.visible = visible;
		}
		if (darkBlock != null) {
			darkBlock.visible = visible;
		}
		if (marked != null) {
			marked.visible = visible;
		}
		if (healing != null) {
			healing.visible = visible;
		}
		if (hearts != null) {
			hearts.visible = visible;
		}
		//shield fx updates its own visibility
		if (aura != null) {
			if (aura.parent == null) {
				aura.show(this, 0);
			}
			aura.visible = visible;
			aura.point(center());
		}
		if (glowBlock != null){
			glowBlock.visible =visible;
		}

		if (sleeping) {
			showSleep();
		} else {
			hideSleep();
		}
		synchronized (EmoIcon.class) {
			if (getEmo() != null && getEmo().alive) {
				getEmo().visible = visible;
			}
		}
	}
	
	@Override
	public void resetColor() {
		super.resetColor();
		if (invisible != null){
			alpha(0.4f);
		}
	}
	
	public void showSleep() {
		synchronized (EmoIcon.class) {
			if (!(getEmo() instanceof EmoIcon.Sleep)) {
				if (getEmo() != null) {
					getEmo().killAndErase();
				}
				setEmo(new EmoIcon.Sleep(this));
				getEmo().visible = visible;
			}
		}
		idle();
	}
	
	public void hideSleep() {
		synchronized (EmoIcon.class) {
			if (getEmo() instanceof EmoIcon.Sleep) {
				getEmo().killAndErase();
				setEmo(null);
			}
		}
	}
	
	public void showAlert() {
		synchronized (EmoIcon.class) {
			if (!(getEmo() instanceof EmoIcon.Alert)) {
				if (getEmo() != null) {
					getEmo().killAndErase();
				}
				setEmo(new EmoIcon.Alert(this));
				getEmo().visible = visible;
			}
		}
	}
	
	public void hideAlert() {
		synchronized (EmoIcon.class) {
			if (getEmo() instanceof EmoIcon.Alert) {
				getEmo().killAndErase();
				setEmo(null);
			}
		}
	}

	public void showInvestigate() {
		synchronized (EmoIcon.class) {
			if (!(emo instanceof EmoIcon.Investigate)) {
				if (emo != null) {
					emo.killAndErase();
				}
				setEmo(new EmoIcon.Investigate(this));
				getEmo().visible = visible;
			}
		}
	}

	public void hideInvestigate() {
		synchronized (EmoIcon.class) {
			if (emo instanceof EmoIcon.Investigate) {
				emo.killAndErase();
				setEmo(null);
			}
		}
	}

	public void showLost() {
		synchronized (EmoIcon.class) {
			if (!(getEmo() instanceof EmoIcon.Lost)) {
				if (getEmo() != null) {
					getEmo().killAndErase();
				}
				setEmo(new EmoIcon.Lost(this));
				getEmo().visible = visible;
			}
		}
	}
	
	public void hideLost() {
		synchronized (EmoIcon.class) {
			if (getEmo() instanceof EmoIcon.Lost) {
				getEmo().killAndErase();
				setEmo(null);
			}
		}
	}

	public void hideEmo(){
		synchronized (EmoIcon.class) {
			if (getEmo() != null) {
				getEmo().killAndErase();
				setEmo(null);
			}
		}
	}
	
	@Override
	public void kill() {
		super.kill();
		
		hideEmo();
		
		for( State s : State.values()){
			processStateRemoval(s);
		}
		
		if (health != null){
			health.killAndErase();
		}
	}

	private float[] shadowMatrix = new float[16];

	@Override
	protected void updateMatrix() {
		super.updateMatrix();
		Matrix.copy(matrix, shadowMatrix);
		Matrix.translate(shadowMatrix,
				(width * (1f - shadowWidth)) / 2f,
				(height * (1f - shadowHeight)) + shadowOffset);
		Matrix.scale(shadowMatrix, shadowWidth, shadowHeight);
	}

	@Override
	public void draw() {
		if (texture == null || (!dirty && buffer == null))
			return;

		if (renderShadow) {
			if (dirty) {
				((Buffer)verticesBuffer).position(0);
				verticesBuffer.put(vertices);
				if (buffer == null)
					buffer = new Vertexbuffer(verticesBuffer);
				else
					buffer.updateVertices(verticesBuffer);
				dirty = false;
			}

			NoosaScript script = script();

			texture.bind();

			script.camera(camera());

			updateMatrix();

			script.uModel.valueM4(shadowMatrix);
			script.lighting(
					0, 0, 0, am * .6f,
					0, 0, 0, aa * .6f);

			script.drawQuad(buffer);
		}

		super.draw();

	}

	@Override
	public void onComplete( Tweener tweener ) {
		if (tweener == jumpTweener) {

			if (visible && Dungeon.level.water[ch.pos] && !ch.flying) {
				GameScene.ripple( ch.pos );
			}
			if (jumpCallback != null) {
				jumpCallback.call();
			}
			GameScene.sortMobSprites();

		} else if (tweener == motion) {

			synchronized (this) {
				isMoving = false;

				motion.killAndErase();
				motion = null;
				ch.onMotionComplete();

				GameScene.sortMobSprites();
				notifyAll();
			}

		}
	}

	@Override
	public synchronized void onComplete( Animation anim ) {
		
		if (animCallback != null) {
			Callback executing = animCallback;
			animCallback = null;
			executing.call();
		} else {
			
			if (anim == attack) {
				
				idle();
				ch.onAttackComplete();
				
			} else if (anim == operate) {
				
				idle();
				ch.onOperateComplete();
				
			}
			
		}
	}

	private static class JumpTweener extends Tweener {

		public CharSprite visual;

		public PointF start;
		public PointF end;

		public float height;

		public JumpTweener( CharSprite visual, PointF pos, float height, float time ) {
			super( visual, time );

			this.visual = visual;
			start = visual.point();
			end = pos;

			this.height = height;
		}

		@Override
		protected void updateValues( float progress ) {
			float hVal = -height * 4 * progress * (1 - progress);
			visual.point( PointF.inter( start, end, progress ).offset( 0, hVal ) );
			visual.shadowOffset = 0.25f - hVal*0.8f;
		}
	}
}
