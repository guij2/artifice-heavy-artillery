package com.heavyartillery.client;

import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.heavyartillery.HeavyArtillery;
import com.heavyartillery.weapon.trait.SightAim;
import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.irons_artifice.api.GunBones;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Optional;

/**
 * Raises a gun to the eye while its holder aims down its reflex sight ({@link SightAim}), in first person: the gun turns
 * level and straight ahead and slides over so the middle of the sight's window sits in the middle of the screen, a set
 * distance in front of the eye, whatever the gun and wherever its sight is mounted. Works from the gun model's optic
 * socket (the sight's mount), so it fits the parent's guns and other addons' alike; a model without one centres its
 * muzzle instead.
 * <p>
 * Applied to the gun's whole pose before its bones, so its own animations (recoil kick, cocking) still play on top, and
 * the first-person arms (drawn from the gun's bones) come along. It also holds the sight still against view bobbing and
 * hand sway while raised.
 */
public final class SightAimPose {
    /** The middle of the sight's window, above its mount, in pixels ({@code reflex_sight.geo.json}). */
    private static final float RETICLE_HEIGHT = 3.5f;
    /** Without an optic socket: this far above the muzzle socket, in pixels. */
    private static final float MUZZLE_SIGHT_HEIGHT = 1.5f;
    /** How far in front of the eye the raised sight sits, in blocks. */
    private static final float EYE_RELIEF = 0.85f;
    private static final Vector3f FORWARD = new Vector3f(0, 0, -1);

    private static final DataTicket<Float> RAISED = DataTicket.create(HeavyArtillery.id("sight_raised").toString(), Float.class);

    private SightAimPose() {
    }

    /**
     * Captures how far the sight is raised, for the local player's own gun in first person. Called from
     * {@code GunInHandRenderer.captureDefaultRenderState}.
     */
    public static void captureRenderState(GeoItemRenderer.RenderData renderData, GeoRenderState renderState, float partialTick) {
        ItemDisplayContext perspective = renderData.renderPerspective();
        if (perspective != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND && perspective != ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
            return;
        }
        if (renderData.itemOwner() != Minecraft.getInstance().player || !SightAim.hasSight(renderData.itemStack())) {
            return;
        }
        float raised = SightClient.raised(partialTick);
        if (raised > 0) {
            renderState.addGeckolibData(RAISED, smoothstep(raised));
        }
    }

    /**
     * Moves the gun's pose towards the aimed one. Called after {@code GunInHandRenderer.adjustRenderPose}, when the pose
     * stack holds the gun model's whole transform.
     */
    public static void adjustRenderPose(RenderPassInfo<GeoRenderState> renderPassInfo) {
        Float raised = renderPassInfo.getGeckolibData(RAISED);
        if (raised == null) {
            return;
        }
        Vector3f reticle = reticle(renderPassInfo.model());
        if (reticle == null) {
            return;
        }
        PoseStack.Pose last = renderPassInfo.poseStack().last();
        Matrix4f pose = last.pose();
        // the hand's pose stack is camera-centred but world-aligned (vanilla folds the inverse view rotation into it), so
        // work in view space, where the eye looks down -Z
        Quaternionf toWorld = new Quaternionf(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        Quaternionf toView = toWorld.conjugate(new Quaternionf());
        Matrix4f view = new Matrix4f().rotation(toView).mul(pose);
        Vector3f eye = view.transformPosition(reticle, new Vector3f());
        // the turn that levels the gun: its forward (model -Z) to straight ahead, then its up (model +Y) to straight up
        Vector3f forward = view.transformDirection(0, 0, -1, new Vector3f()).normalize();
        Vector3f up = view.transformDirection(0, 1, 0, new Vector3f()).normalize();
        Quaternionf level = new Quaternionf().rotationTo(forward, FORWARD);
        Vector3f upLevelled = level.transform(up);
        level.premul(new Quaternionf().rotationZ((float) Math.atan2(upLevelled.x, upLevelled.y)));

        Quaternionf turn = new Quaternionf().slerp(level, raised);
        Vector3f at = new Vector3f(eye).lerp(new Vector3f(0, 0, -EYE_RELIEF), raised);
        Matrix4f correction = new Matrix4f().rotation(toWorld).translate(at).rotate(turn).translate(-eye.x, -eye.y, -eye.z).rotate(toView);
        pose.mulLocal(correction);
        last.normal().mulLocal(new Matrix3f().rotation(toWorld).rotate(turn).rotate(toView));
    }

    /** The point to centre on screen, in the gun model's blocks: the sight's window, else just above the muzzle. */
    private static Vector3f reticle(BakedGeoModel model) {
        Optional<GeoBone> optic = model.getBone(GunBones.SOCKET_OPTIC);
        if (optic.isPresent()) {
            return pivot(optic.get(), RETICLE_HEIGHT);
        }
        return model.getBone(GunBones.SOCKET_MUZZLE).map(muzzle -> pivot(muzzle, MUZZLE_SIGHT_HEIGHT)).orElse(null);
    }

    /** A bone's pivot raised by {@code height} pixels, in blocks. */
    private static Vector3f pivot(GeoBone bone, float height) {
        return new Vector3f(bone.pivotX(), bone.pivotY() + height, bone.pivotZ()).div(16);
    }

    private static float smoothstep(float t) {
        return t * t * (3 - 2 * t);
    }
}
