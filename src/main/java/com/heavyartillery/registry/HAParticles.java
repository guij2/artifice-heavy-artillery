package com.heavyartillery.registry;

import com.heavyartillery.HeavyArtillery;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Particle types. Each needs a provider in {@code client.HAClient} and a sprite list in {@code particles/<name>.json}.
 */
public final class HAParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, HeavyArtillery.MODID);

    /**
     * A billow of molten spray: white-hot, cooling through orange and red into smoke as it slows. Used as a shot's
     * particle trail (it carries on along the shot) and as the splash where molten shots land.
     */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> MOLTEN_SPRAY =
            PARTICLE_TYPES.register("molten_spray", () -> new SimpleParticleType(false));

    public static void register(IEventBus modEventBus) {
        PARTICLE_TYPES.register(modEventBus);
    }

    private HAParticles() {
    }
}
