package com.heavyartillery.datagen;

import com.heavyartillery.HeavyArtillery;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = HeavyArtillery.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
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
        generator.addProvider(event.includeServer(), new HARecipeProvider(output));
        generator.addProvider(event.includeServer(), new HAItemTagProvider(output, registries, existingFileHelper));
    }
}
