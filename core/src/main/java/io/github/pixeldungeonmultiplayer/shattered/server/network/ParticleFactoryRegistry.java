package io.github.pixeldungeonmultiplayer.shattered.server.network;

import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BloodParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ChallengeParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.CorrosionParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.EarthParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ElmoParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.EnergyParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlowParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.PitfallParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.PoisonParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.PurpleParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.RainbowParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SacrificialParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShaftParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SnowParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SpectralWallParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.WebParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.WindParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.WoolParticle;
import com.shatteredpixel.shatteredpixeldungeon.levels.CavesBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.CityLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.SewerLevel;
import com.shatteredpixel.shatteredpixeldungeon.particles.Emitter;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GooSprite;
import com.watabou.noosa.particles.SerializableParticleFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public class ParticleFactoryRegistry {

    private static final Map<SerializableParticleFactory, String> objectNames = new IdentityHashMap<>();
    private static final Map<Class<? extends SerializableParticleFactory>, String> classNames = new HashMap<>();
    private static final Set<Class<?>> loggedMissingFactories = new HashSet<>();

    static {
        register(Speck.SpeckFactory.class, "speck");
        register(Splash.SplashFactory.class, "splash");

        register(BlastParticle.FACTORY, "blast");
        register(BloodParticle.FACTORY, "blood");
        register(BloodParticle.BURST, "blood_burst");
        register(ChallengeParticle.FACTORY, "challenge");
        register(CorrosionParticle.MISSILE, "corrosion_missile");
        register(CorrosionParticle.SPLASH, "corrosion_splash");
        register(EarthParticle.FACTORY, "earth");
        register(EarthParticle.SMALL, "earth_small");
        register(EarthParticle.FALLING, "earth_falling");
        register(ElmoParticle.FACTORY, "elmo");
        register(EnergyParticle.FACTORY, "energy");
        register(FlameParticle.FACTORY, "flame");
        register(FlowParticle.FACTORY, "flow");
        register(LeafParticle.GENERAL, "leaf_general");
        register(LeafParticle.LEVEL_SPECIFIC, "leaf_level_specific");
        register(PitfallParticle.FACTORY4, "pitfall4");
        register(PitfallParticle.FACTORY8, "pitfall8");
        register(PoisonParticle.MISSILE, "poison_missile");
        register(PoisonParticle.SPLASH, "poison_splash");
        register(PurpleParticle.MISSILE, "purple_missile");
        register(PurpleParticle.BURST, "purple_burst");
        register(RainbowParticle.BURST, "rainbow_burst");
        register(SacrificialParticle.FACTORY, "sacrificial");
        register(ShadowParticle.MISSILE, "shadow_missile");
        register(ShadowParticle.CURSE, "shadow_curse");
        register(ShadowParticle.UP, "shadow_up");
        register(ShaftParticle.FACTORY, "shaft");
        register(SmokeParticle.FACTORY, "smoke");
        register(SmokeParticle.SPEW, "smoke_spew");
        register(SnowParticle.FACTORY, "snow");
        register(SparkParticle.FACTORY, "spark");
        register(SparkParticle.STATIC, "spark_static");
        register(WebParticle.FACTORY, "web");
        register(WindParticle.FACTORY, "wind");
        register(WoolParticle.FACTORY, "wool");
        register(GooSprite.GooParticle.FACTORY, "goo");

        register(MagicMissile.MagicParticle.FACTORY, "magic_particle");
        register(MagicMissile.MagicParticle.ATTRACTING, "magic_particle_attracting");
        register(MagicMissile.EarthParticle.FACTORY, "magic_earth_particle");
        register(MagicMissile.EarthParticle.BURST, "magic_earth_particle_burst");
        register(MagicMissile.EarthParticle.ATTRACT, "magic_earth_particle_attract");
        register(MagicMissile.ShamanParticle.RED, "shaman_particle_red");
        register(MagicMissile.ShamanParticle.BLUE, "shaman_particle_blue");
        register(MagicMissile.ShamanParticle.PURPLE, "shaman_particle_purple");
        register(MagicMissile.WhiteParticle.FACTORY, "white_particle");
        register(MagicMissile.WhiteParticle.YELLOW, "white_particle_yellow");
        register(MagicMissile.WhiteParticle.WALL, "white_particle_wall");
        register(MagicMissile.SlowParticle.FACTORY, "slow_particle");
        register(MagicMissile.ForceParticle.FACTORY, "force_particle");
        register(MagicMissile.WardParticle.FACTORY, "ward_particle");
        register(MagicMissile.WardParticle.UP, "ward_particle_up");
        register(SpectralWallParticle.FACTORY, "spectral_wall");

        register(CityLevel.GreenFlame.factory, "city_green_flame");
        register(CityLevel.Smoke.factory, "city_smoke");
        register(SewerLevel.Sink.factory, "sewer_sink");
        register(CavesBossLevel.PylonEnergy.DIRECTED_SPARKS, "pylon_energy_directed_sparks");

        // TODO: StaffParticleFactory depends on a particular MagesStaff.wand instance,
        // so it needs a dedicated serializer with enough wand state to rebuild client FX.
        // register(MagesStaff.StaffParticleFactory, "staff_particle");
    }

    public static void register(Emitter.Factory factory, String name) {
        registerObject(factory, name);
    }

    public static void register(Class<? extends SerializableParticleFactory> factoryClass, String name) {
        registerClass(factoryClass, name);
    }

    public static String resolve(SerializableParticleFactory factory) {
        String name = objectNames.get(factory);
        if (name != null) {
            return name;
        }
        name = classNames.get(factory.getClass());
        if (name == null) {
            logMissingFactory(factory);
        }
        return name;
    }

    private static void registerObject(SerializableParticleFactory factory, String name) {
        objectNames.put(factory, name);
    }

    private static void registerClass(Class<? extends SerializableParticleFactory> factoryClass, String name) {
        classNames.put(factoryClass, name);
    }

    private static void logMissingFactory(SerializableParticleFactory factory) {
        Class<?> factoryClass = factory.getClass();
        if (!loggedMissingFactories.add(factoryClass)) {
            return;
        }
        System.err.println("Lost particle factory: " + factoryClass.getName());
        new Exception("Lost particle factory stacktrace").printStackTrace();
    }
}
