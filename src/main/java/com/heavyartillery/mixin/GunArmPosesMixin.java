package com.heavyartillery.mixin;

import com.heavyartillery.client.HeavyArmPoses;
import io.redspace.irons_artifice.client.gun.GunArmPoses;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Heavy weapons are rifles to the parent mod, which registers its rifle arm pose for every gun (registering our own
 * would clash), so the third-person carry pose takes over from inside that pose.
 */
// Cosmetic hooks (require = 0): if another mod reworks a target, only the effect is lost, not the game. Dev runs still
// fail on a miss (mixin.debug.countInjections).
@Mixin(GunArmPoses.class)
public abstract class GunArmPosesMixin {
    @Inject(method = "applyRiflePose", at = @At("HEAD"), cancellable = true, require = 0)
    private static void artifice_heavy_artillery$heavyCarryPose(HumanoidModel<?> model, HumanoidRenderState renderState, HumanoidArm arm, CallbackInfo ci) {
        if (HeavyArmPoses.apply(model, renderState, arm)) {
            ci.cancel();
        }
    }
}
