package com.heavyartillery.weapon.trait;

import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.MagazineContents;
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
    private static final ThreadLocal<Boolean> SIZING = ThreadLocal.withInitial(() -> false);

    public static <T> T run(ItemStack gun, Supplier<T> finish) {
        // a gun that was never fired has no magazine tag and reads as full at magazineCapacity(), which is the drummed
        // capacity during the finish: write down the rounds it holds first, so a drum does not make it look full
        if (gun.getItem() instanceof GunItem && (gun.getTag() == null || !gun.getTag().contains(MagazineContents.TAG_KEY))) {
            GunItem.setMagazine(gun, GunItem.getMagazine(gun));
        }
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

    /**
     * {@link MagazineSize} of the gun being finished. Working it out composes the gun's shot, which reads its magazine, and a
     * gun with no magazine tag counts as full at {@code magazineCapacity()}: asked again while sizing, that is the plain one.
     */
    public static int capacity(ItemStack gun, int base) {
        if (SIZING.get()) {
            return base;
        }
        SIZING.set(true);
        try {
            return MagazineSize.capacity(gun, base);
        } finally {
            SIZING.set(false);
        }
    }

    private ReloadFinish() {
    }
}
