package com.heavyartillery.mixin;

import com.heavyartillery.client.SightClient;
import io.redspace.irons_artifice.client.gui.CrosshairRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A raised reflex sight's reticle replaces the parent's gun crosshair (see {@link SightClient#renderReticle}).
 */
// Cosmetic hooks (require = 0): if another mod reworks a target, only the effect is lost, not the game. Dev runs still
// fail on a miss (mixin.debug.countInjections).
@Mixin(value = CrosshairRenderer.class, remap = false)
public abstract class CrosshairRendererMixin {
    @Inject(method = "renderGunCrosshair", at = @At("HEAD"), cancellable = true, require = 0)
    private static void artifice_heavy_artillery$sightReticle(GuiGraphics graphics, CallbackInfoReturnable<Boolean> cir) {
        if (SightClient.renderReticle(graphics, Minecraft.getInstance().getFrameTime())) {
            cir.setReturnValue(true);
        }
    }
}
