package com.heavyartillery.mixin;

import com.heavyartillery.weapon.trait.MagazineSize;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.utils.RecoilHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * The recoil pattern counts rounds fired out of the magazine as sized by {@link MagazineSize}, so a drum never gives a
 * negative round index.
 */
@Mixin(RecoilHelper.class)
public abstract class RecoilHelperMixin {
    @ModifyExpressionValue(
            method = "getBulletIndex",
            at = @At(value = "INVOKE", target = "Lio/redspace/irons_artifice/gun/GunProfile;magazineCapacity()I")
    )
    private static int artifice_heavy_artillery$capacity(int base, @Local(argsOnly = true) ShotProfile profile) {
        return MagazineSize.capacity(profile, base);
    }
}
