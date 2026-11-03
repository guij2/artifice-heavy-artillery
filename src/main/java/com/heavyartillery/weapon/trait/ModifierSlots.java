package com.heavyartillery.weapon.trait;

import com.heavyartillery.HAConfig;
import io.redspace.irons_artifice.gun.GunProfile;
import io.redspace.irons_artifice.menu.GunContainer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * A heavy weapon's modifier slots come from the config. The parent sizes everything (the modifier menu, which slots a
 * shot reads, the tooltip) from {@link GunProfile#modifierSlots()}, which {@code GunProfileMixin} routes here.
 * <p>
 * Lowering the count leaves the modifiers in the removed slots on the gun but unused; they go back to the player when
 * the gun's modifier menu is next opened ({@code GunModifierMenuMixin}), before saving the menu could drop them.
 */
public final class ModifierSlots {
    private static final Map<GunProfile, ModConfigSpec.IntValue> SLOTS = new IdentityHashMap<>();

    /** Called once per heavy weapon, while items register. */
    public static void register(GunProfile profile, ModConfigSpec.IntValue slots) {
        SLOTS.put(profile, slots);
    }

    public static int slots(GunProfile profile, int base) {
        ModConfigSpec.IntValue slots = SLOTS.get(profile);
        return slots == null ? base : HAConfig.get(slots);
    }

    /**
     * Server side, when a gun's modifier menu opens: gives the player any modifiers stored past the gun's slots.
     */
    public static void returnStranded(Player player, ItemStack gun, Container modifiers) {
        if (player.level().isClientSide() || !(modifiers instanceof GunContainer container)) {
            return;
        }
        ItemContainerContents contents = gun.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        boolean returned = false;
        for (int slot = container.getContainerSize(); slot < contents.getSlots(); slot++) {
            ItemStack stranded = contents.getStackInSlot(slot).copy();
            if (!stranded.isEmpty()) {
                player.getInventory().placeItemBackInInventory(stranded);
                returned = true;
            }
        }
        if (returned) {
            // rewrites the gun's contents with only its slots
            container.setChanged();
        }
    }

    private ModifierSlots() {
    }
}
