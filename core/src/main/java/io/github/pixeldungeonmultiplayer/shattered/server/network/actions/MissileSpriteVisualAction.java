package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MissileSpriteVisualAction implements LiveStateNetworkAction {

    @NotNull public final MissileSprite.Anchor from;
    @NotNull public final MissileSprite.Anchor to;
    public final float speed;
    public final float angularSpeed;
    public final float angle;
    public final boolean flipHorizontal;
    @Nullable public final Item item;

    public MissileSpriteVisualAction(@NotNull MissileSprite.Anchor from, @NotNull MissileSprite.Anchor to,
                                     float speed, float angularSpeed, float angle, boolean flipHorizontal,
                                     @Nullable Item item) {
        this.from = from;
        this.to = to;
        this.speed = speed;
        this.angularSpeed = angularSpeed;
        this.angle = angle;
        this.flipHorizontal = flipHorizontal;
        this.item = item;
    }

    @Override
    public @NotNull String actionName() {
        return "missile_sprite_visual";
    }
}
