package com.heavyartillery.weapon.trait;

import com.heavyartillery.HeavyArtillery;
import com.heavyartillery.weapon.HeavyMath;
import com.heavyartillery.weapon.HeavyShotComponents;
import io.redspace.irons_artifice.api.ComposeShotEvent;
import io.redspace.irons_artifice.api.GunBones;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.item.AttachmentMap;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.registry.DataComponentRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * A gun fitted with a Reflex Sight aims down it while its holder holds use: like the parent's scope, but without the
 * zoom, the scope overlay or hiding the gun, so it works at any range. Spread and recoil drop by
 * {@link HeavyShotComponents#AIMED_SPREAD_REDUCTION} / {@link HeavyShotComponents#AIMED_RECOIL_REDUCTION} (stacking with
 * bracing and the tripod). Holding use is started in {@code GunItemMixin}; in first person the gun is raised so the
 * sight sits in the middle of the screen ({@code client.SightAimPose}), and its reticle replaces the crosshair.
 */
@EventBusSubscriber(modid = HeavyArtillery.MODID)
public final class SightAim {
    /** The sight's attachment id in the optic socket; also its model ({@code geckolib/models/item/reflex_sight.geo.json}). */
    public static final Identifier ATTACHMENT = HeavyArtillery.id("reflex_sight");
    /** Holding use aims for as long as it is held (like the parent's scope, which uses 1200). */
    public static final int USE_DURATION = 72000;

    /**
     * Whether a gun aims down a reflex sight: it has one in its optic socket and no scope (a scope takes over use).
     */
    public static boolean hasSight(ItemStack stack) {
        AttachmentMap attachments = stack.get(DataComponentRegistry.ATTACHMENT.get());
        return attachments != null && ATTACHMENT.equals(attachments.attachments().get(GunBones.SOCKET_OPTIC))
                && !GunItem.hasGunSpyglass(stack);
    }

    public static boolean isAiming(LivingEntity entity) {
        return entity.isUsingItem() && hasSight(entity.getUseItem());
    }

    @SubscribeEvent
    static void onCompose(ComposeShotEvent event) {
        if (!isAiming(event.getEntity())) {
            return;
        }
        ShotProfile profile = event.getShotProfile();
        double spread = HeavyMath.fraction(profile.value(HeavyShotComponents.AIMED_SPREAD_REDUCTION));
        if (spread > 0) {
            profile.modifyValue(ShotComponents.SPREAD, new ValueModifier(-spread, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.HARMFUL));
        }
        double recoil = HeavyMath.fraction(profile.value(HeavyShotComponents.AIMED_RECOIL_REDUCTION));
        if (recoil > 0) {
            profile.modifyValue(ShotComponents.CAMERA_RECOIL_MULTIPLIER, new ValueModifier(-recoil, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.HARMFUL));
        }
    }

    private SightAim() {
    }
}
