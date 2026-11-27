package com.heavyartillery.weapon;

import net.minecraft.util.Mth;

public final class HeavyMath {
    /**
     * Reductions never fully remove a stat, no matter how many modifiers stack.
     */
    public static final double MAX_REDUCTION = 0.9;

    public static double fraction(double value) {
        return Mth.clamp(value, 0, MAX_REDUCTION);
    }

    public static String percent(double fraction) {
        return "-" + Math.round(fraction * 100) + "%";
    }

    private HeavyMath() {
    }
}
