package com.heavyartillery.modifier;

import com.heavyartillery.HAConfig;
import com.heavyartillery.weapon.HeavyShotComponents;
import com.heavyartillery.weapon.trait.PowderRam;
import io.redspace.irons_artifice.data.ValueModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Taking your time pays: by default up to +50% damage, building over 1.5 s since your last shot (see
 * {@link PowderRam}). The slow-firing counterpart of the parent's Mechanical Accelerator. Each one fitted adds its
 * bonus and its charge time again: two give up to +100% over 3 s.
 */
public final class PowderRamModifier extends ConfiguredModifier {
    public PowderRamModifier() {
        super(() -> Map.of(
                HeavyShotComponents.POWDER_RAM, new ValueModifier(HAConfig.get(HAConfig.POWDER_RAM_DAMAGE_BONUS), ValueModifier.Operation.ADD),
                HeavyShotComponents.POWDER_RAM_CHARGE, new ValueModifier(HAConfig.get(HAConfig.POWDER_RAM_CHARGE_TICKS), ValueModifier.Operation.ADD)
        ));
    }

    @Override
    public void getDescriptionText(Consumer<Component> builder) {
        builder.accept(Component.translatable("artifice_heavy_artillery.modifier.powder_ram",
                ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(HAConfig.get(HAConfig.POWDER_RAM_DAMAGE_BONUS) * 100),
                ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(HAConfig.get(HAConfig.POWDER_RAM_CHARGE_TICKS) / 20.0)).withStyle(ChatFormatting.GREEN));
    }
}
