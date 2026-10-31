package com.heavyartillery.client;

import com.heavyartillery.weapon.HeavyGunItem;
import com.heavyartillery.weapon.HeavyHold;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Makes shot trails start at the muzzle of a held heavy gun. Shots leave from the shooter's eye, and the parent mod
 * draws their trails from there (nudged a little down and to the side, which suits its shoulder-fired guns), but heavy
 * guns are carried at the hip. Seen in third person (anyone else, or yourself in F5), trail particles on a heavy gun
 * user's line of fire are moved to come out of the gun's {@link HeavyHold#muzzle()} instead, joining the true line of
 * fire over {@link #BLEND} blocks; the stretch of the line that runs inside the gun is squeezed into the first bit past
 * the muzzle. First person is left as it is (the gun on screen is drawn in a different place).
 */
public final class MuzzleTrails {
    /** Blocks past the muzzle over which a trail joins the line of fire. */
    private static final double BLEND = 3;
    /** The stretch of the line of fire inside the gun is drawn in this many blocks past the muzzle. */
    private static final double SQUEEZE = 0.3;
    /** How far from a shooter's line of fire a trail particle can be and still be theirs (the parent's nudge is 0.35). */
    private static final double LINE_RADIUS = 1.5;

    /** Where a trail particle at {@code at} is drawn. */
    public static Vec3 fromHeldMuzzle(ClientLevel level, Vec3 at) {
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();
        for (Player player : level.players()) {
            if (player == mc.player && camera.getEntity() == player && !camera.isDetached()
                    || !(player.getMainHandItem().getItem() instanceof HeavyGunItem gun) || gun.traits().hold() == null) {
                continue;
            }
            Vec3 eye = player.getEyePosition();
            Vec3 look = player.getViewVector(1);
            Vec3 offset = at.subtract(eye);
            double along = offset.dot(look);
            if (along < -0.5 || along > 8 || offset.subtract(look.scale(along)).lengthSqr() > LINE_RADIUS * LINE_RADIUS) {
                continue;
            }
            Vec3 muzzle = HeavyArmPoses.heldPointInWorld(player, gun.traits().hold().muzzle(), 1);
            if (muzzle == null) {
                continue;
            }
            double muzzleAlong = muzzle.subtract(eye).dot(look);
            if (along < muzzleAlong) {
                return muzzle.add(look.scale(SQUEEZE * Math.max(0, along + 0.5) / (muzzleAlong + 0.5)));
            }
            double blend = 1 - (along - muzzleAlong) / BLEND;
            return blend <= 0 ? at : at.add(muzzle.subtract(eye.add(look.scale(muzzleAlong))).scale(blend));
        }
        return at;
    }

    private MuzzleTrails() {
    }
}
