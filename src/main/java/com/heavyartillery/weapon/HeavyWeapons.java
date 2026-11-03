package com.heavyartillery.weapon;

import com.heavyartillery.HAConfig;
import com.heavyartillery.HeavyArtillery;
import com.heavyartillery.registry.HAParticles;
import io.redspace.irons_artifice.client.sounds.GunShotSoundSettings;
import io.redspace.irons_artifice.data.FireCycleCue;
import io.redspace.irons_artifice.data.FireCycleCueStack;
import io.redspace.irons_artifice.data.FireMode;
import io.redspace.irons_artifice.data.ImpactSoundStack;
import io.redspace.irons_artifice.data.MuzzleFlashType;
import io.redspace.irons_artifice.data.ParticleStack;
import io.redspace.irons_artifice.data.PlayableSound;
import io.redspace.irons_artifice.data.RecoilProfile;
import io.redspace.irons_artifice.data.ReloadCue;
import io.redspace.irons_artifice.data.ReloadCueStack;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.Value;
import io.redspace.irons_artifice.gun.ArmPoseKind;
import io.redspace.irons_artifice.gun.GunProfile;
import io.redspace.irons_artifice.item.animation_adjuster.AnimationAdjuster;
import io.redspace.irons_artifice.modifier.on_hit_handlers.BlackpowderChargeOnHit;
import io.redspace.irons_artifice.modifier.on_hit_handlers.IgnitePostHit;
import io.redspace.irons_artifice.registry.SoundRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Weapon definitions, in the same shape as the parent mod's {@code io.redspace.irons_artifice.gun.Guns}.
 * <p>
 * To add a weapon: define a {@link GunProfile} (plus {@link HeavyTraits}) here, register a {@link HeavyGunItem} in
 * {@code HAItems}, and add the GeckoLib model/animation/texture named after the item.
 * <p>
 * Balance: heavy weapons are late-game guns, meant to rival other mods' end-game weaponry (LTX Industries' Stargazer
 * and Nova) once filled with a good build, and to be weaker than those bare. So they get 8 modifier slots by default
 * (the parent's guns have 5 to 7), and a bare one roughly matches a fully modded gun of the parent's. README's Balance
 * section has the numbers.
 * <p>
 * The numbers modpacks may want to change (damage, spread, fire rate, capacity, reload time, movement penalty, modifier
 * slots and each weapon's own) come from {@link HAConfig}, which holds their defaults; the rest are here.
 * <p>
 * Reload cue times are in seconds into the reload animation; fire cycle cue times are fractions (0..1) of the fire delay.
 */
public final class HeavyWeapons {
    /**
     * Gunsteel Gatling: hand-cranked Gatling gun. High fire rate once spun up, a big magazine and a slow reload. Slows the wielder and
     * rewards crouching to brace it.
     * <p>
     * Fire rate: 2 ticks between shots (10/s) at full spin; +6 ticks (2.5/s) from rest, reaching full speed over 10 shots.
     */
    public static final GunProfile GUNSTEEL_GATLING = HAConfig.GATLING.profile(FireMode.AUTO, ArmPoseKind.RIFLE,
                    HAConfig.GATLING.template(0.03, RecoilProfile.of(4f, .6f, 0.9f, 1862))
                            .set(HeavyShotComponents.SPIN_UP_DELAY, HAConfig.live(HAConfig.GATLING_SPIN_UP_DELAY))
                            .set(HeavyShotComponents.SPIN_UP_SHOTS, HAConfig.live(HAConfig.GATLING_SPIN_UP_SHOTS))
                            .value(HeavyShotComponents.BRACE_SPREAD_REDUCTION, 0.5)
                            .value(HeavyShotComponents.BRACE_RECOIL_REDUCTION, 0.4)
                            .gunshotSound(
                                    GunShotSoundSettings.standardShot(SoundRegistry.CLOCKWORK_RIFLE_SHOOT, 0.85f),
                                    GunShotSoundSettings.standardEcho(SoundRegistry.BULLET_ECHO_GENERIC, 0.9f),
                                    PlayableSound.holder(SoundEvents.DISPENSER_FAIL))
                            .muzzleFlash(MuzzleFlashType.TRIANGLE, MuzzleFlashType.SMALL_STAR)
                            .build())
            .reloadCues(ReloadCueStack.of(
                    new ReloadCue(0.40f, PlayableSound.of(SoundRegistry.CLOCKWORK_RIFLE_EJECT_MAG, 1.25f, 0.75f, 0.85f)),
                    // hopper seats at 1.8333s, crank winds 2.08-2.58s (see gunsteel_gatling reload animation)
                    new ReloadCue(1.83f, PlayableSound.of(SoundRegistry.CLOCKWORK_RIFLE_INSERT_MAG, 1.25f, 0.75f, 0.85f)),
                    new ReloadCue(2.45f, PlayableSound.of(SoundRegistry.COCK_HAMMER, 1.25f, 0.7f, 0.8f))
            ))
            .equipSound(PlayableSound.of(SoundRegistry.CLOCKWORK_RIFLE_EQUIP, 0.75f, 0.7f, 0.8f))
            .fireCycleCues(FireCycleCueStack.of(
                    // ratchet click of the crank
                    new FireCycleCue(0.5f, PlayableSound.of(SoundRegistry.COCK_HAMMER, 0.35f, 1.5f, 1.7f))
            ))
            .animationAdjusters(HeavyHoldAdjuster.TILT)
            .build();

    public static final HeavyTraits GUNSTEEL_GATLING_TRAITS = HeavyTraits.builder()
            .sprintLockedWhileFiring()
            // left hand along the top carry handle (between its posts); on reload it turns the side crank (2.08-2.58s, as the animation does)
            .hold(new HeavyHold(new Vec3(0, 12, -10), new Vec3(0, 12, -4), new Vec3(0, 3.5, -32),
                    new HeavyHold.Crank(new Vec3(-4.5, 3.5, -7), new Vec3(-7, 7.5, -7), 2.0833, 2.5833)))
            .build();

    /**
     * Gunsteel Crucible: flamethrower. Bullets fed from the capsule slung under the barrel are melted down in its boiler,
     * and the pressure built up with its valve wheel drives the molten slag out of the shroud in a short, burning spray.
     * <p>
     * Each shot is 3 slow, high-drag globs that pierce one target and set it alight; they stop after about 7 blocks. As
     * damage scales with speed (Bullet.resolveDamage: full at 2 blocks/tick and up), they hit at full strength within 2
     * blocks and fade from there. 10 shots/s, 5 s of fire per load. It leaves blocks alone, unless a Breaching Shell
     * lets it set fire to the burnable ones it hits (trait.MoltenShot).
     */
    public static final GunProfile GUNSTEEL_CRUCIBLE = HAConfig.CRUCIBLE.profile(FireMode.AUTO, ArmPoseKind.RIFLE,
                    HAConfig.CRUCIBLE.template(0, RecoilProfile.of(1.2f, .5f, 0.35f, 451))
                            .set(ShotComponents.PROJECTILE_COUNT, HAConfig.live(HAConfig.CRUCIBLE_GLOBS))
                            .bulletSpeed(2.0)
                            .gravity(0.02)
                            .knockback(0)
                            .value(ShotComponents.BULLET_DRAG, 0.75)
                            // quenched at once underwater
                            .value(ShotComponents.UNDERWATER_DRAG, 0.2)
                            .value(ShotComponents.PIERCING, 1)
                            .set(HeavyShotComponents.MOLTEN, () -> true)
                            .modify(map -> map.getOrCreate(ShotComponents.POST_HIT_EFFECTS)
                                    .getOrCreate(IgnitePostHit.class, () -> new IgnitePostHit(0))
                                    .addDuration(HAConfig.get(HAConfig.CRUCIBLE_BURN_SECONDS) * 20))
                            // the billows smoke as they cool (no trail accents: those spawn right at the user's eye)
                            .set(ShotComponents.PARTICLE_TRAIL, () -> {
                                ParticleStack trail = new ParticleStack();
                                trail.add(HAParticles.MOLTEN_SPRAY.get());
                                return trail;
                            })
                            // landing globs splash and sizzle instead (trait.MoltenShot)
                            .set(ShotComponents.IMPACT_SOUND, () -> new ImpactSoundStack(Optional.empty(), Optional.empty()))
                            .gunshotSound(
                                    new GunShotSoundSettings(PlayableSound.holder(SoundEvents.FIRECHARGE_USE), 0.5f, 0.7f, -1, 0, 48),
                                    new GunShotSoundSettings(PlayableSound.holder(SoundEvents.BLAZE_BURN), 0.7f, 0.9f, -1, 0, 24),
                                    PlayableSound.holder(SoundEvents.DISPENSER_FAIL))
                            .muzzleFlash(MuzzleFlashType.SMALL_STAR)
                            .build())
            .reloadCues(ReloadCueStack.of(
                    new ReloadCue(0.40f, PlayableSound.of(SoundRegistry.CLOCKWORK_RIFLE_EJECT_MAG, 1.25f, 0.7f, 0.8f)),
                    // capsule seats at 1.8333s, the valve wheel turns 2.08-2.58s, then the pilot flame catches
                    new ReloadCue(1.83f, PlayableSound.of(SoundRegistry.CLOCKWORK_RIFLE_INSERT_MAG, 1.25f, 0.7f, 0.8f)),
                    new ReloadCue(2.20f, PlayableSound.of(SoundRegistry.COCK_HAMMER, 1.0f, 0.6f, 0.7f)),
                    new ReloadCue(2.45f, PlayableSound.of(SoundRegistry.COCK_HAMMER, 1.0f, 0.6f, 0.7f)),
                    new ReloadCue(2.65f, PlayableSound.of(PlayableSound.holder(SoundEvents.FLINTANDSTEEL_USE), 1.0f, 0.8f, 0.9f)),
                    new ReloadCue(2.70f, PlayableSound.of(PlayableSound.holder(SoundEvents.FIRECHARGE_USE), 0.6f, 1.2f, 1.3f))
            ))
            .equipSound(PlayableSound.of(SoundRegistry.CLOCKWORK_RIFLE_EQUIP, 0.75f, 0.6f, 0.7f))
            .animationAdjusters(HeavyHoldAdjuster.TILT)
            .build();

    public static final HeavyTraits GUNSTEEL_CRUCIBLE_TRAITS = HeavyTraits.builder()
            .sprintLockedWhileFiring()
            // left hand on the wire-wrapped bar of the top handle; on reload it turns the valve wheel on the boiler's left
            .hold(new HeavyHold(new Vec3(0, 12.75, -9.5), new Vec3(0, 12.75, -5.5), new Vec3(0, 4.5, -37),
                    new HeavyHold.Crank(new Vec3(-5, 4.5, -1.5), new Vec3(-7, 6.6, -1.5), 2.0833, 2.5833)))
            .build();

    /**
     * Gunsteel Congreve: shoulder-fired war-rocket tube. One rocket at a time, muzzle-loaded; each is packed from
     * several bullets ({@link HeavyShotComponents#RESERVE_AMMO_COST}, 8 by default).
     * <p>
     * The rocket is still the parent's bullet, so every modifier applies to it: it flies slower than a bullet (about
     * 52 blocks/s) but drops less over the same distance (the motor keeps it up), and bursts on impact like a stacked
     * Blackpowder Charge (radius 4 by default; a Blackpowder Charge modifier widens it further, Breach lets it break blocks). The launch shoves the shooter back,
     * more in the air, less braced, not at all on a tripod ({@code trait.Blowback}).
     * <p>
     * A rocket hits for 80 by default, and the burst deals the same, fading with distance, to everything else around.
     * Block damage follows shot damage in the parent, so it is scaled to {@link #CONGREVE_BLOCK_DAMAGE} whatever the
     * configured damage, keeping Breach craters the size they were at 28 damage.
     */
    public static final double CONGREVE_BLOCK_DAMAGE = 30;
    public static final GunProfile GUNSTEEL_CONGREVE = HAConfig.CONGREVE.profile(FireMode.SEMI, ArmPoseKind.RIFLE,
                    HAConfig.CONGREVE.template(0.9, RecoilProfile.of(22f, .4f, 1.4f, 1823))
                            .bulletSpeed(2.6)
                            // drop per block flown goes as gravity / speed^2: to fall less than even a musket ball (0.05 a
                            // tick at 18 blocks a tick) at 2.6 blocks a tick, the rocket needs under 0.05 * (2.6 / 18)^2 = 0.00104
                            .gravity(0.0008)
                            .knockback(1.5)
                            // the motor keeps burning: barely slows
                            .value(ShotComponents.BULLET_DRAG, 0.995)
                            .set(ShotComponents.BLOCK_DAMAGE_MULTIPLIER, () -> Value.of(CONGREVE_BLOCK_DAMAGE / Math.max(1, HAConfig.get(HAConfig.CONGREVE.damage))))
                            .value(ShotComponents.UNDERWATER_DRAG, 0.9)
                            .set(HeavyShotComponents.RESERVE_AMMO_COST, HAConfig.live(HAConfig.CONGREVE_BULLETS_PER_ROCKET))
                            .value(HeavyShotComponents.AIRBORNE_BLOWBACK, 1.75)
                            .value(HeavyShotComponents.BRACE_SPREAD_REDUCTION, 0.5)
                            .value(HeavyShotComponents.BRACE_RECOIL_REDUCTION, 0.5)
                            .set(HeavyShotComponents.SHOT_MODEL, () -> Optional.of(HeavyArtillery.id("congreve_rocket")))
                            .modify(map -> {
                                BlackpowderChargeOnHit warhead = map.getOrCreate(ShotComponents.ON_HIT)
                                        .getOrCreate(BlackpowderChargeOnHit.class, BlackpowderChargeOnHit::new);
                                // the first stack bursts BASE_RADIUS wide, each further one RADIUS_PER_STACK wider
                                double extra = HAConfig.get(HAConfig.CONGREVE_BLAST_RADIUS) - BlackpowderChargeOnHit.BASE_RADIUS;
                                for (int i = 0; i <= Math.round(extra / BlackpowderChargeOnHit.RADIUS_PER_STACK); i++) {
                                    warhead.addStack();
                                }
                            })
                            // smoke behind it, with sparks from the motor
                            .set(ShotComponents.PARTICLE_TRAIL, () -> {
                                ParticleStack trail = new ParticleStack();
                                trail.add(ParticleTypes.SMOKE);
                                trail.addAccent(new ParticleStack.ParticleAccent(ParticleTypes.LARGE_SMOKE, 0.12));
                                trail.addAccent(new ParticleStack.ParticleAccent(ParticleTypes.FLAME, 0.2));
                                return trail;
                            })
                            .gunshotSound(
                                    new GunShotSoundSettings(PlayableSound.holder(SoundEvents.FIREWORK_ROCKET_LAUNCH), 0.5f, 0.6f, -1, 0, 128),
                                    GunShotSoundSettings.standardEcho(SoundRegistry.BULLET_ECHO_MUZZLELOADER, 0.7f),
                                    PlayableSound.holder(SoundEvents.DISPENSER_FAIL))
                            .muzzleFlash(MuzzleFlashType.LARGE)
                            .build())
            .reloadCues(ReloadCueStack.of(
                    // muzzle tipped up (0-0.4s), a rocket fetched and slid down the tube (1.2-1.9s), the igniter cocked (2.75s)
                    new ReloadCue(0.15f, PlayableSound.of(SoundRegistry.LEATHER_ACCENT, 1.25f, 0.75f, 0.85f)),
                    new ReloadCue(1.20f, PlayableSound.of(SoundRegistry.FLINTLOCK_RELOAD_INSERT_BULLET, 1.25f, 0.6f, 0.7f)),
                    new ReloadCue(1.90f, PlayableSound.of(SoundRegistry.ARQUEBUS_LOAD, 1.25f, 0.6f, 0.7f)),
                    new ReloadCue(2.75f, PlayableSound.of(SoundRegistry.COCK_HAMMER, 1.25f, 0.7f, 0.8f))
            ))
            .equipSound(PlayableSound.of(SoundRegistry.MUSKET_EQUIP, 0.75f, 0.6f, 0.7f))
            .animationAdjusters(AnimationAdjuster.LOWER_HAMMER, HeavyHoldAdjuster.LOADED_ROUND)
            .build();

    /** Shouldered like a musket (the parent's rifle pose), so no heavy hold. */
    public static final HeavyTraits GUNSTEEL_CONGREVE_TRAITS = HeavyTraits.NONE;

    private HeavyWeapons() {
    }
}
