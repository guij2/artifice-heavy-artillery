package com.heavyartillery.client;

import com.heavyartillery.HeavyArtillery;
import com.heavyartillery.client.particle.MoltenSprayParticle;
import com.heavyartillery.registry.HAParticles;
import com.heavyartillery.weapon.trait.SightAim;
import io.redspace.irons_artifice.client.gun.AttachmentGeoRenderer;
import io.redspace.irons_artifice.client.gun.AttachmentRenderableRegistry;
import io.redspace.irons_artifice.client.gun.SimpleItemGeoModel;
import io.redspace.irons_artifice.registry.IronsArtificeEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;

/** Client registration (mod bus). */
@Mod.EventBusSubscriber(modid = HeavyArtillery.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class HAClient {

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(HAParticles.MOLTEN_SPRAY.get(), MoltenSprayParticle.Provider::new);
    }

    /** Runs after Iron's Arms 'n Artifice's (we load after it), so this replaces its invisible bullet renderer. */
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(IronsArtificeEntities.BULLET.get(), ShotModelRenderer::new);
        AttachmentRenderableRegistry.register(SightAim.ATTACHMENT,
                new AttachmentGeoRenderer(new SimpleItemGeoModel<>(HeavyArtillery.MODID, "reflex_sight", "model/reflex_sight", "empty")));
    }

    /** The shot models {@link ShotModelRenderer} draws, which no item uses. */
    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(ShotModelRenderer.modelLocation(HeavyArtillery.id("congreve_rocket")));
    }

    private HAClient() {
    }
}
