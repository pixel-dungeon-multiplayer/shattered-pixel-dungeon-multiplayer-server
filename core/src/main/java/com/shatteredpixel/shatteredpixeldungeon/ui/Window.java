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

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.nikita22007.multiplayer.utils.Log;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.HeroHelp;
import com.shatteredpixel.shatteredpixeldungeon.SPDAction;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.ShadowBox;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.PointerArea;
import com.watabou.utils.Point;
import com.watabou.utils.Signal;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Window extends Group implements Signal.Listener<KeyEvent> {

    protected static final String HIDE_WINDOW_TYPE = "hide";

    protected int width;
	protected int height;

	protected int xOffset;
	protected int yOffset;
	
	protected PointerArea blocker;
	protected ShadowBox shadow;
	protected NinePatch chrome;

	public static final int WHITE = 0xFFFFFF;
	public static final int TITLE_COLOR = 0xFFFF44;
	public static final int SHPX_COLOR = 0x33BB33;
	private static final Map<Integer, Map<Integer, Window>> windows = new HashMap<>(SPDSettings.maxPlayers());
	private static final Map<Integer, Integer> idCounter = new HashMap<>(SPDSettings.maxPlayers()); // contains last used Window.id for each hero

	private Hero ownerHero;
	//Each window CURRENTLY open for ownerHero has a unique id. Two windows can have the same id only with different ownerHero.
	private int id;

	@Contract(pure = true)
	public final int getId() {
		return id;
	}

	@Contract(mutates = "this")
	private void setId(int id) {
		this.id = id;
	}

	@Contract(pure = true)
	public final Hero getOwnerHero() {
		return ownerHero;
	}

	@Contract(mutates = "this")
	protected void setOwnerHero(Hero ownerHero) {
		this.ownerHero = ownerHero;
	}
	public Window(Hero hero) {
		this();
		attachToHero(hero);
	}

	@Contract(pure = true)
	public static boolean hasWindow(Hero hero) {
		Map<Integer, Window> heroWindows = windows.getOrDefault(HeroHelp.getHeroID(hero), null);
		return (heroWindows != null) && !heroWindows.isEmpty();
	}

	@Contract(pure = true)
	public static Window getWindow(Class<? extends Window> wndClass, Hero hero) {
		Map<Integer, Window> heroWindows = windows.getOrDefault(HeroHelp.getHeroID(hero), null);
		if (heroWindows != null) {
			for (Window window : heroWindows.values()) {
				if (window.getClass().equals(wndClass)) {
					return window;
				}
			}
		}
		return null;
	}

	@Contract(pure = true)
	public static boolean hasWindow(Class<? extends Window> wndClass, Hero hero){
		return getWindow(wndClass, hero) != null;
	}

	@Contract(mutates = "this")
	protected synchronized final void attachToHero(Hero hero) {
		if (getId() > 0) {
			if (hero != getOwnerHero()) {
				throw new AssertionError("Attaching window to different heroes");
			}
			return;
		}
		if (hero == null) return;
		int heroId = HeroHelp.getHeroID(hero);

		setOwnerHero(hero);
		if (!idCounter.containsKey(heroId)) {
			idCounter.put(heroId, 0);
		}
		if (!windows.containsKey(heroId)) {
			windows.put(heroId, new HashMap<>(3));
		}
		setId(idCounter.get(heroId) + 1);
		idCounter.put(heroId, getId());
		windows.get(heroId).put(getId(), this);
	}

	private Window() {
		this( 0, 0, Chrome.get( Chrome.Type.WINDOW ) );
	}
	
	private Window( int width, int height ) {
		this( width, height, Chrome.get( Chrome.Type.WINDOW ) );
	}

	private Window( int width, int height, NinePatch chrome ) {
		super();
		
		blocker = new PointerArea( 0, 0, PixelScene.uiCamera.width, PixelScene.uiCamera.height ) {
			@Override
			protected void onClick( PointerEvent event ) {
				if (Window.this.parent != null && !Window.this.chrome.overlapsScreenPoint(
					(int) event.current.x,
					(int) event.current.y )) {
					
					onBackPressed();
				}
			}
		};
		blocker.camera = PixelScene.uiCamera;
		add( blocker );
		
		this.chrome = chrome;

		this.width = width;
		this.height = height;

		shadow = new ShadowBox();
		shadow.am = 0.5f;
		shadow.camera = PixelScene.uiCamera.visible ?
				PixelScene.uiCamera : Camera.main;
		add( shadow );

		chrome.x = -chrome.marginLeft();
		chrome.y = -chrome.marginTop();
		chrome.size(
			width - chrome.x + chrome.marginRight(),
			height - chrome.y + chrome.marginBottom() );
		add( chrome );
		
		camera = new Camera( 0, 0,
			(int)chrome.width,
			(int)chrome.height,
			PixelScene.defaultZoom );
		camera.x = (int)(Game.width - camera.width * camera.zoom) / 2;
		camera.y = (int)(Game.height - camera.height * camera.zoom) / 2;
		camera.y -= yOffset * camera.zoom;
		camera.scroll.set( chrome.x, chrome.y );
		Camera.add( camera );

		shadow.boxRect(
				camera.x / camera.zoom,
				camera.y / camera.zoom,
				chrome.width(), chrome.height );

		KeyEvent.addKeyListener( this );
	}
	
	public void resize( int w, int h ) {

	}
	public Window(Hero owner, int width, int height, NinePatch chrome ) {
		this(width, height, chrome);
		setOwnerHero(owner);
		attachToHero(owner);
	}

		public Point getOffset(){
		return new Point(xOffset, yOffset);
	}

	public final void offset( Point offset ){
		offset(offset.x, offset.y);
	}

	//windows with scroll panes will likely need to override this and refresh them when offset changes
	public void offset( int xOffset, int yOffset ){
		camera.x -= this.xOffset * camera.zoom;
		this.xOffset = xOffset;
		camera.x += xOffset * camera.zoom;

		camera.y -= this.yOffset * camera.zoom;
		this.yOffset = yOffset;
		camera.y += yOffset * camera.zoom;

		shadow.boxRect( camera.x / camera.zoom, camera.y / camera.zoom, chrome.width(), chrome.height );
	}

	//ensures the window, with offset, does not go beyond a given margin
	public void boundOffsetWithMargin( int margin ){
		float x = camera.x / camera.zoom;
		float y = camera.y / camera.zoom;

		Camera sceneCam = PixelScene.uiCamera.visible ? PixelScene.uiCamera : Camera.main;

		int newXOfs = xOffset;
		if (x < margin){
			newXOfs += margin - x;
		} else if (x + camera.width > sceneCam.width - margin){
			newXOfs += (sceneCam.width - margin) - (x + camera.width);
		}

		int newYOfs = yOffset;
		if (y < margin){
			newYOfs += margin - y;
		} else if (y + camera.height > sceneCam.height - margin){
			newYOfs += (sceneCam.height - margin) - (y + camera.height);
		}

		offset(newXOfs, newYOfs);
	}
	//TODO: check this
	public void hide() {
		destroy();
	}

	@MustBeInvokedByOverriders
	public void destroy() {
		if (getOwnerHero() != null) {
			Window removed = windows.get(HeroHelp.getHeroID(getOwnerHero())).remove(getId());
			if ((removed != null) && (removed != this)) {
				throw new AssertionError("Removed window is not current Window");
			}
		}
		super.destroy();
	}

	@Override
	public boolean onSignal( KeyEvent event ) {
		if (event.pressed) {
			if (KeyBindings.getActionForKey( event ) == SPDAction.BACK
				|| KeyBindings.getActionForKey( event ) == SPDAction.WAIT){
				onBackPressed();
			}
		}
		
		return true;
	}
	
	public void onBackPressed() {
		hide();
	}
	public static void OnButtonPressed(@NotNull Hero hero, int ID, int button, @Nullable JSONObject res) {
		final int heroId = HeroHelp.getHeroID(hero);
		Window window;
		try {
			window = windows.get(heroId).get(ID);
			Objects.requireNonNull(window);
		} catch (NullPointerException e) {
			Log.i("Window", "No such window.");
			return;
		}
		if (button == -1) {
			window.onBackPressed();
		} else {
			window.onSelect(button, res);
		}
	}
	public void onSelect(int button, @Nullable JSONObject args) {
		onSelect(button);
	}

	protected void onSelect(int button) {
	}
}
