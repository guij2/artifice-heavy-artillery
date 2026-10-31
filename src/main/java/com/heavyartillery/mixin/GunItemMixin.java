package com.heavyartillery.mixin;

import com.heavyartillery.weapon.trait.MagazineSize;
import com.heavyartillery.weapon.trait.SightAim;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.redspace.irons_artifice.item.GunItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The gun's tooltip (capacity, and whether to show fire rate) and its durability bar use the capacity after
 * {@link MagazineSize}. A gun with a reflex sight aims down it while use is held ({@link SightAim}).
 */
@Mixin(GunItem.class)
public abstract class GunItemMixin {
    @ModifyExpressionValue(
            method = "appendHoverText",
            at = @At(value = "INVOKE", target = "Lio/redspace/irons_artifice/gun/GunProfile;magazineCapacity()I")
    )
    private int artifice_heavy_artillery$tooltipCapacity(int base, @Local(argsOnly = true) ItemStack stack) {
        return MagazineSize.capacity(stack, base);
    }

    @ModifyExpressionValue(
            method = "getBarWidth",
            at = @At(value = "INVOKE", target = "Lio/redspace/irons_artifice/item/GunItem;magazineCapacity()I")
    )
    private int artifice_heavy_artillery$barCapacity(int base, @Local(argsOnly = true) ItemStack stack) {
        return MagazineSize.capacity(stack, base);
    }

    /** Holding use with a reflex sight aims down it, as the parent's scope does (but silently). Not while reloading. */
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void artifice_heavy_artillery$aimDownSight(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack stack = player.getItemInHand(hand);
        if (SightAim.hasSight(stack) && !GunItem.isReloading(stack)) {
            cir.setReturnValue(ItemUtils.startUsingInstantly(level, player, hand));
        }
    }

    @Inject(method = "getUseDuration", at = @At("HEAD"), cancellable = true)
    private void artifice_heavy_artillery$aimDuration(ItemStack stack, LivingEntity user, CallbackInfoReturnable<Integer> cir) {
        if (SightAim.hasSight(stack)) {
            cir.setReturnValue(SightAim.USE_DURATION);
        }
    }
}
