package com.heavyartillery.mixin;

import com.heavyartillery.weapon.trait.MagazineSize;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.redspace.irons_artifice.item.CowboyHatItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * The Cowboy Hat's instant reload fills the whole magazine, as sized by {@link MagazineSize}.
 */
@Mixin(CowboyHatItem.class)
public abstract class CowboyHatItemMixin {
    @ModifyExpressionValue(
            method = "onBulletKill",
            at = @At(value = "INVOKE", target = "Lio/redspace/irons_artifice/item/GunItem;magazineCapacity()I")
    )
    private static int artifice_heavy_artillery$capacity(int base, @Local(argsOnly = true) LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            return MagazineSize.capacity(attacker.getItemBySlot(EquipmentSlot.MAINHAND), base);
        }
        return base;
    }

    @ModifyExpressionValue(
            method = "performInstantReload",
            at = @At(value = "INVOKE", target = "Lio/redspace/irons_artifice/item/GunItem;magazineCapacity()I")
    )
    private static int artifice_heavy_artillery$refillCapacity(int base, @Local(argsOnly = true, ordinal = 0) ItemStack gun) {
        return MagazineSize.capacity(gun, base);
    }
}
