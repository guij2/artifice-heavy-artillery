package com.heavyartillery.mixin.compat;

import com.heavyartillery.weapon.trait.ReloadFinish;
import com.heavyartillery.weapon.trait.ReserveAmmo;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.redspace.irons_artifice.item.BulletContainerItem;
import io.redspace.irons_artifice.utils.IronsArtificeTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Iron's Arms 'n Artifice: Spellbullet finishes every player reload itself, counting and taking bullets through its
 * {@code ReloadHelper} instead of the parent's {@code countBullets} and {@code consumeBullets}, which {@link ReserveAmmo}
 * hooks. Within a reload finish ({@link ReloadFinish}), its count and its take of plain bullets go through
 * {@link ReserveAmmo} as well, so an Enchanted Magazine reloads for free and a Congreve rocket costs its bullets. Spell
 * bullets keep Spellbullet's own cost of one per round.
 * <p>
 * Switching the selected bullet empties the gun and hands back a real bullet for each loaded round. Rounds that came free
 * were never bullets, so with a reserve cost below 1 plain bullets come back only at that cost's rate (none with an
 * Enchanted Magazine), or a free reload and a switch would make bullets out of nothing.
 * <p>
 * Added by {@code CompatMixins} only when Spellbullet is installed.
 */
@Pseudo
@Mixin(targets = "com.ironsspellbullet.event.ReloadHelper", remap = false)
public abstract class SpellBulletReloadHelperMixin {
    /** Spellbullet's id for plain, spell-less bullets. */
    @Unique
    private static final String artifice_heavy_artillery$NORMAL = "normal";
    /** The bullet kind Spellbullet last counted for the reload being finished on this thread. */
    @Unique
    private static final ThreadLocal<String> artifice_heavy_artillery$counted = new ThreadLocal<>();

    @ModifyReturnValue(method = "countSpecificBullets(Lnet/minecraft/world/entity/player/Player;Ljava/lang/String;)I", at = @At("RETURN"))
    private static int artifice_heavy_artillery$available(int carried, @Local(argsOnly = true) Player player, @Local(argsOnly = true) String bullet) {
        ItemStack gun = ReloadFinish.gun();
        if (gun == null || player == null) {
            return carried;
        }
        artifice_heavy_artillery$counted.set(bullet);
        if (!artifice_heavy_artillery$NORMAL.equals(bullet)) {
            return carried;
        }
        return ReserveAmmo.effectiveAvailable(carried, ReserveAmmo.cost(player, gun));
    }

    @WrapMethod(method = "smartConsumeAndRecord(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V")
    private static void artifice_heavy_artillery$pay(Player player, ItemStack gun, int amount, Operation<Void> original) {
        if (player == null || gun == null || gun != ReloadFinish.gun() || !artifice_heavy_artillery$NORMAL.equals(artifice_heavy_artillery$counted.get())) {
            original.call(player, gun, amount);
            return;
        }
        // Spellbullet records one chamber entry per bullet it takes and fires one per shot, so it takes at most one bullet
        // per round; bullets a round costs beyond that (a Congreve rocket's) are taken here
        ReserveAmmo.pay(player, gun, amount, paid -> {
            original.call(player, gun, Math.min(paid, amount));
            if (paid > amount) {
                artifice_heavy_artillery$takePlainBullets(player, paid - amount);
            }
        });
    }

    @WrapOperation(
            method = "unloadGunChamber(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z")
    )
    private static boolean artifice_heavy_artillery$noFreeRefund(Inventory inventory, ItemStack bullet, Operation<Boolean> original,
                                                                 @Local(argsOnly = true) Player player, @Local(argsOnly = true) ItemStack gun) {
        if (player != null && gun != null && artifice_heavy_artillery$isPlain(bullet)) {
            double cost = ReserveAmmo.cost(player, gun);
            if (cost < 1 && ReserveAmmo.toConsume(1, cost, player.getRandom()) == 0) {
                return true; // kept back: true also stops Spellbullet dropping it
            }
        }
        return original.call(inventory, bullet);
    }

    @Unique
    private static boolean artifice_heavy_artillery$isPlain(ItemStack bullet) {
        return bullet.is(IronsArtificeTags.AMMO) && bullet.getComponentsPatch().isEmpty();
    }

    /** Takes plain bullets (no spell or other data on them) from the inventory, then from bullet pouches. */
    @Unique
    private static void artifice_heavy_artillery$takePlainBullets(Player player, int amount) {
        Inventory inventory = player.getInventory();
        int remaining = amount;
        for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (artifice_heavy_artillery$isPlain(stack)) {
                int take = Math.min(remaining, stack.getCount());
                stack.shrink(take);
                remaining -= take;
            }
        }
        for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.getItem() instanceof BulletContainerItem) {
                remaining -= BulletContainerItem.drain(stack, remaining);
            }
        }
    }
}
