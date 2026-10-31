package com.heavyartillery.weapon.trait;

import com.heavyartillery.HeavyArtillery;
import com.heavyartillery.registry.HAAttachments;
import com.heavyartillery.weapon.HeavyShotComponents;
import io.redspace.irons_artifice.api.ComposeShotEvent;
import io.redspace.irons_artifice.api.GunAboutToShootEvent;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.gun.ShotProfile;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Guns with {@link HeavyShotComponents#POWDER_RAM} hit harder the longer the shooter has held fire: the full bonus
 * after {@link #CHARGE_TICKS} without shooting, in proportion before that. The first shot ever counts as fully
 * charged. The slow-firing mirror of the parent's Mechanical Accelerator, built the same way (a damage multiplier
 * added while composing the shot).
 * <p>
 * The last shot's time is tracked from {@link GunAboutToShootEvent}, which fires on both sides, after the shot has
 * been composed, so the shot being composed still sees the time since the previous one.
 */
@EventBusSubscriber(modid = HeavyArtillery.MODID)
public final class PowderRam {
    public static final int CHARGE_TICKS = 30;

    @SubscribeEvent
    static void onCompose(ComposeShotEvent event) {
        ShotProfile profile = event.getShotProfile();
        double bonus = profile.value(HeavyShotComponents.POWDER_RAM) * charge(event.getEntity());
        if (bonus > 0) {
            profile.modifyValue(ShotComponents.DAMAGE, new ValueModifier(bonus, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.BENEFICIAL));
        }
    }

    /**
     * @return 0 right after a shot, 1 once {@link #CHARGE_TICKS} have passed
     */
    public static double charge(LivingEntity entity) {
        long last = entity.getData(HAAttachments.LAST_SHOT);
        if (last == Long.MIN_VALUE) {
            return 1;
        }
        long since = entity.level().getGameTime() - last;
        return Math.clamp(since / (double) CHARGE_TICKS, 0, 1);
    }

    // lowest priority, not receiving cancelled events: only count shots that really go off
    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onAboutToShoot(GunAboutToShootEvent event) {
        LivingEntity shooter = event.getEntity();
        shooter.setData(HAAttachments.LAST_SHOT, shooter.level().getGameTime());
    }

    private PowderRam() {
    }
}
