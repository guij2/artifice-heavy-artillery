package com.heavyartillery.client;

import com.heavyartillery.weapon.HeavyShotComponents;
import com.heavyartillery.weapon.ShotModelHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.redspace.irons_artifice.entity.Bullet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

/**
 * Draws shots that have a {@link HeavyShotComponents#SHOT_MODEL} (a rocket), pointing the way they fly. Replaces the
 * parent's renderer for its bullets, which draws nothing: shots without a model still draw nothing.
 * <p>
 * Shots leave from the shooter's eye; like the parent's trails, the model is drawn nudged down and to the side for its
 * first {@link Bullet#TRAIL_COMPENSATION_TICKS} ticks so it comes out of the gun, and not at all for the first tick,
 * when it would be in the shooter's face.
 */
public class ShotModelRenderer extends EntityRenderer<Bullet, ShotModelRenderer.ShotRenderState> {
    /** Bullets are tiny, so vanilla would stop drawing them 16 blocks away. */
    private static final double MAX_DISTANCE = 160;
    /** Turns about its own axis per tick, like a spin-stabilised war rocket. */
    private static final float SPIN_PER_TICK = 18;

    private final ItemModelResolver itemModelResolver;
    private final Map<Identifier, ItemStack> stacks = new HashMap<>();

    public ShotModelRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public boolean shouldRender(Bullet bullet, Frustum culler, double camX, double camY, double camZ) {
        return ((ShotModelHolder) bullet).artifice_heavy_artillery$shotModel() != null
                && bullet.distanceToSqr(camX, camY, camZ) < MAX_DISTANCE * MAX_DISTANCE
                && culler.isVisible(bullet.getBoundingBox().inflate(1));
    }

    @Override
    protected int getBlockLightLevel(Bullet bullet, BlockPos blockPos) {
        return 15; // its motor is alight
    }

    @Override
    public void extractRenderState(Bullet bullet, ShotRenderState state, float partialTicks) {
        super.extractRenderState(bullet, state, partialTicks);
        Identifier model = ((ShotModelHolder) bullet).artifice_heavy_artillery$shotModel();
        state.visible = model != null && state.ageInTicks >= 1;
        if (!state.visible) {
            return;
        }
        ItemStack stack = stacks.computeIfAbsent(model, id -> {
            ItemStack display = new ItemStack(Items.STICK);
            display.set(DataComponents.ITEM_MODEL, id);
            return display;
        });
        itemModelResolver.updateForNonLiving(state.item, stack, ItemDisplayContext.NONE, bullet);
        Vec3 motion = bullet.getDeltaMovement();
        state.direction = motion.lengthSqr() < 1.0E-6 ? new Vec3(0, 0, 1) : motion.normalize();
        float compensation = 1 - state.ageInTicks / Bullet.TRAIL_COMPENSATION_TICKS;
        if (compensation > 0) {
            // the same nudge Bullet gives its trail
            Vec3 right = state.direction.cross(new Vec3(0, 1, 0)).normalize();
            state.offset = new Vec3(0, -0.25, 0).add(right.scale(0.25)).scale(compensation);
        } else {
            state.offset = Vec3.ZERO;
        }
    }

    @Override
    public void submit(ShotRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.visible) {
            Vec3 dir = state.direction;
            poseStack.pushPose();
            poseStack.translate(state.offset.x, state.offset.y, state.offset.z);
            // the model points to -Z (north), like a gun model's muzzle
            poseStack.mulPose(Axis.YP.rotation((float) Math.atan2(-dir.x, -dir.z)));
            poseStack.mulPose(Axis.XP.rotation((float) Math.atan2(dir.y, Math.hypot(dir.x, dir.z))));
            poseStack.mulPose(Axis.ZP.rotationDegrees(state.ageInTicks * SPIN_PER_TICK));
            // the item model is drawn about its centre; move its front edge (z = 0, the rocket's point) onto the shot,
            // so the rest of it trails behind and the point is what touches what the shot hits
            poseStack.translate(0, 0, 0.5);
            state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            poseStack.popPose();
        }
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    @Override
    public ShotRenderState createRenderState() {
        return new ShotRenderState();
    }

    public static class ShotRenderState extends EntityRenderState {
        public final ItemStackRenderState item = new ItemStackRenderState();
        public boolean visible;
        public Vec3 direction = Vec3.ZERO;
        public Vec3 offset = Vec3.ZERO;
    }
}
