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
 * Crouching on the ground braces the weapon, cutting spread and recoil by
 * {@link HeavyShotComponents#BRACE_SPREAD_REDUCTION} / {@link HeavyShotComponents#BRACE_RECOIL_REDUCTION}.
 * This stacks with the parent mod's own (smaller) crouch spread bonus.
 */
@EventBusSubscriber(modid = HeavyArtillery.MODID)
public final class Brace {

    public static boolean isBracing(LivingEntity entity) {
        return entity.isCrouching() && entity.onGround();
    }

    @SubscribeEvent
    static void onCompose(ComposeShotEvent event) {
        if (!isBracing(event.getEntity())) {
            return;
        }
        ShotProfile profile = event.getShotProfile();
        double spread = HeavyMath.fraction(profile.value(HeavyShotComponents.BRACE_SPREAD_REDUCTION));
        if (spread > 0) {
            profile.modifyValue(ShotComponents.SPREAD, new ValueModifier(-spread, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.HARMFUL));
        }
        double recoil = HeavyMath.fraction(profile.value(HeavyShotComponents.BRACE_RECOIL_REDUCTION));
        if (recoil > 0) {
            profile.modifyValue(ShotComponents.CAMERA_RECOIL_MULTIPLIER, new ValueModifier(-recoil, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.HARMFUL));
        }
    }

    private Brace() {
    }
}
