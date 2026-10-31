package com.heavyartillery.modifier;

import com.heavyartillery.weapon.HeavyShotComponents;
import com.heavyartillery.weapon.trait.PowderRam;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.modifier.ValueStackModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Taking your time pays: up to +50% damage, building over 1.5 s since your last shot (see {@link PowderRam}). The
 * slow-firing counterpart of the parent's Mechanical Accelerator, and like it, each one fitted adds its bonus again.
 */
public final class PowderRamModifier extends ValueStackModifier {
    public static final double DAMAGE_BONUS = 0.5;

    public PowderRamModifier() {
        super(Map.of(
                HeavyShotComponents.POWDER_RAM, new ValueModifier(DAMAGE_BONUS, ValueModifier.Operation.ADD, ValueModifier.Type.BENEFICIAL)
        ));
    }

    @Override
    public void getDescriptionText(Consumer<Component> builder) {
        builder.accept(Component.translatable("artifice_heavy_artillery.modifier.powder_ram",
                Math.round(DAMAGE_BONUS * 100), PowderRam.CHARGE_TICKS / 20f).withStyle(ChatFormatting.GREEN));
    }
}
