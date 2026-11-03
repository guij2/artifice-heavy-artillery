package com.heavyartillery.weapon.trait;

import com.heavyartillery.HAConfig;
import com.heavyartillery.HeavyArtillery;
import com.heavyartillery.weapon.HeavyGunItem;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

/**
 * Heavy weapons slow their holder by their configured {@code movementPenalty}. The item carries the default as an
 * attribute modifier; this swaps in the configured value whenever the game reads the item's modifiers (to apply them
 * when it is picked up or switched to, and for the tooltip).
 */
@EventBusSubscriber(modid = HeavyArtillery.MODID)
public final class MovementPenalty {
    public static final Identifier ID = HeavyArtillery.id("heavy_weapon_movement_penalty");

    public static AttributeModifier modifier(double penalty) {
        return new AttributeModifier(ID, -penalty, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @SubscribeEvent
    static void onItemAttributes(ItemAttributeModifierEvent event) {
        if (!(event.getItemStack().getItem() instanceof HeavyGunItem gun)) {
            return;
        }
        double penalty = HAConfig.get(gun.balance().movementPenalty);
        if (penalty > 0) {
            event.replaceModifier(Attributes.MOVEMENT_SPEED, modifier(penalty), EquipmentSlotGroup.MAINHAND);
        } else {
            event.removeModifier(Attributes.MOVEMENT_SPEED, ID);
        }
    }

    private MovementPenalty() {
    }
}
