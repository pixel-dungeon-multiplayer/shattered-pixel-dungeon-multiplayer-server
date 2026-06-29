package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.dtos;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.utils.PointF;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LightningAnchor {
    public static final String TYPE_CELL = "cell";
    public static final String TYPE_RAISED_CELL = "raised_cell";
    public static final String TYPE_TARGET = "target";
    public static final String TYPE_TARGET_DESTINATION = "target_destination";
    public static final String TYPE_TARGET_POINT = "target_point";

    private final @NotNull String type;
    private final @Nullable Integer cell;
    private final @Nullable Integer targetCharId;
    private final float xFactor;
    private final float yFactor;
    private final float shiftX;
    private final float shiftY;

    private final transient @Nullable CharSprite sprite;

    private LightningAnchor(@NotNull String type, @Nullable Integer cell, @Nullable Integer targetCharId,
                            float xFactor, float yFactor, float shiftX, float shiftY, @Nullable CharSprite sprite) {
        this.type = type;
        this.cell = cell;
        this.targetCharId = targetCharId;
        this.xFactor = xFactor;
        this.yFactor = yFactor;
        this.shiftX = shiftX;
        this.shiftY = shiftY;
        this.sprite = sprite;
    }

    public static LightningAnchor cell(int cell) {
        return new LightningAnchor(TYPE_CELL, cell, null, 0f, 0f, 0f, 0f, null);
    }

    public static LightningAnchor raisedCell(int cell) {
        return new LightningAnchor(TYPE_RAISED_CELL, cell, null, 0f, 0f, 0f, 0f, null);
    }

    public static LightningAnchor target(@NotNull CharSprite sprite) {
        if (sprite.ch.id() == -1) throw new IllegalStateException("Target char has no network id");
        return new LightningAnchor(TYPE_TARGET, null, sprite.ch.id(), 0f, 0f, 0f, 0f, sprite);
    }

    public static LightningAnchor targetDestination(@NotNull CharSprite sprite) {
        if (sprite.ch.id() == -1) throw new IllegalStateException("Target char has no network id");
        return new LightningAnchor(TYPE_TARGET_DESTINATION, null, sprite.ch.id(), 0f, 0f, 0f, 0f, sprite);
    }

    public static LightningAnchor targetPoint(@NotNull CharSprite sprite, float xFactor, float yFactor) {
        return targetPoint(sprite, xFactor, yFactor, 0f, 0f);
    }

    public static LightningAnchor targetPoint(@NotNull CharSprite sprite, float xFactor, float yFactor, float shiftX, float shiftY) {
        if (sprite.ch.id() == -1) throw new IllegalStateException("Target char has no network id");
        return new LightningAnchor(TYPE_TARGET_POINT, null, sprite.ch.id(), xFactor, yFactor, shiftX, shiftY, sprite);
    }

    public @NotNull String type() {
        return type;
    }

    public @Nullable Integer cell() {
        return cell;
    }

    public @Nullable Integer targetCharId() {
        return targetCharId;
    }

    public float xFactor() {
        return xFactor;
    }

    public float yFactor() {
        return yFactor;
    }

    public float shiftX() {
        return shiftX;
    }

    public float shiftY() {
        return shiftY;
    }

    public PointF toPointF() {
        if (TYPE_CELL.equals(type)) {
            return DungeonTilemap.tileCenterToWorld(cell);
        } else if (TYPE_RAISED_CELL.equals(type)) {
            return DungeonTilemap.raisedTileCenterToWorld(cell);
        } else if (TYPE_TARGET.equals(type) || TYPE_TARGET_DESTINATION.equals(type) || TYPE_TARGET_POINT.equals(type)) {
            CharSprite resolvedSprite = sprite;
            if (resolvedSprite == null) {
                Char ch = (Char) Actor.findById(targetCharId);
                if (ch != null && ch.getSprite() != null) {
                    resolvedSprite = ch.getSprite();
                } else if (ch != null) {
                    return DungeonTilemap.raisedTileCenterToWorld(ch.pos);
                } else {
                    throw new IllegalStateException("No char with id " + targetCharId);
                }
            }
            if (TYPE_TARGET_DESTINATION.equals(type)) {
                return resolvedSprite.destinationCenter();
            } else if (TYPE_TARGET_POINT.equals(type)) {
                return new PointF(
                        resolvedSprite.x + resolvedSprite.width() * xFactor + shiftX * resolvedSprite.scale.x,
                        resolvedSprite.y + resolvedSprite.height() * yFactor + shiftY * resolvedSprite.scale.y);
            }
            return resolvedSprite.center();
        }
        throw new IllegalStateException("Invalid lightning anchor type");
    }
}
