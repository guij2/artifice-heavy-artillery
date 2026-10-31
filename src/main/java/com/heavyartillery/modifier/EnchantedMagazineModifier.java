package com.heavyartillery.modifier;

import com.heavyartillery.weapon.HeavyShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.modifier.ValueStackModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Infinite ammo: reloads are free and need no bullets in the inventory. Implemented as -100%
 * {@link HeavyShotComponents#RESERVE_AMMO_COST}, so it works on every gun.
 */
public final class EnchantedMagazineModifier extends ValueStackModifier {
    public EnchantedMagazineModifier() {
        super(Map.of(
                HeavyShotComponents.RESERVE_AMMO_COST, new ValueModifier(-1, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.HARMFUL)
        ));
    }

    /**
     * The effect line says it all; the "-100% Reserve Ammo Cost" stat line would only repeat it.
     */
    @Override
    public void getDescriptionText(Consumer<Component> builder) {
        builder.accept(Component.translatable("artifice_heavy_artillery.modifier.enchanted_magazine").withStyle(ChatFormatting.AQUA));
    }
}
