package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.dtos;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.utils.PointF;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BeamAnchor {
    public static final String TYPE_CELL = "cell";
    public static final String TYPE_RAISED_CELL = "raised_cell";
    public static final String TYPE_TARGET = "target";
    public static final String TYPE_TARGET_DESTINATION = "target_destination";

    private final @NotNull String type;
    private final @Nullable Integer cell;
    private final @Nullable Integer targetCharId;

    private final transient @Nullable CharSprite sprite;

    private BeamAnchor(@NotNull String type, @Nullable Integer cell, @Nullable Integer targetCharId, @Nullable CharSprite sprite) {
        this.type = type;
        this.cell = cell;
        this.targetCharId = targetCharId;
        this.sprite = sprite;
    }

    public static BeamAnchor cell(int cell) {
        return new BeamAnchor(TYPE_CELL, cell, null, null);
    }

    public static BeamAnchor raisedCell(int cell) {
        return new BeamAnchor(TYPE_RAISED_CELL, cell, null, null);
    }

    public static BeamAnchor target(@NotNull CharSprite sprite) {
        if (sprite.ch.id() == -1) throw new IllegalStateException("Target char has no network id");
        return new BeamAnchor(TYPE_TARGET, null, sprite.ch.id(), sprite);
    }

    public static BeamAnchor targetDestination(@NotNull CharSprite sprite) {
        if (sprite.ch.id() == -1) throw new IllegalStateException("Target char has no network id");
        return new BeamAnchor(TYPE_TARGET_DESTINATION, null, sprite.ch.id(), sprite);
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

    public PointF toPointF() {
        if (TYPE_CELL.equals(type)) {
            return DungeonTilemap.tileCenterToWorld(cell);
        } else if (TYPE_RAISED_CELL.equals(type)) {
            return DungeonTilemap.raisedTileCenterToWorld(cell);
        } else if (TYPE_TARGET.equals(type) || TYPE_TARGET_DESTINATION.equals(type)) {
            if (sprite != null) {
                return TYPE_TARGET_DESTINATION.equals(type) ? sprite.destinationCenter() : sprite.center();
            }
            Char ch = (Char) Actor.findById(targetCharId);
            if (ch != null && ch.getSprite() != null) {
                return TYPE_TARGET_DESTINATION.equals(type) ? ch.getSprite().destinationCenter() : ch.getSprite().center();
            }
            if (ch != null) {
                return DungeonTilemap.raisedTileCenterToWorld(ch.pos);
            }
            throw new IllegalStateException("No char with id " + targetCharId);
        }
        throw new IllegalStateException("Invalid beam anchor type");
    }
}
