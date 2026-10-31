package com.heavyartillery.weapon.trait;

import com.heavyartillery.HeavyArtillery;
import com.heavyartillery.weapon.HeavyGunItem;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Heavy weapons with {@link com.heavyartillery.weapon.HeavyTraits#sprintLockedWhileFiring()} stop the holder from
 * sprinting while they keep firing. Ending a sprint happens here; preventing a new one from starting is done
 * client-side in {@code LocalPlayerMixin}, since vanilla re-starts sprinting from input every tick.
 */
@EventBusSubscriber(modid = HeavyArtillery.MODID)
public final class SprintLock {

    public static boolean isSprintLocked(Player player) {
        return player.getMainHandItem().getItem() instanceof HeavyGunItem gun
                && gun.traits().sprintLockedWhileFiring()
                && FiringStreak.isSustainingFire(player);
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.isSprinting() && isSprintLocked(player)) {
            player.setSprinting(false);
        }
    }

    private SprintLock() {
    }
}
