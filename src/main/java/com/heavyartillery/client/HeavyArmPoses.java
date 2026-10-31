package com.heavyartillery.client;

import com.geckolib.animatable.GeoItem;
import com.geckolib.animation.AnimationController;
import com.geckolib.constant.DataTickets;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.heavyartillery.weapon.HeavyGunItem;
import com.heavyartillery.weapon.HeavyHold;
import com.heavyartillery.weapon.HeavyHoldAdjuster;
import io.redspace.irons_artifice.api.GunAnimations;
import io.redspace.irons_artifice.api.GunBones;
import io.redspace.irons_artifice.item.GunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

/**
 * Third-person hip-carry pose for heavy weapons with a {@link HeavyHold}: the gun points where its holder looks, the
 * holding arm carries it at the hip and the other arm reaches for the support grip.
 * <p>
 * Part of the head's pitch goes to the holding arm and the rest tilts the gun about its grip ({@link #tiltGun}), so the
 * gun can point straight up or down without swinging through the holder's head.
 * <p>
 * Everything is in HumanoidModel space, in pixels: y down, forward -z, the player's right -x. The support hand is placed
 * by following the gun through vanilla's held-item transform (arm, then {@code ItemInHandLayer}'s hand offset, then the
 * item's display transform) and aiming the arm at the result.
 */
public final class HeavyArmPoses {
    /**
     * Rest rotation of the holding arm (forward and inward, hand at the hip). Together with the thirdperson_righthand
     * rotation in {@code heavy_gun_display.json} [90 + deg(X), deg(Y), 0] it levels the gun; change them together.
     */
    private static final float HOLD_X_ROT = -0.3f;
    private static final float HOLD_Y_ROT = -0.5f;
    /** thirdperson_righthand in {@code heavy_gun_display.json}. */
    private static final Vector3f DISPLAY_TRANSLATION = new Vector3f(0, 0, 1);
    private static final Quaternionfc DISPLAY_ROTATION = new Quaternionf().rotationXYZ(72.81f * Mth.DEG_TO_RAD, -28.65f * Mth.DEG_TO_RAD, 0);
    private static final float DISPLAY_SCALE = 0.8f;
    /** ItemInHandLayer: the right hand's item offset, after turning the arm frame into the item frame. */
    private static final Vector3f HAND_OFFSET = new Vector3f(1, 2, -10);
    private static final Quaternionfc ARM_TO_ITEM = new Quaternionf().rotationX(-Mth.HALF_PI).rotateY(Mth.PI);
    /** AvatarRenderer shrinks the player model to this. */
    private static final float PLAYER_SCALE = 0.9375f;
    /** GeckoLib draws item models 0.01 blocks up. */
    private static final float GECKOLIB_Y_OFFSET = 0.16f;

    /** Share of the head's pitch the holding arm takes when looking up and down; the gun tilts the rest. */
    private static final float ARM_PITCH_UP = 0.5f;
    private static final float ARM_PITCH_DOWN = 0.7f;
    /** The support shoulder comes forward and in a little (like the parent's rifle pose), so the hand reaches the grip. */
    private static final Vector3f SUPPORT_SHOULDER_OFFSET = new Vector3f(-1, 0, -2);
    /** The support hand's grip point in its arm's space (the arm box is centred 1 px off the pivot). */
    private static final Vector3f SUPPORT_HAND = new Vector3f(1, 10.5f, 0);
    /** How far the support shoulder may follow the hand when the grip is out of reach. */
    private static final float MAX_SHOULDER_STRETCH = 2;
    /** Spots tried along the support grip. */
    private static final int GRIP_SAMPLES = 9;

    /** Seconds on the reload timeline the support hand takes to move onto the crank, and back to the grip after it. */
    private static final double CRANK_REACH_START = 0.15;
    private static final double CRANK_REACH_TIME = 0.3;

    private HeavyArmPoses() {
    }

