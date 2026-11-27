package com.heavyartillery.weapon;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Implemented by the parent's {@code Bullet} ({@code BulletMixin}): the {@link HeavyShotComponents#SHOT_MODEL} of a
 * shot, synced to clients so they can draw it in flight.
 */
public interface ShotModelHolder {
    @Nullable
    ResourceLocation artifice_heavy_artillery$shotModel();
}
