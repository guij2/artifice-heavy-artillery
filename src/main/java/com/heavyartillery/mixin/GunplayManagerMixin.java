package com.heavyartillery.mixin;

import com.heavyartillery.weapon.HeavyGunItem;
import com.heavyartillery.weapon.trait.MagazineSize;
import com.heavyartillery.weapon.trait.ReserveAmmo;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.item.GunplayManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Routes Iron's Arms 'n Artifice's reload ammo accounting through {@link ReserveAmmo}, so
 * {@code HeavyShotComponents.RESERVE_AMMO_COST} decides how many carried bullets a reload uses, its magazine
 * capacity reads through {@link MagazineSize}, and its reload time through {@link HeavyGunItem#reloadTime}. A reload
 * fills what the magazine is missing when it finishes.
 */
@Mixin(GunplayManager.class)
public abstract class GunplayManagerMixin {

    @WrapOperation(
            method = {"attemptStartReload", "attemptFinishReload"},
            at = @At(value = "INVOKE", target = "Lio/redspace/irons_artifice/item/GunplayManager;countBullets(Lnet/minecraft/world/entity/player/Player;)I")
    )
    private static int artifice_heavy_artillery$reserveAmmoAvailable(Player player, Operation<Integer> original, @Local(argsOnly = true) ItemStack gun) {
        int carried = original.call(player);
        return ReserveAmmo.effectiveAvailable(carried, ReserveAmmo.cost(player, gun));
    }

    @WrapOperation(
            method = "attemptFinishReload",
            at = @At(value = "INVOKE", target = "Lio/redspace/irons_artifice/item/GunplayManager;consumeBullets(Lnet/minecraft/world/entity/player/Player;I)V")
    )
    private static void artifice_heavy_artillery$reserveAmmoConsume(Player player, int amount, Operation<Void> original, @Local(argsOnly = true) ItemStack gun) {
        double cost = ReserveAmmo.cost(player, gun);
        int paid = ReserveAmmo.toConsume(amount, cost, player.getRandom());
        if (paid > 0) {
            original.call(player, paid);
        }
        if (cost < 1) {
            ReserveAmmo.onRefund(player, amount - paid);
        }
    }

    @ModifyExpressionValue(
            method = {"attemptStartReload", "attemptFinishReload"},
            at = @At(value = "INVOKE", target = "Lio/redspace/irons_artifice/item/GunItem;magazineCapacity()I")
    )
    private static int artifice_heavy_artillery$reloadCapacity(int base, @Local(argsOnly = true) ItemStack gun) {
        return MagazineSize.capacity(gun, base);
    }

    /**
     * The parent caps a reload at the rounds the magazine was missing when it started. The modifier menu opens mid-reload,
     * so a Drum Magazine fitted then only filled the old capacity. Uncapped (0), the reload loads what is missing when it
     * finishes, still limited by the bullets carried.
     */
    @ModifyVariable(method = "attemptFinishReload", at = @At("HEAD"), argsOnly = true)
    private static int artifice_heavy_artillery$fillToCurrentCapacity(int roundsToLoad) {
        return 0;
    }

    @ModifyExpressionValue(
            method = "fireShot",
            at = @At(value = "INVOKE", target = "Lio/redspace/irons_artifice/gun/GunProfile;magazineCapacity()I")
    )
    private static int artifice_heavy_artillery$fireCapacity(int base, @Local(argsOnly = true) ShotProfile profile) {
        return MagazineSize.capacity(profile, base);
    }

    @ModifyExpressionValue(
            method = "attemptStartReload",
            at = @At(value = "INVOKE", target = "Lio/redspace/irons_artifice/gun/GunProfile;reloadTimeTicks()I")
    )
    private static int artifice_heavy_artillery$reloadTime(int base, @Local(argsOnly = true) ItemStack gun) {
        return HeavyGunItem.reloadTime(gun, base);
    }
}
