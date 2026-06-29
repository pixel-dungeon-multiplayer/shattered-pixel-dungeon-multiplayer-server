package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.dtos;

import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FloatingTextAnchor {
    public static final String TYPE_RAISED_CELL = "raised_cell";
    public static final String TYPE_TARGET_DESTINATION = "target_destination";

    private final @NotNull String type;
    private final @Nullable Integer cell;
    private final @Nullable Integer targetCharId;

    private FloatingTextAnchor(@NotNull String type, @Nullable Integer cell, @Nullable Integer targetCharId) {
        this.type = type;
        this.cell = cell;
        this.targetCharId = targetCharId;
    }

    public static FloatingTextAnchor raisedCell(int cell) {
        return new FloatingTextAnchor(TYPE_RAISED_CELL, cell, null);
    }

    public static FloatingTextAnchor targetDestination(@NotNull CharSprite sprite) {
        if (sprite.ch.id() == -1) throw new IllegalStateException("Target char has no network id");
        return new FloatingTextAnchor(TYPE_TARGET_DESTINATION, null, sprite.ch.id());
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
}
