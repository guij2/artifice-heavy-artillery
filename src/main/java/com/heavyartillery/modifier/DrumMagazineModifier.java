package com.heavyartillery.modifier;

import com.heavyartillery.weapon.HeavyShotComponents;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.modifier.ValueStackModifier;

import java.util.Map;

/**
 * A bigger magazine, slower to reload: +50% capacity, rounded up (see {@code trait.MagazineSize}). Stacks like the
 * parent's stat modifiers: each drum multiplies capacity and reload speed again.
 */
public final class DrumMagazineModifier extends ValueStackModifier {
    public static final double CAPACITY_BONUS = 0.5;
    public static final double RELOAD_PENALTY = 0.2;

    public DrumMagazineModifier() {
        super(Map.of(
                HeavyShotComponents.MAGAZINE_CAPACITY, new ValueModifier(CAPACITY_BONUS, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.BENEFICIAL),
                ShotComponents.RELOAD_SPEED_MULTIPLIER, new ValueModifier(-RELOAD_PENALTY, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.BENEFICIAL)
        ));
    }
}
