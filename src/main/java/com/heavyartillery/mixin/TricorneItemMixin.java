package com.heavyartillery.mixin;

import com.heavyartillery.weapon.trait.MagazineSize;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.redspace.irons_artifice.api.ComposeShotEvent;
import io.redspace.irons_artifice.item.TricorneItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * The Tricorne's first-shot bonus waits for a full magazine, as sized by {@link MagazineSize}.
 */
@Mixin(TricorneItem.class)
public abstract class TricorneItemMixin {
    @ModifyExpressionValue(
            method = "handleTricorneAbility",
            at = @At(value = "INVOKE", target = "Lio/redspace/irons_artifice/gun/GunProfile;magazineCapacity()I")
    )
    private static int artifice_heavy_artillery$capacity(int base, @Local(argsOnly = true) ComposeShotEvent event) {
        return MagazineSize.capacity(event.getShotProfile(), base);
    }
}
