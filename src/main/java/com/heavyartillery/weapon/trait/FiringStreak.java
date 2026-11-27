package com.heavyartillery.weapon.trait;

import com.heavyartillery.HeavyArtillery;
import com.heavyartillery.weapon.HeavyEntityData;
import com.heavyartillery.weapon.HeavyShotComponents;
import io.redspace.irons_artifice.api.GunAboutToShootEvent;
import io.redspace.irons_artifice.gun.ShotProfile;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * How long an entity has been firing without letting go of the trigger. Shared by every trait that cares about
 * sustained fire (spin-up, sprint lock, and later things like overheating).
 * <p>
 * Updated from {@link GunAboutToShootEvent}, which fires on both sides, so the client and server keep matching copies.
 *
 * @param shots     consecutive shots in the current streak
 * @param expiresAt game time after which the streak is over (fire delay of the last shot + spin-down grace)
 */
public record FiringStreak(int shots, long expiresAt) {
    public static final FiringStreak NONE = new FiringStreak(0, Long.MIN_VALUE);

    public boolean isActive(long now) {
        return shots > 0 && now <= expiresAt;
    }

    public int liveShots(long now) {
        return isActive(now) ? shots : 0;
    }

    public static FiringStreak get(LivingEntity entity) {
        return HeavyEntityData.of(entity).artifice_heavy_artillery$firingStreak();
    }

    public static boolean isSustainingFire(LivingEntity entity) {
        return get(entity).isActive(entity.level().getGameTime());
    }

    @Mod.EventBusSubscriber(modid = HeavyArtillery.MODID)
    public static final class Tracker {
        // lowest priority, not receiving cancelled events: only count shots that really go off
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onAboutToShoot(GunAboutToShootEvent event) {
            LivingEntity shooter = event.getEntity();
            ShotProfile profile = event.getShotProfile();
            long now = shooter.level().getGameTime();
            FiringStreak current = get(shooter);
            long window = profile.fireDelayTicks() + Math.round(profile.value(HeavyShotComponents.SPIN_DOWN_GRACE));
            HeavyEntityData.of(shooter).artifice_heavy_artillery$setFiringStreak(new FiringStreak(current.liveShots(now) + 1, now + window));
        }
    }
}
