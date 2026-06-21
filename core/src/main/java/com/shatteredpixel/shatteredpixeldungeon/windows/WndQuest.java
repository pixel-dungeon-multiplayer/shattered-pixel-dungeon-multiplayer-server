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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.nikita22007.multiplayer.utils.text.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

public class WndQuest extends WndTitledMessage {

	private final String spriteName;
	private final LocalizedString charName;
	private final LocalizedString text;

	public WndQuest(NPC questgiver, String text, Hero hero) {
		this(questgiver, LocalizedString.raw(text), hero);
	}
	public WndQuest(NPC questgiver, LocalizedString text, Hero hero) {
		super(hero, questgiver.sprite(), Messages.titleCase( questgiver.name() ), text);
		this.spriteName = questgiver.getSprite().getClass().getName();
		this.charName = questgiver.name();
		this.text = text;
	}

	public String spriteName() {
		return spriteName;
	}

	public LocalizedString charName() {
		return charName;
	}

	public LocalizedString text() {
		return text;
	}
}
