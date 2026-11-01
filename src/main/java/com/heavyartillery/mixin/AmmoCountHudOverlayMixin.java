package com.heavyartillery.mixin;

import com.heavyartillery.weapon.trait.MagazineSize;
import com.heavyartillery.weapon.trait.ReserveAmmo;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.redspace.irons_artifice.client.gui.AmmoCountHudOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * The ammo HUD's reserve count is in rounds the carried bullets can load, for guns whose rounds take several bullets
 * each (a rocket): 64 bullets show as 8 rockets. Other guns keep the parent's plain bullet count.
 * <p>
 * The capacity shown is the one after {@link MagazineSize} (a Drum Magazine).
 */
// Cosmetic hooks (require = 0): if another mod reworks a target, only the effect is lost, not the game. Dev runs still
// fail on a miss (mixin.debug.countInjections).
@Mixin(AmmoCountHudOverlay.class)
public abstract class AmmoCountHudOverlayMixin {
    @WrapOperation(
            method = "render",
            at = @At(value = "INVOKE", target = "Lio/redspace/irons_artifice/item/GunplayManager;countBullets(Lnet/minecraft/world/entity/player/Player;)I"),
            require = 0
    )
    private static int artifice_heavy_artillery$reserveInRounds(Player player, Operation<Integer> original) {
        int carried = original.call(player);
        double cost = ReserveAmmo.cost(player, player.getMainHandItem());
        return cost > 1 ? ReserveAmmo.effectiveAvailable(carried, cost) : carried;
    }

    @ModifyExpressionValue(
            method = "render",
            at = @At(value = "INVOKE", target = "Lio/redspace/irons_artifice/item/GunItem;magazineCapacity()I"),
            require = 0
    )
    private static int artifice_heavy_artillery$capacity(int base) {
        Player player = Minecraft.getInstance().player;
        return player == null ? base : MagazineSize.capacity(player.getMainHandItem(), base);
    }
}
