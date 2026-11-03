package com.heavyartillery.weapon.trait;

import com.heavyartillery.HAConfig;
import com.heavyartillery.weapon.HeavyShotComponents;
import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.gun.ShotProfile;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Shots with {@link HeavyShotComponents#RIFLING} hit harder the further they have flown: the bonus per block times
 * the straight-line distance from where the shot was fired to where it hits, counting at most {@link #maxDistance()}
 * blocks (so stacked barrels raise the bonus at every range, not only how fast it builds).
 * <p>
 * Applied to {@code Bullet.resolveDamage} ({@code BulletMixin}), so whatever reads the shot's damage when it lands
 * sees it too: the hit itself, a Blackpowder Charge burst, chain lightning, frozen shrapnel.
 */
public final class Rifling {
    /** Blocks of flight that count towards the bonus, from the config. */
    public static int maxDistance() {
        return HAConfig.get(HAConfig.RIFLED_MAX_DISTANCE);
    }

    /**
     * @param origin where the shot was fired from, or null if not known
     * @param impact where it is hitting
     */
    public static float apply(Bullet bullet, float damage, @Nullable Vec3 origin, Vec3 impact) {
        ShotProfile profile = bullet.getProfile();
        if (profile == null || origin == null) {
            return damage;
        }
        double perBlock = profile.value(HeavyShotComponents.RIFLING);
        if (perBlock <= 0) {
            return damage;
        }
        return (float) (damage * (1 + perBlock * Math.min(maxDistance(), origin.distanceTo(impact))));
    }

    private Rifling() {
    }
}
