package com.heavyartillery.weapon;

import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * How a heavy weapon is carried in third person: hip-carried, the trigger hand on the rear grip and the other hand on
 * a support grip (a carry handle, a foregrip...), the gun pointing where its holder looks. Without one, the parent mod's
 * rifle pose is used.
 * <p>
 * Points are in the gun model's Blockbench pixels, the same space as the first-person {@code left_arm} bone. To let the
 * gun point fully up and down without swinging through the holder, the GunProfile should also list
 * {@link HeavyHoldAdjuster#TILT}, which pivots the gun about its grip in third person.
 *
 * @param supportGripFrom one end of the support grip; the hand holds the spot along it its arm reaches most naturally
 * @param supportGripTo   the other end (the same point for a single-spot grip)
 * @param muzzle          where shots leave the gun (its {@code attachment_muzzle} bone): their trails start here when
 *                        the gun is seen in third person
 * @param reloadCrank     a crank the support hand turns while reloading, or null to keep it on the support grip
 */
public record HeavyHold(Vec3 supportGripFrom, Vec3 supportGripTo, Vec3 muzzle, @Nullable Crank reloadCrank) {

    /**
     * A crank on the side of the gun. The support hand moves onto {@code handle} for the reload and follows it around
     * {@code hub} (one turn about the model's X axis, eased in and out like the animation) between {@code turnStart}
     * and {@code turnEnd}, in seconds on the reload animation timeline.
     */
    public record Crank(Vec3 hub, Vec3 handle, double turnStart, double turnEnd) {
    }
}
