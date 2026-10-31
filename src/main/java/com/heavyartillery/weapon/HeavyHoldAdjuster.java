package com.heavyartillery.weapon;

import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.heavyartillery.client.HeavyArmPoses;
import io.redspace.irons_artifice.api.GunBones;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.MagazineContents;
import io.redspace.irons_artifice.item.animation_adjuster.AnimationAdjuster;

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

    private static void tilt(RenderPassInfo<GeoRenderState> renderPassInfo, BoneSnapshots snapshots) {
        HeavyArmPoses.tiltGun(renderPassInfo, snapshots);
    }

    private static void loadedRound(RenderPassInfo<GeoRenderState> renderPassInfo, BoneSnapshots snapshots) {
        MagazineContents magazine = renderPassInfo.getGeckolibData(GunItem.MAGAZINE_ANIMATION_TICKET);
        boolean reloading = renderPassInfo.getOrDefaultGeckolibData(GunItem.RELOAD_PERCENT_TICKET, 0f) > 0
                || renderPassInfo.getOrDefaultGeckolibData(GunItem.RELOAD_PROGRESS_SECONDS_TICKET, 0.0) > 0;
        if (reloading || magazine != null && magazine.count() > 0) {
            return;
        }
        snapshots.get(GunBones.MAGAZINE).ifPresent(bone -> bone.skipRender(true).skipChildrenRender(true));
    }

    private HeavyHoldAdjuster() {
    }
}
