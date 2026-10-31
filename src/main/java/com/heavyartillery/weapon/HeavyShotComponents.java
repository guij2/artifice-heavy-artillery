package com.heavyartillery.weapon;

import com.heavyartillery.HeavyArtillery;
import io.redspace.irons_artifice.data.ComponentType;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.Value;
import net.minecraft.resources.Identifier;

import java.util.Optional;

/**
 * Shot components added by Heavy Artillery, alongside the parent mod's {@link ShotComponents}.
 * <p>
 * Because these live in the gun's {@code ShotComponentMap}, they are resolved exactly like damage or spread:
 * the gun's template sets the base value, then every installed {@code GunModifier} may modify it, then
 * {@code ComposeShotEvent} handlers read the final value. That means any gun can gain a heavy trait through a
 * modifier, and any modifier can tune a heavy trait. For example, a modifier that halves the spin-up delay:
 * <pre>{@code
 * new ValueStackModifier(Map.of(
 *         HeavyShotComponents.SPIN_UP_DELAY,
 *         new ValueModifier(-0.5, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.HARMFUL)));
 * }</pre>
 * {@link io.redspace.irons_artifice.data.ValueModifier.Type} says whether an <i>increase</i> of the value is good
 * (BENEFICIAL) or bad (HARMFUL), which drives the tooltip color.
 * <p>
 * Tooltip names come from {@code artifice_heavy_artillery.component_type.<path>} in the lang file.
 */
public final class HeavyShotComponents {
    /* **********************************
     * Spin-up (see trait.SpinUp)
     ********************************** */
    /**
     * Extra fire delay, in ticks, while the barrels are at rest. Fades linearly to zero as the gun spins up. Increase is HARMFUL.
     */
    public static final ComponentType<Value> SPIN_UP_DELAY = new ComponentType<>(HeavyArtillery.id("spin_up_delay"), () -> Value.of(0));
    /**
     * Consecutive shots needed to reach full fire rate. Increase is HARMFUL.
     */
    public static final ComponentType<Value> SPIN_UP_SHOTS = new ComponentType<>(HeavyArtillery.id("spin_up_shots"), () -> Value.of(0));
    /**
     * Ticks the barrels keep spinning after the gun is ready to fire again, before the streak resets. Increase is BENEFICIAL.
     */
    public static final ComponentType<Value> SPIN_DOWN_GRACE = new ComponentType<>(HeavyArtillery.id("spin_down_grace"), () -> Value.of(4));

    /* **********************************
     * Bracing (see trait.Brace)
     ********************************** */
    /**
     * Fraction of spread removed while crouched on the ground, 0..1. Increase is BENEFICIAL.
     */
    public static final ComponentType<Value> BRACE_SPREAD_REDUCTION = new ComponentType<>(HeavyArtillery.id("brace_spread_reduction"), () -> Value.of(0));
    /**
     * Fraction of camera recoil removed while crouched on the ground, 0..1. Increase is BENEFICIAL.
     */
    public static final ComponentType<Value> BRACE_RECOIL_REDUCTION = new ComponentType<>(HeavyArtillery.id("brace_recoil_reduction"), () -> Value.of(0));

    /* **********************************
     * Tripod (see trait.TripodMount)
     ********************************** */
    /**
     * Fraction of spread removed while the gun is mounted on its tripod (crouching on the ground), 0..1. Stacks with
     * bracing. Increase is BENEFICIAL.
     */
    public static final ComponentType<Value> MOUNTED_SPREAD_REDUCTION = new ComponentType<>(HeavyArtillery.id("mounted_spread_reduction"), () -> Value.of(0));
    /**
     * Fraction of camera recoil removed while the gun is mounted on its tripod, 0..1. Increase is BENEFICIAL.
     */
    public static final ComponentType<Value> MOUNTED_RECOIL_REDUCTION = new ComponentType<>(HeavyArtillery.id("mounted_recoil_reduction"), () -> Value.of(0));
    /**
     * Set by the first Tripod Attachment Modifier on a gun, so further tripods add nothing.
     */
    public static final ComponentType<Boolean> TRIPOD_FITTED = new ComponentType<>(HeavyArtillery.id("tripod_fitted"), () -> false);

