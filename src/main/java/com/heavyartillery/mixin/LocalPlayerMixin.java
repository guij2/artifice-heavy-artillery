package com.heavyartillery.mixin;

import com.heavyartillery.weapon.trait.SprintLock;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Inject(method = "canStartSprinting", at = @At("HEAD"), cancellable = true)
    private void artifice_heavy_artillery$blockSprintWhileFiring(CallbackInfoReturnable<Boolean> cir) {
        if (SprintLock.isSprintLocked((LocalPlayer) (Object) this)) {
            cir.setReturnValue(false);
        }
    }
}
