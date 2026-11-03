package com.heavyartillery.mixin;

import com.heavyartillery.weapon.trait.ModifierSlots;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.redspace.irons_artifice.gun.GunProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * A heavy weapon's modifier slots come from the config ({@link ModifierSlots}); the parent reads them only from here.
 */
@Mixin(GunProfile.class)
public abstract class GunProfileMixin {
    @ModifyReturnValue(method = "modifierSlots", at = @At("RETURN"))
    private int artifice_heavy_artillery$modifierSlots(int base) {
        return ModifierSlots.slots((GunProfile) (Object) this, base);
    }
}
