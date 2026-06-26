/*
 * Pixel Dungeon Multiplayer
 * Copyright (C) 2021-2023 Nikita Shaposhnikov
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
package io.github.pixeldungeonmultiplayer.shattered.server.ui;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.BannerSprites;
import io.github.pixeldungeonmultiplayer.shattered.server.network.SendData;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.ShowBannerAction;
import org.jetbrains.annotations.NotNull;

public class Banner {

    public static void show(@NotNull Hero actor, @NotNull BannerSprites.Type banner, int color, float fadeTime, float showTime) {
        SendData.sendAction(actor,new ShowBannerAction(banner, color, fadeTime, showTime));
    }

    public static void show(@NotNull Hero actor, @NotNull BannerSprites.Type banner, int color, float fadeTime) {
        show(actor, banner, color, fadeTime, Float.MAX_VALUE);
    }
}
