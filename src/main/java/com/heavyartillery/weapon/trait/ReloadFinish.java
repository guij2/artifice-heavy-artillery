package com.heavyartillery.weapon.trait;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * The gun whose reload is being finished on this thread, set around the parent's {@code attemptFinishReload} call by
 * {@code ServerEventsMixin}.
 * <p>
 * Hooks inside that method do not run when another mod replaces its body: Iron's Arms 'n Artifice: Spellbullet cancels it
 * for every player and finishes the reload itself, reading the plain magazine capacity. While a gun is set here,
 * {@code GunItemMixin} makes {@code GunItem.magazineCapacity()} answer with that gun's {@link MagazineSize}, so a Drum
 * Magazine fills whoever finishes the reload.
 */
public final class ReloadFinish {
    private static final ThreadLocal<ItemStack> GUN = new ThreadLocal<>();

    public static <T> T run(ItemStack gun, Supplier<T> finish) {
        ItemStack outer = GUN.get();
        GUN.set(gun);
        try {
            return finish.get();
        } finally {
            if (outer == null) {
                GUN.remove();
            } else {
                GUN.set(outer);
            }
        }
    }

    /** {@return the gun being finished on this thread, or null outside a reload finish} */
    public static @Nullable ItemStack gun() {
        return GUN.get();
    }

    private ReloadFinish() {
    }
}
