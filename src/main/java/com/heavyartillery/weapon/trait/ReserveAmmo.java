package com.heavyartillery.weapon.trait;

import com.heavyartillery.weapon.HeavyShotComponents;
import io.redspace.irons_artifice.data.PlayableSound;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.GunplayManager;
import io.redspace.irons_artifice.registry.IronsArtificeSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.function.IntConsumer;

/**
 * Reload-time ammo cost, driven by {@link HeavyShotComponents#RESERVE_AMMO_COST}.
 * <p>
 * Iron's Arms 'n Artifice reloads by counting the bullets a player carries and then consuming as many as it loads, with no
 * event in between. {@code GunplayManagerMixin} wraps those two calls and routes them through here, so any modifier that
 * lowers the cost works on every gun. {@code SpellBulletReloadHelperMixin} does the same for Iron's Arms 'n Artifice:
 * Spellbullet, which counts and takes bullets itself.
 */
public final class ReserveAmmo {
    private ReserveAmmo() {
    }

    /**
     * {@return the composed cost for this gun: bullets per loaded round, at least 0}
     */
    public static double cost(Player player, ItemStack gun) {
        if (!(gun.getItem() instanceof GunItem gunItem)) {
            return 1;
        }
        double cost = GunplayManager.compose(player, gunItem.getGun(), gun).value(HeavyShotComponents.RESERVE_AMMO_COST);
        return Math.max(0, cost);
    }

    /**
     * How many rounds a reload may load with {@code carried} bullets in the inventory. At zero cost reloads need no bullets
     * at all; otherwise the cost decides how far the carried bullets stretch (less than one round each, or several bullets
     * per round).
     */
    public static int effectiveAvailable(int carried, double cost) {
        if (cost <= 0) {
            return Integer.MAX_VALUE;
        }
        if (cost == 1 || carried <= 0) {
            return carried;
        }
        return (int) Math.min(Integer.MAX_VALUE, Math.floor(carried / cost + 1e-9));
    }

    /**
     * How many bullets {@code loaded} rounds cost: the whole part of the cost for every round, plus one more for each round
     * with probability equal to its fractional part.
     */
    public static int toConsume(int loaded, double cost, RandomSource random) {
        if (cost <= 0) {
            return 0;
        }
        double whole = Math.floor(cost);
        double fraction = cost - whole;
        long paid = (long) (loaded * whole);
        if (fraction > 1e-9) {
            for (int i = 0; i < loaded; i++) {
                if (random.nextDouble() < fraction) {
                    paid++;
                }
            }
        }
        return (int) Math.min(Integer.MAX_VALUE, paid);
    }

    /**
     * Takes what {@code loaded} rounds cost through {@code consume} (the parent's bullet take, or another mod's), with the
     * refund chime for rounds that came free.
     */
    public static void pay(Player player, ItemStack gun, int loaded, IntConsumer consume) {
        double cost = cost(player, gun);
        int paid = toConsume(loaded, cost, player.getRandom());
        if (paid > 0) {
            consume.accept(paid);
        }
        if (cost < 1) {
            onRefund(player, loaded - paid);
        }
    }

    /**
     * Feedback when a reload came (partly) free: the parent's enchanted-bullet chime.
     */
    public static void onRefund(Player player, int refunded) {
        if (refunded <= 0 || player.level().isClientSide()) {
            return;
        }
        PlayableSound.of(IronsArtificeSounds.INFINITY_BULLET, 0.6f, 0.9f, 1.1f).play(player.level(), player.position(), SoundSource.PLAYERS);
    }
}
