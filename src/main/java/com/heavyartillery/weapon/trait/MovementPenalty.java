package com.heavyartillery.weapon.trait;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.heavyartillery.HAConfig;
import com.heavyartillery.weapon.HeavyGunItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

/**
 * Heavy weapons slow their holder by their configured {@code movementPenalty}, as a main hand attribute modifier the
 * game reads from {@link HeavyGunItem#getAttributeModifiers} (to apply it when the gun is picked up or switched to,
 * and for the tooltip), so it always has the configured value.
 */
public final class MovementPenalty {
    public static final UUID ID = UUID.fromString("5d1f0a3e-8b2c-4c1e-9f6a-3e7b2d9c4a11");

    public static AttributeModifier modifier(double penalty) {
        return new AttributeModifier(ID, "Heavy weapon movement penalty", -penalty, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    /** The gun's other modifiers, plus the movement penalty in the main hand when there is one. */
    public static Multimap<Attribute, AttributeModifier> withPenalty(HeavyGunItem gun, EquipmentSlot slot, Multimap<Attribute, AttributeModifier> base) {
        double penalty = HAConfig.get(gun.balance().movementPenalty);
        if (slot != EquipmentSlot.MAINHAND || penalty <= 0) {
            return base;
        }
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.putAll(base);
        builder.put(Attributes.MOVEMENT_SPEED, modifier(penalty));
        return builder.build();
    }

    private MovementPenalty() {
    }
}
