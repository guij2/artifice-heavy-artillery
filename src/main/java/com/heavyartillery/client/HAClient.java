package com.heavyartillery.client;

import com.heavyartillery.HeavyArtillery;
import com.heavyartillery.client.particle.MoltenSprayParticle;
import com.heavyartillery.registry.HAParticles;
import io.redspace.irons_artifice.registry.EntityRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

@EventBusSubscriber(modid = HeavyArtillery.MODID, value = Dist.CLIENT)
public final class HAClient {

    @SubscribeEvent
    static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(HAParticles.MOLTEN_SPRAY.get(), MoltenSprayParticle.Provider::new);
    }

    @SubscribeEvent
    static void registerRenderPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(MoltenSprayParticle.PIPELINE);
    }

    /** Runs after Iron's Arms 'n Artifice's (we load after it), so this replaces its invisible bullet renderer. */
    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.BULLET.get(), ShotModelRenderer::new);
    }

    private HAClient() {
    }
}
