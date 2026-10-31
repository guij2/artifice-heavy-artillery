package com.heavyartillery.datagen;

import com.heavyartillery.HeavyArtillery;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = HeavyArtillery.MODID)
public final class HADataGenerators {
    private HADataGenerators() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> registries = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new HAItemModelProvider(output, existingFileHelper));
        generator.addProvider(event.includeServer(), new HARecipeProvider(output, registries));
        generator.addProvider(event.includeServer(), new HAItemTagProvider(output, registries, existingFileHelper));
    }
}
