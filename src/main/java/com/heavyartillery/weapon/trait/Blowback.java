package com.heavyartillery.weapon.trait;

import com.heavyartillery.HeavyArtillery;
import com.heavyartillery.weapon.HeavyMath;
import com.heavyartillery.weapon.HeavyShotComponents;
import io.redspace.irons_artifice.api.ComposeShotEvent;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.gun.ShotProfile;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * How hard a shot pushes its shooter back (the parent's {@link ShotComponents#CHARACTER_BLOWBACK}) depends on footing:
 * more in the air ({@link HeavyShotComponents#AIRBORNE_BLOWBACK}), less when braced ({@link Brace}, by the brace's
 * recoil reduction), and none at all when mounted on a tripod ({@link TripodMount}), which can't move.
 */
@EventBusSubscriber(modid = HeavyArtillery.MODID)
public final class Blowback {

    @SubscribeEvent
    static void onCompose(ComposeShotEvent event) {
        ShotProfile profile = event.getShotProfile();
        if (profile.value(ShotComponents.CHARACTER_BLOWBACK) <= 0) {
            return;
        }
        LivingEntity shooter = event.getEntity();
        if (TripodMount.isMounted(shooter)) {
            profile.modifyValue(ShotComponents.CHARACTER_BLOWBACK, new ValueModifier(-1, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.HARMFUL));
            return;
        }
        if (!shooter.onGround()) {
            double airborne = Math.max(0, profile.value(HeavyShotComponents.AIRBORNE_BLOWBACK));
            profile.modifyValue(ShotComponents.CHARACTER_BLOWBACK, new ValueModifier(airborne - 1, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.HARMFUL));
        } else if (Brace.isBracing(shooter)) {
            double braced = HeavyMath.fraction(profile.value(HeavyShotComponents.BRACE_RECOIL_REDUCTION));
            if (braced > 0) {
                profile.modifyValue(ShotComponents.CHARACTER_BLOWBACK, new ValueModifier(-braced, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.HARMFUL));
            }
        }
    }

    private Blowback() {
    }
}
