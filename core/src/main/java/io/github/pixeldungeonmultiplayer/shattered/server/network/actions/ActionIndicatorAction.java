package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.gltextures.TextureSource;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Image;
import com.watabou.noosa.Visual;
import com.watabou.utils.RectF;
import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ActionIndicatorAction implements ImmutableNetworkAction {
	public final @Nullable LocalizedString displayName;
	public final @Nullable String actionId;
	public final int indicatorColor;
	public final @Nullable VisualData primaryVisual;
	public final @Nullable VisualData secondaryVisual;

	public ActionIndicatorAction(@Nullable ActionIndicator.Action action, @NotNull Hero hero) {
		if (action == null) {
			displayName = null;
			actionId = null;
			indicatorColor = 0;
			primaryVisual = null;
			secondaryVisual = null;
		} else {
			displayName = action.actionName();
			actionId = action.getClass().getName();
			indicatorColor = action.indicatorColor(hero);
			primaryVisual = VisualData.snapshot(action.primaryVisual(hero), action);
			secondaryVisual = VisualData.snapshot(action.secondaryVisual(hero), action);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ActionIndicatorAction)) return false;
		ActionIndicatorAction that = (ActionIndicatorAction) o;
		return indicatorColor == that.indicatorColor
				&& Objects.equals(displayName, that.displayName) && Objects.equals(actionId, that.actionId)
				&& Objects.equals(primaryVisual, that.primaryVisual)
				&& Objects.equals(secondaryVisual, that.secondaryVisual);
	}

	@Override
	public int hashCode() {
		return Objects.hash(displayName, actionId, indicatorColor, primaryVisual, secondaryVisual);
	}

	@Override
	public @NotNull String actionName() {
		return "action_indicator";
	}

	public static final class VisualData {
		public enum Kind { STATIC_IMAGE, ITEM_SPRITE, BITMAP_TEXT }

		public final Kind kind;
		public final @NotNull Visual visual;
		public final @Nullable TextureSource textureSource;
		public final @Nullable RectF frame;
		public final @Nullable String text;
		public final int itemImage;
		public final @Nullable ItemSprite.Glowing glowing;
		public final float width, height, scaleX, scaleY;
		public final float rm, gm, bm, ra, ga, ba;

		private VisualData(Kind kind, @NotNull Visual visual, @Nullable TextureSource textureSource,
				@Nullable RectF frame, @Nullable String text, int itemImage,
				@Nullable ItemSprite.Glowing glowing) {
			this.kind = kind;
			this.visual = visual;
			this.textureSource = textureSource;
			this.frame = frame == null ? null : new RectF(frame);
			this.text = text;
			this.itemImage = itemImage;
			this.glowing = glowing == null ? null : new ItemSprite.Glowing(glowing.color, glowing.period);
			width = visual.width;
			height = visual.height;
			scaleX = visual.scale.x;
			scaleY = visual.scale.y;
			rm = visual.rm; gm = visual.gm; bm = visual.bm;
			ra = visual.ra; ga = visual.ga; ba = visual.ba;
		}

		private static @Nullable VisualData snapshot(@Nullable Visual visual, ActionIndicator.Action action) {
			if (visual == null) return null;
			if (visual.getClass() == BitmapText.class) {
				BitmapText text = (BitmapText) visual;
				if (text.font() != PixelScene.pixelFont) {
					throw unsupported(visual, action, "unsupported bitmap font");
				}
				return new VisualData(Kind.BITMAP_TEXT, visual, null, null,
						text.text(), 0, null);
			}
			if (visual.getClass() == ItemSprite.class) {
				ItemSprite sprite = (ItemSprite) visual;
				return new VisualData(Kind.ITEM_SPRITE, visual, null, null, null,
						sprite.image(), sprite.glowing());
			}
			if (visual.getClass() == Image.class || visual.getClass() == HeroIcon.class) {
				Image image = (Image) visual;
				if (image.texture == null || image.texture.source == null) {
					throw unsupported(visual, action, "image has no serializable texture source");
				}
				return new VisualData(Kind.STATIC_IMAGE, visual, image.texture.source,
						image.frame(), null, 0, null);
			}
			throw unsupported(visual, action, "unsupported visual type");
		}

		private static IllegalArgumentException unsupported(Visual visual, ActionIndicator.Action action, String reason) {
			return new IllegalArgumentException(reason + ": " + visual.getClass().getName()
					+ " for ActionIndicator action " + action.getClass().getName());
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof VisualData)) return false;
			VisualData v = (VisualData) o;
			return kind == v.kind && Objects.equals(textureSource, v.textureSource)
					&& rectEquals(frame, v.frame) && Objects.equals(text, v.text) && itemImage == v.itemImage
					&& glowingEquals(glowing, v.glowing)
					&& Float.compare(width, v.width) == 0 && Float.compare(height, v.height) == 0
					&& Float.compare(scaleX, v.scaleX) == 0 && Float.compare(scaleY, v.scaleY) == 0
					&& Float.compare(rm, v.rm) == 0 && Float.compare(gm, v.gm) == 0
					&& Float.compare(bm, v.bm) == 0 && Float.compare(ra, v.ra) == 0
					&& Float.compare(ga, v.ga) == 0 && Float.compare(ba, v.ba) == 0;
		}

		private static boolean glowingEquals(ItemSprite.Glowing a, ItemSprite.Glowing b) {
			return a == b || a != null && b != null && a.color == b.color
					&& Float.compare(a.period, b.period) == 0;
		}

		private static boolean rectEquals(RectF a, RectF b) {
			return a == b || a != null && b != null && Float.compare(a.left, b.left) == 0
					&& Float.compare(a.top, b.top) == 0 && Float.compare(a.right, b.right) == 0
					&& Float.compare(a.bottom, b.bottom) == 0;
		}

		@Override
		public int hashCode() {
			return Objects.hash(kind, textureSource, frame == null ? 0 : frame.left,
					frame == null ? 0 : frame.top, frame == null ? 0 : frame.right,
					frame == null ? 0 : frame.bottom, text, itemImage,
					glowing == null ? 0 : glowing.color, glowing == null ? 0 : glowing.period,
					width, height, scaleX, scaleY, rm, gm, bm, ra, ga, ba);
		}
	}
}
