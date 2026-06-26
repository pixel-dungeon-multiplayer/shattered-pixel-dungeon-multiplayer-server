package com.shatteredpixel.shatteredpixeldungeon;

import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;

import java.util.HashSet;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.heroes;

public class HeroHelp {
    public static int HeroCount() {
        int count = 0;
        for (int i = 0; i < heroes.length; i++) {
            if ((!(heroes[i] == null)) && (heroes[i].isAlive())) {
                count++;
            }
        }
        return count;
    }

    public static Hero GetRandomHero() {
        HashSet<Hero> HeroesList = new HashSet<>();
        for (int i = 0; i < heroes.length; i++)
            if (heroes[i] != null && heroes[i].isAlive()) {
                HeroesList.add(heroes[i]);
            }

        return HeroesList.size() == 0 ? null : com.watabou.utils.Random.element(HeroesList);
    }

    public static int HeroesCountOnLevel(int depth) {
        return HeroCount();
        /*
        int count=0;
        for (int i=0;i<Settings.maxPlayers;i++){
            if (!(Dungeon.heroes[i]==null)){
            if (Dungeon.heroes[i].depth==depth){
                count++;
                }
            }
        }
        return  count;
         */
    }

    public static LocalizedString GetHeroesClass() {
        int count = HeroCount();
        if (count == 1) {
            for (int i = 0; i < heroes.length; i++) {
                if ((!(heroes[i] == null)) && (heroes[i].isAlive())) {
                    return heroes[i].className();
                }
            }
        }
        LocalizedString ClassName = null;
        if (count > 1) {

            for (int i = 0; i < heroes.length; i++) {
                if ((!(heroes[i] == null)) && (heroes[i].isAlive())) {
                    if (ClassName == null) {
                        ClassName = heroes[i].className();
                    } else {
                        if (ClassName != heroes[i].className())
                        {
                            return LocalizedString.raw("heroes");
                        }
                    }
                }
            }
            return LocalizedString.raw("%ss", ClassName, 's');
        }
        return LocalizedString.raw("ERROR");
    }

    public static Hero GetHeroOnLevel(int depth) { //use  this  if on level  only  one Hero
        return heroes[0];//fixme  This need to be other code
        // Dungeon.heroes[i]==depth?  return  Dungeon.heroes[i];
    }

    ;

    public static int getHeroID(Hero hero) {
        for (int i = 0; i < heroes.length; i++)
        {
            if (heroes[i] == hero){
                return i;
            }
        }
        return -1;
    }

    public static boolean haveAliveHero() {
        for (int i = 0; i < heroes.length; i++) {
            if (Dungeon.heroes[i] != null && heroes[i].isAlive()) {
                return true;
            }
        }
        return false;
    }
}
