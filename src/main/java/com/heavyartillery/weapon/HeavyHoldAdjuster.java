package com.heavyartillery.weapon;

import com.heavyartillery.client.HeavyArmPoses;
import io.redspace.irons_artifice.api.GunBones;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.MagazineContents;
import io.redspace.irons_artifice.item.animation_adjuster.AnimationAdjuster;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

/**
 * Animation adjusters for heavy weapons, listed in a GunProfile like the parent's own (they only run when rendering).
 */
public final class HeavyHoldAdjuster {
    /** Tilts the gun about its grip in third person so it points where its holder looks; see {@link HeavyHold}. */
    public static final AnimationAdjuster TILT = HeavyHoldAdjuster::tilt;
    /**
     * For guns whose {@code magazine} bone is the round itself (the rocket in a launcher's tube): hides it while the gun
     * is empty, until a reload brings a new one in.
     */
    public static final AnimationAdjuster LOADED_ROUND = HeavyHoldAdjuster::loadedRound;

    private static void tilt(AnimationState<GunItem> animationState, GeoModel<GunItem> model) {
        HeavyArmPoses.tiltGun(animationState, model);
    }

    private static void loadedRound(AnimationState<GunItem> animationState, GeoModel<GunItem> model) {
        MagazineContents magazine = animationState.getData(GunItem.MAGAZINE_ANIMATION_TICKET);
        Float reloadPercent = animationState.getData(GunItem.RELOAD_PERCENT_TICKET);
        boolean reloading = reloadPercent != null && reloadPercent > 0 || AnimationAdjuster.reloadProgressSeconds(animationState) > 0;
        boolean shown = reloading || magazine == null || magazine.count() > 0;
        // GeckoLib 4 bones keep their hidden flag between renders (and every stack of a gun shares them): set it both ways
        model.getBone(GunBones.MAGAZINE).ifPresent(bone -> {
            bone.setHidden(!shown);
            bone.setChildrenHidden(!shown);
        });
    }

    private HeavyHoldAdjuster() {
    }
}
