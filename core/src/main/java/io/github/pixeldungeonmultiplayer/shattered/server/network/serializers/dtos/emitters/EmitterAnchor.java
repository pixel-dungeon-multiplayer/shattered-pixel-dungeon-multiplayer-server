package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.dtos.emitters;

import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class EmitterAnchor {

	public static final String TYPE_WORLD = "world";
	public static final String TYPE_CELL = "cell";
	public static final String TYPE_TARGET = "target";

	private final String type;
	@Nullable
	private final Integer cell;
	@Nullable
	private final Integer targetCharId;
	private final float x;
	private final float y;
	private final float width;
	private final float height;
	private final float shiftX;
	private final float shiftY;
	private final boolean fillTarget;

	private EmitterAnchor(@NotNull String type, @Nullable Integer cell, @Nullable Integer targetCharId,
						  float x, float y, float width, float height,
						  float shiftX, float shiftY, boolean fillTarget) {
		this.type = Objects.requireNonNull(type);
		this.cell = cell;
		this.targetCharId = targetCharId;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.shiftX = shiftX;
		this.shiftY = shiftY;
		this.fillTarget = fillTarget;
	}

	@NotNull
	public static EmitterAnchor world(float x, float y, float width, float height, float shiftX, float shiftY) {
		return new EmitterAnchor(TYPE_WORLD, null, null, x, y, width, height, shiftX, shiftY, true);
	}

	@NotNull
	public static EmitterAnchor cell(int cell, float x, float y, float width, float height, float shiftX, float shiftY) {
		return new EmitterAnchor(TYPE_CELL, cell, null, x, y, width, height, shiftX, shiftY, true);
	}

	@NotNull
	public static EmitterAnchor target(@NotNull CharSprite target, float x, float y, float width, float height,
									   float shiftX, float shiftY, boolean fillTarget) {
		Objects.requireNonNull(target);
		Objects.requireNonNull(target.ch);
		if (target.ch.id() == -1) throw new IllegalStateException("Target char has no network id");
		return new EmitterAnchor(TYPE_TARGET, null, target.ch.id(), x, y, width, height, shiftX, shiftY, fillTarget);
	}

	@NotNull
	public String type() {
		return type;
	}

	@Nullable
	public Integer cell() {
		return cell;
	}

	@Nullable
	public Integer targetCharId() {
		return targetCharId;
	}

	public float x() {
		return x;
	}

	public float y() {
		return y;
	}

	public float width() {
		return width;
	}

	public float height() {
		return height;
	}

	public float shiftX() {
		return shiftX;
	}

	public float shiftY() {
		return shiftY;
	}

	public boolean fillTarget() {
		return fillTarget;
	}
}
