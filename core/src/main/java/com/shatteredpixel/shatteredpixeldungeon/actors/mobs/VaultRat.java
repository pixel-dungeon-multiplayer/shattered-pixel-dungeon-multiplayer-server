package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite;
import com.watabou.utils.Random;

public class VaultRat extends VaultMob {

	{
		spriteClass = RatSprite.class;

		HP = HT = 8;
		defenseSkill = 2;

		maxLvl = -2;
	}

	@Override
	public int damageRoll() {
		return 0;
	}

	@Override
	public int attackSkill(Char target) {
		return 8;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 1);
	}

	@Override
	public LocalizedString name() {
		return Messages.get(Rat.class, "name");
	}

	@Override
	public LocalizedString description() {
		return LocalizedString.concat(Messages.get(Rat.class, "desc"), "\n\n", super.description());
	}
}
