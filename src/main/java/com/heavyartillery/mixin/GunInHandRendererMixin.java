package com.heavyartillery.mixin;

import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.heavyartillery.client.PortableTripodRenderer;
import com.heavyartillery.client.SightAimPose;
import io.redspace.irons_artifice.client.gun.GunInHandRenderer;
import io.redspace.irons_artifice.item.GunItem;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws the Portable Tripod alongside the parent's attachments, on every gun (see {@link PortableTripodRenderer}), and
 * raises a gun to the eye while aiming down its reflex sight ({@link SightAimPose}).
 */
@Mixin(GunInHandRenderer.class)
public abstract class GunInHandRendererMixin {
    @Inject(method = "captureDefaultRenderState", at = @At("TAIL"))
    private void artifice_heavy_artillery$captureTripod(GunItem animatable, GeoItemRenderer.RenderData renderData, GeoRenderState renderState,
                                                        float partialTick, CallbackInfo ci) {
        PortableTripodRenderer.captureRenderState(animatable, renderData, renderState, partialTick);
        SightAimPose.captureRenderState(renderData, renderState, partialTick);
    }

    @Inject(method = "adjustRenderPose", at = @At("TAIL"))
    private void artifice_heavy_artillery$raiseSight(RenderPassInfo<GeoRenderState> renderPassInfo, CallbackInfo ci) {
        SightAimPose.adjustRenderPose(renderPassInfo);
    }

    @Inject(method = "handleAttachmentRendering", at = @At("HEAD"))
    private void artifice_heavy_artillery$renderTripod(RenderPassInfo<GeoRenderState> renderPassInfo, SubmitNodeCollector renderTasks, CallbackInfo ci) {
        PortableTripodRenderer.addToRenderPass(renderPassInfo);
    }
}
