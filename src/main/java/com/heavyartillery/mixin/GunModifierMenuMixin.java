package com.heavyartillery.mixin;

import com.heavyartillery.weapon.trait.ModifierSlots;
import io.redspace.irons_artifice.menu.GunModifierMenu;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Opening a gun's modifier menu gives back the modifiers left past its slots after the config lowered them
 * ({@link ModifierSlots#returnStranded}). Only matters once slots are lowered (require = 0): if another mod reworks the
 * menu, those modifiers stay unused on the gun rather than coming back. Dev runs still fail on a miss
 * (mixin.debug.countInjections).
 */
@Mixin(GunModifierMenu.class)
public abstract class GunModifierMenuMixin {
    @Shadow
    @Final
    public ItemStack gunstack;

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;)V", at = @At("TAIL"), require = 0)
    private void artifice_heavy_artillery$returnStranded(int containerId, Inventory playerInventory, Container gunInventory, CallbackInfo ci) {
        ModifierSlots.returnStranded(playerInventory.player, gunstack, gunInventory);
    }
}
