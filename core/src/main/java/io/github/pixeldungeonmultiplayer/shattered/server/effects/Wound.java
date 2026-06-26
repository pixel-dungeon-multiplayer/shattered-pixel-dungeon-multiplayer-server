/*
 * Pixel Dungeon
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
package io.github.pixeldungeonmultiplayer.shattered.server.effects;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import io.github.pixeldungeonmultiplayer.shattered.server.network.SendData;
import org.jetbrains.annotations.Contract;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.WoundVisualAction;

public class Wound {

    private static final float TIME_TO_FADE = 0.8f;
    @Deprecated
    @Contract(value = " -> fail", pure = true)
    protected Wound() {
        throw new RuntimeException();
    }

    public static void reset(int p) {
        SendData.sendActionForAll(new WoundVisualAction(p, TIME_TO_FADE));
    }

    public static void hit(Char ch) {
        reset(ch.pos);
    }

    public static void hit(int pos) {
        reset(pos);
    }
}
