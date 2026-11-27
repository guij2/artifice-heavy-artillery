package com.heavyartillery.mixin;

import com.heavyartillery.weapon.trait.ReloadFinish;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.redspace.irons_artifice.data.ReloadResult;
import io.redspace.irons_artifice.events.ServerEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Where the parent finishes a reload, hooked from the caller's side: another mod may cancel
 * {@code GunplayManager.attemptFinishReload} at its head and finish the reload itself, skipping hooks inside it.
 */
@Mixin(value = ServerEvents.class, remap = false)
public abstract class ServerEventsMixin {

    /**
     * Finishes the reload with the gun in {@link ReloadFinish} and uncapped (0). The parent caps it at the rounds the magazine
     * was missing when it started, and the modifier menu opens mid-reload, so a Drum Magazine fitted then would only fill the
     * old capacity.
     */
    @WrapOperation(
            method = "onEntityTick",
            at = @At(value = "INVOKE", target = "Lio/redspace/irons_artifice/item/GunplayManager;attemptFinishReload(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;I)Lio/redspace/irons_artifice/data/ReloadResult;")
    )
    private static ReloadResult artifice_heavy_artillery$finishReload(LivingEntity living, ItemStack gun, int roundsToLoad, Operation<ReloadResult> original) {
        return ReloadFinish.run(gun, () -> original.call(living, gun, 0));
    }
}
