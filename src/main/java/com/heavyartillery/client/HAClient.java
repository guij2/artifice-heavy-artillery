package com.heavyartillery.client;

import com.heavyartillery.HeavyArtillery;
import com.heavyartillery.client.particle.MoltenSprayParticle;
import com.heavyartillery.registry.HAParticles;
import com.heavyartillery.weapon.trait.SightAim;
import io.redspace.irons_artifice.client.gun.AttachmentGeoRenderer;
import io.redspace.irons_artifice.client.gun.AttachmentRenderableRegistry;
import io.redspace.irons_artifice.client.gun.SimpleItemGeoModel;
import io.redspace.irons_artifice.registry.EntityRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

/** Client registration (mod bus). */
@EventBusSubscriber(modid = HeavyArtillery.MODID, value = Dist.CLIENT)
public final class HAClient {

    @SubscribeEvent
    static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(HAParticles.MOLTEN_SPRAY.get(), MoltenSprayParticle.Provider::new);
    }

    /** Runs after Iron's Arms 'n Artifice's (we load after it), so this replaces its invisible bullet renderer. */
    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.BULLET.get(), ShotModelRenderer::new);
        AttachmentRenderableRegistry.register(SightAim.ATTACHMENT,
                new AttachmentGeoRenderer(new SimpleItemGeoModel<>(HeavyArtillery.MODID, "reflex_sight", "model/reflex_sight", "empty")));
    }

    /** The shot models {@link ShotModelRenderer} draws, which no item uses. */
    @SubscribeEvent
    static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(ShotModelRenderer.modelLocation(HeavyArtillery.id("congreve_rocket")));
    }

    private HAClient() {
    }
}
