package com.heavyartillery.mixin;

import com.heavyartillery.client.PortableTripodRenderer;
import com.heavyartillery.client.SightAimPose;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.irons_artifice.client.gun.GunInHandRenderer;
import io.redspace.irons_artifice.item.GunItem;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/**
 * Draws the Portable Tripod alongside the parent's attachments, on every gun (see {@link PortableTripodRenderer}), and
 * raises a gun to the eye while aiming down its reflex sight ({@link SightAimPose}).
 * <p>
 * Extends the renderer's superclass for its current stack and perspective.
 */
@Mixin(GunInHandRenderer.class)
public abstract class GunInHandRendererMixin extends GeoItemRenderer<GunItem> {
    /** This render's tripod, if the gun has one. */
    @Unique
    private PortableTripodRenderer.@Nullable Plan artifice_heavy_artillery$tripod;

    private GunInHandRendererMixin(GeoModel<GunItem> model) {
        super(model);
    }

    @Inject(method = "preRender", at = @At("TAIL"))
    private void artifice_heavy_artillery$preRender(PoseStack poseStack, GunItem animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource,
                                                    @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight,
                                                    int packedOverlay, int colour, CallbackInfo ci) {
        if (isReRender || this.currentItemStack == null) {
            return;
        }
        this.artifice_heavy_artillery$tripod = PortableTripodRenderer.plan(animatable, model, this.currentItemStack, this.renderPerspective, partialTick);
        SightAimPose.adjustRenderPose(poseStack, model, this.currentItemStack, this.renderPerspective, partialTick);
    }

    @Inject(method = "handleAttachmentRendering", at = @At("HEAD"))
    private void artifice_heavy_artillery$renderTripod(PoseStack poseStack, GeoBone bone, MultiBufferSource bufferSource, float partialTick,
                                                       int packedLight, CallbackInfo ci) {
        if (this.artifice_heavy_artillery$tripod != null) {
            PortableTripodRenderer.renderAtBone(this.artifice_heavy_artillery$tripod, poseStack, bone, bufferSource, partialTick, packedLight);
        }
    }
}
