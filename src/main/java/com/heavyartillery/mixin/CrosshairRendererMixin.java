package com.heavyartillery.mixin;

import com.heavyartillery.client.SightClient;
import io.redspace.irons_artifice.client.gui.CrosshairRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A raised reflex sight's reticle replaces the parent's gun crosshair (see {@link SightClient#renderReticle}).
 */
@Mixin(CrosshairRenderer.class)
public abstract class CrosshairRendererMixin {
    @Inject(method = "renderGunCrosshair", at = @At("HEAD"), cancellable = true)
    private static void artifice_heavy_artillery$sightReticle(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfoReturnable<Boolean> cir) {
        if (SightClient.renderReticle(graphics, deltaTracker.getGameTimeDeltaPartialTick(false))) {
            cir.setReturnValue(true);
        }
    }
}
