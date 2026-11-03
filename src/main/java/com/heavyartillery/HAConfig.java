package com.heavyartillery;

import io.redspace.irons_artifice.data.FireMode;
import io.redspace.irons_artifice.data.RecoilProfile;
import io.redspace.irons_artifice.data.ShotComponentTemplate;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.Value;
import io.redspace.irons_artifice.gun.ArmPoseKind;
import io.redspace.irons_artifice.gun.GunProfile;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.function.Supplier;

/**
 * The weapons' and modifiers' balance numbers, for modpacks to tune, in
 * {@code config/artifice_heavy_artillery-server.toml}. A server config, so clients get the server's values on joining.
 * The defaults are the mod's own balance.
 * <p>
 * Values are read whenever a shot is composed rather than baked into the guns, so they apply to guns already made, and
 * as soon as the file is saved. Until the config loads (in the main menu), the defaults apply.
 */
public final class HAConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final Weapon GATLING;
    public static final ModConfigSpec.IntValue GATLING_SPIN_UP_DELAY;
    public static final ModConfigSpec.IntValue GATLING_SPIN_UP_SHOTS;

    public static final Weapon CRUCIBLE;
    public static final ModConfigSpec.IntValue CRUCIBLE_GLOBS;
    public static final ModConfigSpec.IntValue CRUCIBLE_BURN_SECONDS;

    public static final Weapon CONGREVE;
    public static final ModConfigSpec.IntValue CONGREVE_BULLETS_PER_ROCKET;
    public static final ModConfigSpec.IntValue CONGREVE_BLAST_RADIUS;

    public static final ModConfigSpec.DoubleValue DRUM_CAPACITY_BONUS;
    public static final ModConfigSpec.DoubleValue DRUM_RELOAD_PENALTY;

    public static final ModConfigSpec.DoubleValue RIFLED_DAMAGE_PER_BLOCK;
    public static final ModConfigSpec.IntValue RIFLED_MAX_DISTANCE;
    public static final ModConfigSpec.DoubleValue RIFLED_RELOAD_PENALTY;

    public static final ModConfigSpec.DoubleValue POWDER_RAM_DAMAGE_BONUS;
    public static final ModConfigSpec.IntValue POWDER_RAM_CHARGE_TICKS;

    public static final ModConfigSpec.DoubleValue SIGHT_SPREAD_REDUCTION;
    public static final ModConfigSpec.DoubleValue SIGHT_RECOIL_REDUCTION;
    public static final ModConfigSpec.DoubleValue SIGHT_AIMED_SPEED;

    public static final ModConfigSpec.DoubleValue TRIPOD_SPREAD_REDUCTION;
    public static final ModConfigSpec.DoubleValue TRIPOD_RECOIL_REDUCTION;

    public static final ModConfigSpec SPEC;

    static {
        GATLING = new Weapon("gunsteel_gatling", "Gunsteel Gatling: fire delay is at full spin", 7, 3.5, 2, 60, 60, 0.2, 8);
        GATLING_SPIN_UP_DELAY = BUILDER
                .comment("Extra ticks between shots from rest, fading as the barrels spin up")
                .defineInRange("spinUpDelay", 6, 0, 100);
        GATLING_SPIN_UP_SHOTS = BUILDER
                .comment("Shots to reach full fire rate")
                .defineInRange("spinUpShots", 10, 1, 100);
        BUILDER.pop();

        CRUCIBLE = new Weapon("gunsteel_crucible", "Gunsteel Crucible: damage is per glob, at full strength within 2 blocks", 7, 6, 2, 50, 60, 0.15, 8);
        CRUCIBLE_GLOBS = BUILDER
                .comment("Globs of molten metal per shot")
                .defineInRange("globsPerShot", 3, 1, 20);
        CRUCIBLE_BURN_SECONDS = BUILDER
                .comment("Seconds a hit target burns")
                .defineInRange("burnSeconds", 3, 0, 60);
        BUILDER.pop();

        CONGREVE = new Weapon("gunsteel_congreve", "Gunsteel Congreve: damage is the rocket's hit, and the burst's at its center", 80, 1.5, 10, 1, 70, 0.15, 8);
        CONGREVE_BULLETS_PER_ROCKET = BUILDER
                .comment("Bullets packed into each rocket on reload")
                .defineInRange("bulletsPerRocket", 8, 0, 64);
        CONGREVE_BLAST_RADIUS = BUILDER
                .comment("Burst radius in blocks; each Blackpowder Charge modifier adds 1. Block damage (with Breach) stays the same whatever the damage")
                .defineInRange("blastRadius", 4, 3, 16);
        BUILDER.pop();

        BUILDER.comment("Modifiers. Fractions are written as 0.5 for 50%").push("modifiers");

        BUILDER.comment("Drum Magazine: each one fitted multiplies these again").push("drum_magazine");
        DRUM_CAPACITY_BONUS = BUILDER
                .comment("Extra magazine capacity, as a fraction (rounded up)")
                .defineInRange("capacityBonus", 0.5, 0, 10);
        DRUM_RELOAD_PENALTY = BUILDER
                .comment("Fraction of reload speed lost")
                .defineInRange("reloadPenalty", 0.2, 0, 0.9);
        BUILDER.pop();

        BUILDER.comment("Rifled Barrel: each one fitted adds these again").push("rifled_barrel");
        RIFLED_DAMAGE_PER_BLOCK = BUILDER
                .comment("Extra damage per block the shot has flown, as a fraction")
                .defineInRange("damagePerBlock", 0.02, 0, 1);
        RIFLED_MAX_DISTANCE = BUILDER
                .comment("Blocks of flight that count towards the bonus")
                .defineInRange("maxDistance", 30, 1, 256);
        RIFLED_RELOAD_PENALTY = BUILDER
                .comment("Fraction of reload speed lost")
                .defineInRange("reloadPenalty", 0.15, 0, 0.9);
        BUILDER.pop();

        BUILDER.comment("Powder Ram: each one fitted adds its bonus and its charge time again").push("powder_ram");
        POWDER_RAM_DAMAGE_BONUS = BUILDER
                .comment("Extra damage when fully charged, as a fraction")
                .defineInRange("damageBonus", 0.5, 0, 10);
        POWDER_RAM_CHARGE_TICKS = BUILDER
                .comment("Ticks without shooting to charge fully (20 ticks = 1 second)")
                .defineInRange("chargeTime", 30, 1, 1200);
        BUILDER.pop();

        BUILDER.comment("Reflex Sight: only one per gun counts").push("reflex_sight");
        SIGHT_SPREAD_REDUCTION = BUILDER
                .comment("Fraction of spread removed while aiming")
                .defineInRange("spreadReduction", 0.3, 0, 1);
        SIGHT_RECOIL_REDUCTION = BUILDER
                .comment("Fraction of camera recoil removed while aiming")
                .defineInRange("recoilReduction", 0.3, 0, 1);
        SIGHT_AIMED_SPEED = BUILDER
                .comment("Walking speed while aiming, as a fraction of normal (using an item is 0.2)")
                .defineInRange("aimedSpeed", 0.6, 0, 1);
        BUILDER.pop();

        BUILDER.comment("Portable Tripod: only one per gun counts").push("portable_tripod");
        TRIPOD_SPREAD_REDUCTION = BUILDER
                .comment("Fraction of spread removed while mounted")
                .defineInRange("spreadReduction", 0.75, 0, 1);
        TRIPOD_RECOIL_REDUCTION = BUILDER
                .comment("Fraction of camera recoil removed while mounted")
                .defineInRange("recoilReduction", 0.75, 0, 1);
        BUILDER.pop();

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    /** The value from the config, or its default before the config has loaded. */
    public static <T> T get(ModConfigSpec.ConfigValue<T> value) {
        return SPEC.isLoaded() ? value.get() : value.getDefault();
    }

    /** A shot component read from the config each time a shot is composed. */
    public static Supplier<Value> live(ModConfigSpec.ConfigValue<? extends Number> value) {
        return () -> Value.of(get(value).doubleValue());
    }

    /**
     * The numbers every heavy weapon has, in a section of its own; the weapon's own numbers follow in the same section,
     * so the caller pops it.
     */
    public static final class Weapon {
        public final ModConfigSpec.DoubleValue damage;
        public final ModConfigSpec.DoubleValue spread;
        public final ModConfigSpec.IntValue fireDelay;
        public final ModConfigSpec.IntValue magazineCapacity;
        public final ModConfigSpec.IntValue reloadTime;
        public final ModConfigSpec.DoubleValue movementPenalty;
        public final ModConfigSpec.IntValue modifierSlots;

        private Weapon(String name, String comment, double damage, double spread, int fireDelay, int magazineCapacity, int reloadTime,
                       double movementPenalty, int modifierSlots) {
            BUILDER.comment(comment).push(name);
            this.damage = BUILDER
                    .comment("Damage per shot")
                    .defineInRange("damage", damage, 0, 10000);
            this.spread = BUILDER
                    .comment("Spread in degrees")
                    .defineInRange("spread", spread, 0, 90);
            this.fireDelay = BUILDER
                    .comment("Ticks between shots (20 ticks = 1 second)")
                    .defineInRange("fireDelay", fireDelay, 1, 1200);
            this.magazineCapacity = BUILDER
                    .comment("Rounds per magazine")
                    .defineInRange("magazineCapacity", magazineCapacity, 1, 10000);
            this.reloadTime = BUILDER
                    .comment("Reload time in ticks (20 ticks = 1 second)")
                    .defineInRange("reloadTime", reloadTime, 1, 1200);
            this.movementPenalty = BUILDER
                    .comment("Fraction (0-1) of walking speed lost while held")
                    .defineInRange("movementPenalty", movementPenalty, 0, 1);
            this.modifierSlots = BUILDER
                    .comment("Modifier slots. When lowered, modifiers in the removed slots go back to the player when they next open the gun's modifier menu")
                    .defineInRange("modifierSlots", modifierSlots, 1, 24);
        }

        /**
         * The parent's gun builder. Capacity, slots and reload time stay at their defaults in the profile: everything
         * that reads them goes through {@code trait.MagazineSize}, {@code trait.ModifierSlots} and
         * {@code HeavyGunItem.reloadTime}, which use the config.
         */
        public GunProfile.Builder profile(FireMode fireMode, ArmPoseKind armPoseKind, ShotComponentTemplate template) {
            return GunProfile.builder(magazineCapacity.getDefault(), modifierSlots.getDefault(), reloadTime.getDefault(), fireMode, armPoseKind, template);
        }

        /** The parent's shot template builder, with damage, spread and fire delay read from the config. */
        public ShotComponentTemplate.Builder template(double characterBlowback, RecoilProfile cameraRecoil) {
            return ShotComponentTemplate.builder(damage.getDefault(), spread.getDefault(), characterBlowback, fireDelay.getDefault(), cameraRecoil)
                    .set(ShotComponents.DAMAGE, live(damage))
                    .set(ShotComponents.SPREAD, live(spread))
                    .set(ShotComponents.FIRE_DELAY, live(fireDelay));
        }
    }

    private HAConfig() {
    }
}
