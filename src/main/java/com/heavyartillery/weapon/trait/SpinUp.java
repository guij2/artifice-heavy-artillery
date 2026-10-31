package com.heavyartillery.weapon.trait;

import com.heavyartillery.HeavyArtillery;
import com.heavyartillery.weapon.HeavyShotComponents;
import io.redspace.irons_artifice.api.ComposeShotEvent;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.gun.ShotProfile;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Guns with {@link HeavyShotComponents#SPIN_UP_DELAY} fire slower from rest and speed up over
 * {@link HeavyShotComponents#SPIN_UP_SHOTS} consecutive shots.
 * <p>
 * Works by adding fire delay while composing the shot, so the parent mod's fire animation speed and
 * fire cycle sound cues slow down along with it.
 */
@EventBusSubscriber(modid = HeavyArtillery.MODID)
public final class SpinUp {

    @SubscribeEvent
    static void onCompose(ComposeShotEvent event) {
        ShotProfile profile = event.getShotProfile();
        double maxPenalty = profile.value(HeavyShotComponents.SPIN_UP_DELAY);
        if (maxPenalty <= 0) {
            return;
        }
        double penalty = maxPenalty * (1 - progress(event.getEntity(), profile));
        if (penalty > 0) {
            profile.modifyValue(ShotComponents.FIRE_DELAY, new ValueModifier(penalty, ValueModifier.Operation.ADD, ValueModifier.Type.HARMFUL));
        }
    }

    /**
     * @return 0 when the barrels are at rest, 1 when fully spun up
     */
    public static double progress(LivingEntity entity, ShotProfile profile) {
        double shotsNeeded = profile.value(HeavyShotComponents.SPIN_UP_SHOTS);
        if (shotsNeeded <= 0) {
            return 1;
        }
        int shots = FiringStreak.get(entity).liveShots(entity.level().getGameTime());
        return Mth.clamp(shots / shotsNeeded, 0, 1);
    }

    private SpinUp() {
    }
}
