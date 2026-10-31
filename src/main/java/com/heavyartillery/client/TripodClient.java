package com.heavyartillery.client;

import com.heavyartillery.HeavyArtillery;
import com.heavyartillery.weapon.trait.TripodMount;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.client.player.Input;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;

/**
 * Client side of {@link TripodMount}: holds the local player still while mounted, and tracks how far each player's
 * tripod has unfolded for {@link PortableTripodRenderer}.
 */
@EventBusSubscriber(modid = HeavyArtillery.MODID, value = Dist.CLIENT)
public final class TripodClient {
    /** Ticks the tripod takes to unfold (and to fold back up). */
    private static final int DEPLOY_TICKS = 5;
    /** Per player id: how far their tripod has unfolded (0..1), last tick and this tick. */
    private static final Int2ObjectMap<float[]> DEPLOYED = new Int2ObjectOpenHashMap<>();

    /** Movement keys do nothing while mounted; sneak stays held, and releasing it stands the player up. */
    @SubscribeEvent
    static void onMovementInput(MovementInputUpdateEvent event) {
        if (!TripodMount.isMounted(event.getEntity())) {
            return;
        }
        Input input = event.getInput();
        input.up = input.down = input.left = input.right = input.jumping = false;
        input.forwardImpulse = 0;
        input.leftImpulse = 0;
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        var level = Minecraft.getInstance().level;
        if (level == null) {
            DEPLOYED.clear();
            return;
        }
        IntSet present = new IntOpenHashSet();
        for (Player player : level.players()) {
            present.add(player.getId());
            float[] deployed = DEPLOYED.computeIfAbsent(player.getId(), id -> new float[2]);
            deployed[0] = deployed[1];
            deployed[1] = Mth.clamp(deployed[1] + (TripodMount.isMounted(player) ? 1f : -1f) / DEPLOY_TICKS, 0, 1);
        }
        DEPLOYED.keySet().removeIf(id -> !present.contains(id));
    }

    /** How far an entity's tripod has unfolded, 0 (folded) to 1 (standing on the ground). */
    public static float deployed(int entityId, float partialTick) {
        float[] deployed = DEPLOYED.get(entityId);
        return deployed == null ? 0 : Mth.lerp(partialTick, deployed[0], deployed[1]);
    }

    private TripodClient() {
    }
}
