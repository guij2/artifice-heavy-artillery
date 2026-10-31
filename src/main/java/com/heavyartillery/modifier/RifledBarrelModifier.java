package com.heavyartillery.modifier;

import com.heavyartillery.weapon.HeavyShotComponents;
import com.heavyartillery.weapon.trait.Rifling;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.modifier.ValueStackModifier;
import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Shots hit harder the further they have flown: +2% damage per block, up to +60% at 30 blocks (see
 * {@link Rifling}). A rifled bore is slower to load, as it was for the real thing. Stacks: each barrel adds its
 * bonus per block again, and slows the reload again.
 */
public final class RifledBarrelModifier extends ValueStackModifier {
    public static final double DAMAGE_PER_BLOCK = 0.02;
    public static final ValueModifier RELOAD_PENALTY = new ValueModifier(-0.15, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.BENEFICIAL);

    public RifledBarrelModifier() {
        super(Map.of(
                HeavyShotComponents.RIFLING, new ValueModifier(DAMAGE_PER_BLOCK, ValueModifier.Operation.ADD, ValueModifier.Type.BENEFICIAL),
                ShotComponents.RELOAD_SPEED_MULTIPLIER, RELOAD_PENALTY
        ));
    }

    /**
     * Says what the rifling does in words rather than as a "+0.02 Rifling" stat line.
     */
    @Override
    public void getDescriptionText(Consumer<Component> builder) {
        builder.accept(Component.translatable("artifice_heavy_artillery.modifier.rifled_barrel",
                Math.round(DAMAGE_PER_BLOCK * 100), Math.round(DAMAGE_PER_BLOCK * Rifling.MAX_DISTANCE * 100)).withStyle(ChatFormatting.GREEN));
        builder.accept(Utils.formatValueModifierDescription(RELOAD_PENALTY, ShotComponents.RELOAD_SPEED_MULTIPLIER));
    }
}
