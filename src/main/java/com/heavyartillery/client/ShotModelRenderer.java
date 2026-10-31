package com.heavyartillery.client;

import com.heavyartillery.weapon.HeavyShotComponents;
import com.heavyartillery.weapon.ShotModelHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.redspace.irons_artifice.entity.Bullet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Draws shots that have a {@link HeavyShotComponents#SHOT_MODEL} (a rocket), pointing the way they fly. Replaces the
 * parent's renderer for its bullets, which draws nothing: shots without a model still draw nothing.
 * <p>
 * Shots leave from the shooter's eye; like the parent's trails, the model is drawn nudged down and to the side for its
 * first {@link Bullet#TRAIL_COMPENSATION_TICKS} ticks so it comes out of the gun, and not at all for the first tick,
 * when it would be in the shooter's face.
 * <p>
 * A shot model {@code <namespace>:<name>} is the item model {@code <namespace>:item/<name>}, loaded as a standalone
 * model ({@link HAClient} registers them).
 */
public class ShotModelRenderer extends EntityRenderer<Bullet> {
    /** Bullets are tiny, so vanilla would stop drawing them 16 blocks away. */
    private static final double MAX_DISTANCE = 160;
    /** Turns about its own axis per tick, like a spin-stabilised war rocket. */
    private static final float SPIN_PER_TICK = 18;
    /** The item renderer only draws a non-empty stack; the model drawn is the shot's, not this item's. */
    private static final ItemStack CARRIER = new ItemStack(Items.STICK);

    public ShotModelRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    /** The standalone model a shot model id names. */
    public static ModelResourceLocation modelLocation(ResourceLocation shotModel) {
        return ModelResourceLocation.standalone(shotModel.withPrefix("item/"));
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
    public void render(Bullet bullet, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        ResourceLocation id = ((ShotModelHolder) bullet).artifice_heavy_artillery$shotModel();
        float ageInTicks = bullet.tickCount + partialTick;
        if (id != null && ageInTicks >= 1) {
            BakedModel model = Minecraft.getInstance().getModelManager().getModel(modelLocation(id));
            Vec3 motion = bullet.getDeltaMovement();
            Vec3 dir = motion.lengthSqr() < 1.0E-6 ? new Vec3(0, 0, 1) : motion.normalize();
            Vec3 offset = Vec3.ZERO;
            float compensation = 1 - ageInTicks / Bullet.TRAIL_COMPENSATION_TICKS;
            if (compensation > 0) {
                // the same nudge Bullet gives its trail
                Vec3 right = dir.cross(new Vec3(0, 1, 0)).normalize();
                offset = new Vec3(0, -0.25, 0).add(right.scale(0.25)).scale(compensation);
            }
            poseStack.pushPose();
            poseStack.translate(offset.x, offset.y, offset.z);
            // the model points to -Z (north), like a gun model's muzzle
            poseStack.mulPose(Axis.YP.rotation((float) Math.atan2(-dir.x, -dir.z)));
            poseStack.mulPose(Axis.XP.rotation((float) Math.atan2(dir.y, Math.hypot(dir.x, dir.z))));
            poseStack.mulPose(Axis.ZP.rotationDegrees(ageInTicks * SPIN_PER_TICK));
            // the item model is drawn about its centre; move its front edge (z = 0, the rocket's point) onto the shot,
            // so the rest of it trails behind and the point is what touches what the shot hits
            poseStack.translate(0, 0, 0.5);
            Minecraft.getInstance().getItemRenderer().render(CARRIER, ItemDisplayContext.NONE, false, poseStack, bufferSource, packedLight,
                    OverlayTexture.NO_OVERLAY, model);
            poseStack.popPose();
        }
        super.render(bullet, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull Bullet bullet) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
