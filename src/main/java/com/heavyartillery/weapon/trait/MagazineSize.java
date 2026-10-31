package com.heavyartillery.weapon.trait;

import com.heavyartillery.weapon.HeavyShotComponents;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.GunplayManager;
import net.minecraft.world.item.ItemStack;

/**
 * Magazine capacity after {@link HeavyShotComponents#MAGAZINE_CAPACITY}. The parent mod reads a gun's capacity
 * straight from its {@code GunProfile}, so every place that does (reloads, the fire pipeline, tooltip, ammo HUD,
 * durability bar, Cowboy Hat, Tricorne, recoil pattern) is routed here by mixins.
 * <p>
 * Taking a drum off a loaded gun leaves the extra rounds in it until they are fired.
 */
public final class MagazineSize {

    public static int capacity(ItemStack gun, int base) {
        if (!(gun.getItem() instanceof GunItem gunItem)) {
            return base;
        }
        // no entity: only the modifiers' own values, without firing ComposeShotEvent
        return capacity(GunplayManager.compose(null, gunItem.getGun(), gun), base);
    }

    public static int capacity(ShotProfile profile, int base) {
        double multiplier = profile.value(HeavyShotComponents.MAGAZINE_CAPACITY);
        return Math.max(1, (int) Math.ceil(base * multiplier - 1e-6));
    }

    private MagazineSize() {
    }
}
