package com.heavyartillery.weapon.trait;

import com.heavyartillery.HeavyArtillery;
import com.heavyartillery.registry.HAParticles;
import com.heavyartillery.weapon.HeavyShotComponents;
import io.redspace.irons_artifice.api.BulletImpactEvent;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.gun.ShotProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.EventHooks;

/**
 * Molten shots ({@link HeavyShotComponents#MOLTEN}) and setting blocks alight
 * ({@link HeavyShotComponents#IGNITE_BLOCK_CHANCE}).
 * <p>
 * A molten shot is still the parent mod's bullet, so damage, modifiers and on-hit effects all apply as usual; only its
 * block impact changes (see {@code BulletMixin}): a splash of slag and a sizzle instead of chipped stone.
 */
@EventBusSubscriber(modid = HeavyArtillery.MODID)
public final class MoltenShot {
    /** Bullet.tick discards a bullet slower than this (squared speed). */
    private static final double MIN_SPEED_SQR = 0.125;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onImpact(BulletImpactEvent event) {
        Bullet bullet = event.getBullet();
        if (event.isCanceled() || !(event.getRayTraceResult() instanceof BlockHitResult hit) || !(bullet.level() instanceof ServerLevel level)) {
            return;
        }
        double chance = bullet.getProfile().value(HeavyShotComponents.IGNITE_BLOCK_CHANCE);
        if (chance > 0 && bullet.getRandom().nextDouble() < chance) {
            ignite(level, bullet.getOwner(), hit);
        }
    }

    /** Sets fire to the block side, the way a fire charge does. */
    private static void ignite(ServerLevel level, Entity owner, BlockHitResult hit) {
        BlockPos pos = hit.getBlockPos().relative(hit.getDirection());
        if (owner instanceof Mob && !EventHooks.canEntityGrief(level, owner)
                || owner instanceof Player player && (!player.mayBuild() || !level.mayInteract(player, pos))
                || !level.isEmptyBlock(pos)) {
            return;
        }
        level.setBlockAndUpdate(pos, BaseFireBlock.getState(level, pos));
    }

    /**
     * Replaces a molten shot's block impact effects (break sound, stone chips).
     *
     * @return whether the shot is molten, and the parent's impact effects should be skipped
     */
    public static boolean blockImpact(Bullet bullet, BlockHitResult hit) {
        if (!bullet.getProfile().peek(HeavyShotComponents.MOLTEN)) {
            return false;
        }
        if (bullet.level() instanceof ServerLevel level) {
            RandomSource random = bullet.getRandom();
            Vec3 at = hit.getLocation().add(Vec3.atLowerCornerOf(hit.getDirection().getNormal()).scale(0.1));
            level.sendParticles(HAParticles.MOLTEN_SPRAY.get(), at.x, at.y, at.z, 1, 0.1, 0.1, 0.1, 0.02);
            level.sendParticles(ParticleTypes.SMOKE, at.x, at.y, at.z, 1, 0.1, 0.1, 0.1, 0.01);
            if (random.nextFloat() < 0.25f) {
                level.sendParticles(ParticleTypes.LAVA, at.x, at.y, at.z, 1, 0, 0, 0, 0);
            }
            // dozens of globs land each second: sizzle now and then, not on every one
            if (random.nextFloat() < 0.1f) {
                level.playSound(null, at.x, at.y, at.z, SoundEvents.LAVA_POP, SoundSource.NEUTRAL, 0.4f, 0.8f + random.nextFloat() * 0.4f);
            } else if (random.nextFloat() < 0.05f) {
                level.playSound(null, at.x, at.y, at.z, SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL, 0.25f, 1.5f + random.nextFloat() * 0.5f);
            }
        }
        return true;
    }

    /**
     * How far a shot flies on level ground before it slows to a stop, following Bullet.tick's motion (move, drag, gravity).
     *
     * @return the range in blocks, or infinity for shots that never slow down enough to stop
     */
    public static double range(ShotProfile profile) {
        double drag = profile.value(ShotComponents.BULLET_DRAG);
        double gravity = profile.value(ShotComponents.GRAVITY);
        Vec3 motion = new Vec3(0, 0, profile.value(ShotComponents.BULLET_SPEED));
        double distance = 0;
        for (int tick = 0; tick < 200; tick++) {
            distance += motion.z;
            motion = motion.scale(drag).subtract(0, gravity, 0);
            if (motion.lengthSqr() < MIN_SPEED_SQR) {
                return distance;
            }
        }
        return Double.POSITIVE_INFINITY;
    }

    private MoltenShot() {
    }
}