    /* **********************************
     * Reflex sight (see trait.SightAim)
     ********************************** */
    /**
     * Fraction of spread removed while aiming down the sight (holding use), 0..1. Increase is BENEFICIAL.
     */
    public static final ComponentType<Value> AIMED_SPREAD_REDUCTION = new ComponentType<>(HeavyArtillery.id("aimed_spread_reduction"), () -> Value.of(0));
    /**
     * Fraction of camera recoil removed while aiming down the sight, 0..1. Increase is BENEFICIAL.
     */
    public static final ComponentType<Value> AIMED_RECOIL_REDUCTION = new ComponentType<>(HeavyArtillery.id("aimed_recoil_reduction"), () -> Value.of(0));
    /**
     * Set by the first Reflex Sight Attachment Modifier on a gun, so further sights add nothing.
     */
    public static final ComponentType<Boolean> SIGHT_FITTED = new ComponentType<>(HeavyArtillery.id("sight_fitted"), () -> false);

    /* **********************************
     * Reserve ammo (see trait.ReserveAmmo)
     ********************************** */
    /**
     * Bullets from the shooter's inventory paid for each reloaded round: 1 normally, more for rounds made from several
     * bullets (a rocket), a fraction for a chance of each round being free. At 0 reloads are free and need no bullets at
     * all. Works on any gun, the parent mod's included. Increase is HARMFUL.
     */
    public static final ComponentType<Value> RESERVE_AMMO_COST = new ComponentType<>(HeavyArtillery.id("reserve_ammo_cost"), () -> Value.of(1));

    /* **********************************
     * Molten shot (see trait.MoltenShot)
     ********************************** */
    /**
     * Shots are globs of molten metal: block impacts splash and sizzle instead of chipping stone, and the gun's tooltip
     * shows how far the spray reaches. Pair it with a slow, high-drag shot.
     */
    public static final ComponentType<Boolean> MOLTEN = new ComponentType<>(HeavyArtillery.id("molten"), () -> false);
    /**
     * Chance, 0..1, that a shot sets fire to the block side it hits (like a fire charge; mobs need mob griefing). Works on
     * any gun. Increase is BENEFICIAL.
     */
    public static final ComponentType<Value> IGNITE_BLOCK_CHANCE = new ComponentType<>(HeavyArtillery.id("ignite_block_chance"), () -> Value.of(0));

    /* **********************************
     * Rockets (see trait.Blowback, client.ShotModelRenderer)
     ********************************** */
    /**
     * Item model drawn on each shot in flight, pointing the way it flies (the parent's bullets are invisible, only their
     * trail shows). Synced to clients with the bullet ({@code BulletMixin}).
     */
    public static final ComponentType<Optional<Identifier>> SHOT_MODEL = new ComponentType<>(HeavyArtillery.id("shot_model"), Optional::empty);
    /**
     * Multiplier on the parent's {@link ShotComponents#CHARACTER_BLOWBACK} (the shooter pushed back by the shot) while
     * the shooter is off the ground. 1 leaves it as it is. Increase is HARMFUL.
     */
    public static final ComponentType<Value> AIRBORNE_BLOWBACK = new ComponentType<>(HeavyArtillery.id("airborne_blowback"), () -> Value.of(1));

    /* **********************************
     * Magazine (see trait.MagazineSize)
     ********************************** */
    /**
     * Multiplier on the gun's magazine capacity, rounded up, so a single-shot gun at 1.5 holds 2. Works on any gun.
     * Increase is BENEFICIAL.
     */
    public static final ComponentType<Value> MAGAZINE_CAPACITY = new ComponentType<>(HeavyArtillery.id("magazine_capacity"), () -> Value.of(1));

    /* **********************************
     * Rifling (see trait.Rifling)
     ********************************** */
    /**
     * Damage bonus per block a shot has travelled when it hits, counting up to {@code trait.Rifling.MAX_DISTANCE}
     * blocks. Increase is BENEFICIAL.
     */
    public static final ComponentType<Value> RIFLING = new ComponentType<>(HeavyArtillery.id("rifling"), () -> Value.of(0));

    /* **********************************
     * Powder ram (see trait.PowderRam)
     ********************************** */
    /**
     * Damage bonus a shot gets when the shooter has not fired for {@code trait.PowderRam.CHARGE_TICKS}; less after a
     * shorter wait, in proportion. Increase is BENEFICIAL.
     */
    public static final ComponentType<Value> POWDER_RAM = new ComponentType<>(HeavyArtillery.id("powder_ram"), () -> Value.of(0));

    private HeavyShotComponents() {
    }
}
