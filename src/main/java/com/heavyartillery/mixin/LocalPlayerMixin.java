package com.heavyartillery.mixin;

import com.heavyartillery.modifier.ReflexSightModifier;
import com.heavyartillery.weapon.trait.SightAim;
import com.heavyartillery.weapon.trait.SprintLock;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Inject(method = "canStartSprinting", at = @At("HEAD"), cancellable = true)
    private void artifice_heavy_artillery$blockSprintWhileFiring(CallbackInfoReturnable<Boolean> cir) {
        if (SprintLock.isSprintLocked((LocalPlayer) (Object) this)) {
            cir.setReturnValue(false);
        }
    }

    /**
     * Using an item slows its user to 20% (1.21.1 has no use_effects component to change that): aiming down a reflex
     * sight only to {@link ReflexSightModifier#AIMED_SPEED}.
     */
    @ModifyConstant(method = "aiStep", constant = @Constant(floatValue = 0.2F))
    private float artifice_heavy_artillery$aimedSpeed(float slowdown) {
        return SightAim.isAiming((LocalPlayer) (Object) this) ? ReflexSightModifier.AIMED_SPEED : slowdown;
    }
}
