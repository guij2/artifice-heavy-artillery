package com.heavyartillery.mixin;

import com.heavyartillery.weapon.HeavyEntityData;
import com.heavyartillery.weapon.trait.FiringStreak;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Carries {@link HeavyEntityData} on every living entity. */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements HeavyEntityData {
    @Unique
    private FiringStreak artifice_heavy_artillery$firingStreak = FiringStreak.NONE;
    @Unique
    private long artifice_heavy_artillery$lastShot = Long.MIN_VALUE;

    @Override
    public FiringStreak artifice_heavy_artillery$firingStreak() {
        return artifice_heavy_artillery$firingStreak;
    }

    @Override
    public void artifice_heavy_artillery$setFiringStreak(FiringStreak streak) {
        artifice_heavy_artillery$firingStreak = streak;
    }

    @Override
    public long artifice_heavy_artillery$lastShot() {
        return artifice_heavy_artillery$lastShot;
    }

    @Override
    public void artifice_heavy_artillery$setLastShot(long gameTime) {
        artifice_heavy_artillery$lastShot = gameTime;
    }
}
