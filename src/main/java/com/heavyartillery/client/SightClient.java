package com.heavyartillery.client;

import com.heavyartillery.HeavyArtillery;
import com.heavyartillery.weapon.trait.SightAim;
import io.redspace.irons_artifice.client.gun.AttachmentGeoRenderer;
import io.redspace.irons_artifice.client.gun.AttachmentRenderableRegistry;
import io.redspace.irons_artifice.client.gun.SimpleItemGeoModel;
import io.redspace.irons_artifice.item.GunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Client side of {@link SightAim}: draws the sight on guns, tracks how far the local player has raised theirs for
 * {@link SightAimPose}, and draws its reticle in place of the crosshair once it is up.
 */
@EventBusSubscriber(modid = HeavyArtillery.MODID, value = Dist.CLIENT)
public final class SightClient {
    /** Ticks to raise the sight to the eye (and to lower it again). */
    private static final int RAISE_TICKS = 4;
    /** How far raised the sight must be before its reticle replaces the crosshair. */
    private static final float RETICLE_FROM = 0.75f;
    private static final int RETICLE_COLOR = 0xFFFF3A28;
    private static final int RETICLE_GLOW = 0x60FF3A28;

    /** How far the local player's sight is raised (0..1), last tick and this tick. */
    private static float raisedO;
    private static float raised;

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        AttachmentRenderableRegistry.register(SightAim.ATTACHMENT,
                new AttachmentGeoRenderer(new SimpleItemGeoModel<>(HeavyArtillery.MODID, "reflex_sight", "model/reflex_sight", "empty")));
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        boolean aiming = player != null && SightAim.isAiming(player) && !GunItem.isReloading(player.getUseItem());
        raisedO = raised;
        raised = Mth.clamp(raised + (aiming ? 1f : -1f) / RAISE_TICKS, 0, 1);
    }

    /** How far the local player's sight is raised, 0 (at the hip) to 1 (at the eye). */
    public static float raised(float partialTick) {
        return Mth.lerp(partialTick, raisedO, raised);
    }

    /**
     * Draws the sight's reticle instead of the parent's crosshair while the sight is up: a red dot with a faint glow,
     * which lands inside the sight's window ({@link SightAimPose} puts it in the middle of the screen).
     *
     * @return whether it was drawn (and the crosshair should not be)
     */
    public static boolean renderReticle(GuiGraphicsExtractor graphics, float partialTick) {
        if (raised(partialTick) < RETICLE_FROM || !Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            return false;
        }
        int x = graphics.guiWidth() / 2;
        int y = graphics.guiHeight() / 2;
        graphics.fill(x - 2, y - 1, x + 2, y + 1, RETICLE_GLOW);
        graphics.fill(x - 1, y - 2, x + 1, y + 2, RETICLE_GLOW);
        graphics.fill(x - 1, y - 1, x + 1, y + 1, RETICLE_COLOR);
        return true;
    }

    private SightClient() {
    }
}