    /**
     * Poses both arms if the gun held in {@code arm} has a {@link HeavyHold}.
     *
     * @return whether it did; if not, the caller's own pose should apply
     */
    public static boolean apply(HumanoidModel<?> model, HumanoidRenderState state, HumanoidArm arm) {
        ItemStack stack = state.getUseItemStackForArm(arm);
        if (!(stack.getItem() instanceof HeavyGunItem gun) || gun.traits().hold() == null) {
            return false;
        }
        HeavyHold hold = gun.traits().hold();
        float side = arm == HumanoidArm.RIGHT ? 1 : -1;  // the left-handed pose is the mirror image
        ModelPart holding = model.getArm(arm);
        ModelPart support = model.getArm(arm.getOpposite());

        boolean tilts = gun.getGun().animationAdjusters().contains(HeavyHoldAdjuster.TILT);
        float pitch = model.head.xRot;
        float tilt = tilts ? gunTilt(pitch) : 0;
        Matrix3f holdRotation = new Matrix3f().rotationY(model.head.yRot).rotateX(tilts ? armPitch(pitch) : pitch)
                .rotateY(side * HOLD_Y_ROT).rotateX(HOLD_X_ROT);
        Vector3f euler = holdRotation.getEulerAnglesZYX(new Vector3f());
        holding.xRot = euler.x;
        holding.yRot = euler.y;
        holding.zRot = euler.z;

        support.x += side * SUPPORT_SHOULDER_OFFSET.x;
        support.y += SUPPORT_SHOULDER_OFFSET.y;
        support.z += SUPPORT_SHOULDER_OFFSET.z;
        Vector3f supportPivot = new Vector3f(support.x, support.y, support.z);
        Vector3f holdingPivot = new Vector3f(holding.x, holding.y, holding.z);
        Vector3f supportHand = new Vector3f(side * SUPPORT_HAND.x, SUPPORT_HAND.y, SUPPORT_HAND.z);

        // gun model point -> model space
        Function<Vec3, Vector3f> place = point -> holdRotation.transform(gunToArm(point, side, tilt)).add(holdingPivot);
        Vector3f target = place.apply(bestGripSpot(hold, place, supportPivot, supportHand.length()));
        Vec3 crank = crankPoint(hold, gun, stack);
        if (crank != null) {
            target.lerp(place.apply(crank), (float) crankWeight(hold, gun, stack));
        }

        float stretch = Math.min(Math.max(0, target.distance(supportPivot) - supportHand.length()), MAX_SHOULDER_STRETCH);
        if (stretch > 0) {
            Vector3f toward = target.sub(supportPivot, new Vector3f()).normalize(stretch);
            support.x += toward.x;
            support.y += toward.y;
            support.z += toward.z;
        }
        aimArm(support, target, supportHand);

        // HumanoidModel adds these after the arm poses; take them out so the hands stay on the gun
        for (ModelPart part : new ModelPart[]{model.rightArm, model.leftArm}) {
            float bob = part == model.rightArm ? 1 : -1;
            part.zRot -= bob * (Mth.cos(state.ageInTicks * 0.09F) * 0.05F + 0.05F);
            part.xRot -= bob * (Mth.sin(state.ageInTicks * 0.067F) * 0.05F);
            if (state.isCrouching) {
                part.xRot -= 0.4f;
            }
        }
        return true;
    }

    /**
     * {@link HeavyHoldAdjuster#TILT}: in third person, pivots the gun about its grip (the root bone's origin) by the part of
     * its holder's pitch the arm doesn't take.
     */
    public static void tiltGun(RenderPassInfo<GeoRenderState> renderPassInfo, BoneSnapshots snapshots) {
        ItemDisplayContext perspective = renderPassInfo.getGeckolibData(DataTickets.ITEM_RENDER_PERSPECTIVE);
        Integer ownerId = renderPassInfo.getGeckolibData(GunItem.ITEM_OWNER_ID_TICKET);
        if (perspective != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND && perspective != ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || ownerId == null || Minecraft.getInstance().level == null) {
            return;
        }
        Entity owner = Minecraft.getInstance().level.getEntity(ownerId);
        if (owner == null) {
            return;
        }
        float pitch = owner.getXRot(renderPassInfo.renderState().getPartialTick()) * Mth.DEG_TO_RAD;
        snapshots.get(GunBones.ROOT).ifPresent(root -> root.setRotation(gunTilt(pitch), 0, 0));
    }

    /**
     * Where a point on a held heavy gun (gun model pixels, such as its muzzle) is in the world, as the gun is drawn in
     * third person by {@link #apply} and {@link #tiltGun}, or null if the player isn't holding a heavy gun with a
     * {@link HeavyHold}. Follows AvatarRenderer: the model turned with the body, flipped, shrunk to player size and
     * raised 1.501 blocks (2 px lower when crouching).
     */
    public static @Nullable Vec3 heldPointInWorld(Player player, Vec3 point, float partialTick) {
        if (!(player.getMainHandItem().getItem() instanceof HeavyGunItem gun) || gun.traits().hold() == null) {
            return null;
        }
        float side = player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
        float bodyYaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
        float headYaw = Mth.wrapDegrees(Mth.rotLerp(partialTick, player.yHeadRotO, player.yHeadRot) - bodyYaw) * Mth.DEG_TO_RAD;
        float pitch = player.getXRot(partialTick) * Mth.DEG_TO_RAD;
        boolean tilts = gun.getGun().animationAdjusters().contains(HeavyHoldAdjuster.TILT);
        Matrix3f holdRotation = new Matrix3f().rotationY(headYaw).rotateX(tilts ? armPitch(pitch) : pitch)
                .rotateY(side * HOLD_Y_ROT).rotateX(HOLD_X_ROT);
        boolean crouching = player.isCrouching();
        // the holding arm's pivot in HumanoidModel (lower when crouching)
        Vector3f model = holdRotation.transform(gunToArm(point, side, tilts ? gunTilt(pitch) : 0)).add(-5 * side, crouching ? 5.2f : 2, 0);
        Vector3f local = new Vector3f(-model.x / 16, 1.501f - model.y / 16, model.z / 16).mul(PLAYER_SCALE)
                .rotateY((180 - bodyYaw) * Mth.DEG_TO_RAD);
        return player.getPosition(partialTick).add(local.x, local.y - (crouching ? 2 / 16f : 0), local.z);
    }

