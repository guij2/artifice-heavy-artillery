package com.heavyartillery.mixin;

import com.heavyartillery.modifier.ReflexSightModifier;
import com.heavyartillery.weapon.trait.SightAim;
import com.heavyartillery.weapon.trait.SprintLock;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
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
     * Using an item slows its user to 20% (1.21.1 has no use_effects component to change that): aiming down a reflex
     * sight only to {@link ReflexSightModifier#AIMED_SPEED}. A @ModifyExpressionValue rather than a @ModifyConstant,
     * so other mods changing the same slowdown (Sinew does) still find their constants.
     */
    @ModifyExpressionValue(method = "aiStep", at = @At(value = "CONSTANT", args = "floatValue=0.2F"), require = 0)
    private float artifice_heavy_artillery$aimedSpeed(float slowdown) {
        return SightAim.isAiming((LocalPlayer) (Object) this) ? ReflexSightModifier.AIMED_SPEED : slowdown;
    }
}
