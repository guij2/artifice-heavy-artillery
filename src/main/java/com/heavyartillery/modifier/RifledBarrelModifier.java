package com.heavyartillery.modifier;

import com.heavyartillery.HAConfig;
import com.heavyartillery.weapon.HeavyShotComponents;
import com.heavyartillery.weapon.trait.Rifling;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Shots hit harder the further they have flown: by default +2% damage per block, up to +60% at 30 blocks (see
 * {@link Rifling}). A rifled bore is slower to load, as it was for the real thing. Stacks: each barrel adds its
 * bonus per block again, and slows the reload again.
 */
public final class RifledBarrelModifier extends ConfiguredModifier {
    public RifledBarrelModifier() {
        super(() -> Map.of(
                HeavyShotComponents.RIFLING, new ValueModifier(HAConfig.get(HAConfig.RIFLED_DAMAGE_PER_BLOCK), ValueModifier.Operation.ADD, ValueModifier.Type.BENEFICIAL),
                ShotComponents.RELOAD_SPEED_MULTIPLIER, reloadPenalty()
        ));
    }

    private static ValueModifier reloadPenalty() {
        return new ValueModifier(-HAConfig.get(HAConfig.RIFLED_RELOAD_PENALTY), ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.BENEFICIAL);
    }

    /**
     * Says what the rifling does in words rather than as a "+0.02 Rifling" stat line.
     */
    @Override
    public void getDescriptionText(Consumer<Component> builder) {
        double perBlock = HAConfig.get(HAConfig.RIFLED_DAMAGE_PER_BLOCK);
        builder.accept(Component.translatable("artifice_heavy_artillery.modifier.rifled_barrel",
                ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(perBlock * 100), ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(perBlock * Rifling.maxDistance() * 100)).withStyle(ChatFormatting.GREEN));
        builder.accept(Utils.formatValueModifierDescription(reloadPenalty(), ShotComponents.RELOAD_SPEED_MULTIPLIER));
    }
}
