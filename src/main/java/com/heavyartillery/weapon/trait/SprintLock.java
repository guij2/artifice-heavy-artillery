package com.heavyartillery.weapon.trait;

import com.heavyartillery.HeavyArtillery;
import com.heavyartillery.weapon.HeavyGunItem;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;

/**
 * Heavy weapons with {@link com.heavyartillery.weapon.HeavyTraits#sprintLockedWhileFiring()} stop the holder from
 * sprinting while they keep firing. Ending a sprint happens here; preventing a new one from starting is done
 * client-side in {@code LocalPlayerMixin}, since vanilla re-starts sprinting from input every tick.
 */
@Mod.EventBusSubscriber(modid = HeavyArtillery.MODID)
public final class SprintLock {

    public static boolean isSprintLocked(Player player) {
        return player.getMainHandItem().getItem() instanceof HeavyGunItem gun
                && gun.traits().sprintLockedWhileFiring()
                && FiringStreak.isSustainingFire(player);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = event.player;
        if (player.isSprinting() && isSprintLocked(player)) {
            player.setSprinting(false);
        }
    }

    private SprintLock() {
    }
}
