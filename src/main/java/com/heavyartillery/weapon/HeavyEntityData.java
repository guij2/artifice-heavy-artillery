package com.heavyartillery.weapon;

import com.heavyartillery.weapon.trait.FiringStreak;
import net.minecraft.world.entity.LivingEntity;

/**
 * Per-entity data the heavy weapon traits keep, carried by every {@link LivingEntity} ({@code mixin.LivingEntityMixin}).
 * Transient and unsynced: each side tracks it independently off {@code GunAboutToShootEvent}, which fires on both the
 * client and the server, so both agree without extra packets.
 */
public interface HeavyEntityData {
    /** How long the entity has been firing without letting go, for {@code trait.FiringStreak}. */
    FiringStreak artifice_heavy_artillery$firingStreak();

    void artifice_heavy_artillery$setFiringStreak(FiringStreak streak);

    /** Game time of the entity's last shot ({@link Long#MIN_VALUE} if none), for {@code trait.PowderRam}. */
    long artifice_heavy_artillery$lastShot();

    void artifice_heavy_artillery$setLastShot(long gameTime);

    static HeavyEntityData of(LivingEntity entity) {
        return (HeavyEntityData) entity;
    }
}
