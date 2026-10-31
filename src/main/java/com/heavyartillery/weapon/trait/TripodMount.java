package com.heavyartillery.weapon.trait;

import com.heavyartillery.HeavyArtillery;
import com.heavyartillery.weapon.HeavyMath;
import com.heavyartillery.weapon.HeavyShotComponents;
import io.redspace.irons_artifice.api.ComposeShotEvent;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.item.AttachmentMap;
import io.redspace.irons_artifice.registry.DataComponentRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * A gun fitted with a Portable Tripod is mounted while its holder crouches on the ground: spread and recoil drop by
 * {@link HeavyShotComponents#MOUNTED_SPREAD_REDUCTION} / {@link HeavyShotComponents#MOUNTED_RECOIL_REDUCTION} (stacking
 * with {@link Brace}), and the holder can't move or jump until they stand up. Movement is client-authoritative, so the
 * lock is applied to the local player's input ({@code client.TripodClient}).
 */
@EventBusSubscriber(modid = HeavyArtillery.MODID)
public final class TripodMount {
    /**
     * The attachment slot the tripod fills. A gun model may have a bone with this name to say where the tripod clamps
     * on; otherwise a spot under the barrel is used ({@code client.PortableTripodRenderer}).
     */
    public static final String SOCKET = "attachment_tripod";

    public static boolean hasTripod(ItemStack stack) {
        AttachmentMap attachments = stack.get(DataComponentRegistry.ATTACHMENT.get());
        return attachments != null && attachments.attachments().containsKey(SOCKET);
    }

    /**
     * Crouching on the ground by holding sneak (not just squeezed under a low ceiling), not riding anything.
     */
    public static boolean isInMountingStance(LivingEntity entity) {
        return entity.isShiftKeyDown() && entity.isCrouching() && entity.onGround() && !entity.isPassenger();
    }

    /**
     * In the mounting stance with a tripod gun in the main hand.
     */
    public static boolean isMounted(LivingEntity entity) {
        return isInMountingStance(entity) && hasTripod(entity.getMainHandItem());
    }

    @SubscribeEvent
    static void onCompose(ComposeShotEvent event) {
        if (!isInMountingStance(event.getEntity())) {
            return;
        }
        ShotProfile profile = event.getShotProfile();
        double spread = HeavyMath.fraction(profile.value(HeavyShotComponents.MOUNTED_SPREAD_REDUCTION));
        if (spread > 0) {
            profile.modifyValue(ShotComponents.SPREAD, new ValueModifier(-spread, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.HARMFUL));
        }
        double recoil = HeavyMath.fraction(profile.value(HeavyShotComponents.MOUNTED_RECOIL_REDUCTION));
        if (recoil > 0) {
            profile.modifyValue(ShotComponents.CAMERA_RECOIL_MULTIPLIER, new ValueModifier(-recoil, ValueModifier.Operation.MULTIPLY_TOTAL, ValueModifier.Type.HARMFUL));
        }
    }

    private TripodMount() {
    }
}
