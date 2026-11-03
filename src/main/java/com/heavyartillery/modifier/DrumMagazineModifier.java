package com.heavyartillery.modifier;

import com.heavyartillery.HAConfig;
import com.heavyartillery.weapon.HeavyShotComponents;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;

import java.util.Map;

/**
 * A bigger magazine, slower to reload: +50% capacity by default, rounded up (see {@code trait.MagazineSize}). Stacks
 * like the parent's stat modifiers: each drum multiplies capacity and reload speed again.
 */
public final class DrumMagazineModifier extends ConfiguredModifier {
    public DrumMagazineModifier() {
        super(() -> Map.of(
                HeavyShotComponents.MAGAZINE_CAPACITY, new ValueModifier(HAConfig.get(HAConfig.DRUM_CAPACITY_BONUS), ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.BENEFICIAL),
                ShotComponents.RELOAD_SPEED_MULTIPLIER, new ValueModifier(-HAConfig.get(HAConfig.DRUM_RELOAD_PENALTY), ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.BENEFICIAL)
        ));
    }
}
