package com.heavyartillery.datagen;

import com.heavyartillery.HeavyArtillery;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = HeavyArtillery.MODID)
public final class HADataGenerators {
    private HADataGenerators() {
    }

    @SubscribeEvent
    public static void gatherClientData(GatherDataEvent.Client event) {
        event.createProvider(HAItemModelProvider::new);
        event.createProvider(HARecipeProvider.Runner::new);
        event.createProvider(HAItemTagProvider::new);
    }
}