    private static float armPitch(float pitch) {
        return pitch * (pitch < 0 ? ARM_PITCH_UP : ARM_PITCH_DOWN);
    }

    /** The gun's tilt about its own X axis (positive raises the barrels) for a head pitch (positive looks down). */
    private static float gunTilt(float pitch) {
        return -(pitch - armPitch(pitch));
    }

    /** The spot along the support grip whose distance from the shoulder best matches the arm's reach. */
    private static Vec3 bestGripSpot(HeavyHold hold, Function<Vec3, Vector3f> place, Vector3f shoulder, float reach) {
        Vec3 best = hold.supportGripFrom();
        float bestError = Float.MAX_VALUE;
        for (int i = 0; i < GRIP_SAMPLES; i++) {
            Vec3 spot = hold.supportGripFrom().lerp(hold.supportGripTo(), i / (GRIP_SAMPLES - 1.0));
            float error = Math.abs(place.apply(spot).distance(shoulder) - reach);
            if (error < bestError) {
                best = spot;
                bestError = error;
            }
        }
        return best;
    }

    /** Where the crank handle is during a reload, in gun model pixels, or null when there is no crank or no reload. */
    private static @Nullable Vec3 crankPoint(HeavyHold hold, HeavyGunItem gun, ItemStack stack) {
        HeavyHold.Crank crank = hold.reloadCrank();
        double time = crank != null ? reloadAnimationTime(gun, stack) : -1;
        if (time < 0) {
            return null;
        }
        double turn = easeInOutQuad(Mth.clamp((time - crank.turnStart()) / (crank.turnEnd() - crank.turnStart()), 0, 1));
        // top -> front -> bottom, as the crank bone turns ([360, 0, 0] in the animation); Vec3.xRot turns the other way round to JOML
        return crank.handle().subtract(crank.hub()).xRot((float) (turn * Mth.TWO_PI)).add(crank.hub());
    }

    /** How far the support hand has moved from its grip onto the crank (0..1). */
    private static double crankWeight(HeavyHold hold, HeavyGunItem gun, ItemStack stack) {
        double time = reloadAnimationTime(gun, stack);
        return smoothstep((time - CRANK_REACH_START) / CRANK_REACH_TIME)
                * (1 - smoothstep((time - hold.reloadCrank().turnEnd()) / CRANK_REACH_TIME));
    }

    /** Seconds into the reload animation, or -1. Read from the animation (synced to every viewer), not the stack's ReloadState. */
    private static double reloadAnimationTime(HeavyGunItem gun, ItemStack stack) {
        AnimationController<?> controller = gun.getAnimatableInstanceCache().getManagerForId(GeoItem.getId(stack))
                .getAnimationControllers().get(GunItem.TRIGGERED_ANIMATION_CONTROLLER);
        return controller != null && controller.isTriggeredAnimation(GunAnimations.RELOAD) ? controller.getCurrentAnimationTime() : -1;
    }

    /** A gun model point (Blockbench pixels) in the holding arm's space, with the gun tilted about its grip. */
    private static Vector3f gunToArm(Vec3 point, float side, float tilt) {
        Vector3f v = new Vector3f((float) point.x, (float) point.y, (float) point.z).rotateX(tilt);
        v.add(0, GECKOLIB_Y_OFFSET, 0).mul(DISPLAY_SCALE);
        DISPLAY_ROTATION.transform(v).add(DISPLAY_TRANSLATION).add(HAND_OFFSET);
        ARM_TO_ITEM.transform(v);
        v.x *= side;
        return v;
    }

    /** Rotates an arm (no roll) so that {@code hand}, a point in its own space, lands on {@code target}. */
    private static void aimArm(ModelPart arm, Vector3f target, Vector3f hand) {
        Vector3f pivot = new Vector3f(arm.x, arm.y, arm.z);
        Vector3f aim = new Vector3f(target);
        Matrix3f rotation = new Matrix3f();
        for (int i = 0; i < 3; i++) {  // the hand sits off the arm's axis, so nudge the aim until it lands
            Vector3f dir = aim.sub(pivot, new Vector3f()).normalize();
            arm.xRot = -(float) Math.acos(Mth.clamp(dir.y, -1, 1));
            arm.yRot = (float) Mth.atan2(-dir.x, -dir.z);
            arm.zRot = 0;
            Vector3f landed = rotation.rotationY(arm.yRot).rotateX(arm.xRot).transform(hand, new Vector3f()).add(pivot);
            aim.add(target.sub(landed, new Vector3f()));
        }
    }

    private static double smoothstep(double t) {
        t = Mth.clamp(t, 0, 1);
        return t * t * (3 - 2 * t);
    }

    private static double easeInOutQuad(double t) {
        return t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2;
    }
}
