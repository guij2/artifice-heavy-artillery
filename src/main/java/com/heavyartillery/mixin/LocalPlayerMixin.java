package com.heavyartillery.mixin;

import com.heavyartillery.HAConfig;
import com.heavyartillery.weapon.trait.SightAim;
import com.heavyartillery.weapon.trait.SprintLock;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Feel-only hooks (require = 0; SprintLock also stops sprinting server-side): if another mod reworks a target, only the
// effect is lost, not the game. Dev runs still fail on a miss (mixin.debug.countInjections).
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Inject(method = "canStartSprinting", at = @At("HEAD"), cancellable = true, require = 0)
    private void artifice_heavy_artillery$blockSprintWhileFiring(CallbackInfoReturnable<Boolean> cir) {
        if (SprintLock.isSprintLocked((LocalPlayer) (Object) this)) {
            cir.setReturnValue(false);
        }
    }

    /**
     * Aiming down a reflex sight walks at the configured speed. The sight's use_effects component has a speed too, but
     * it is set when the sight is fitted, so it would miss later config changes.
     */
    @ModifyReturnValue(method = "itemUseSpeedMultiplier", at = @At("RETURN"), require = 0)
    private float artifice_heavy_artillery$aimedSpeed(float speed) {
        return SightAim.isAiming((LocalPlayer) (Object) this) ? HAConfig.get(HAConfig.SIGHT_AIMED_SPEED).floatValue() : speed;
    }
}
