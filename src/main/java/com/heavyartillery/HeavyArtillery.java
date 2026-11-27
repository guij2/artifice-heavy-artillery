package com.heavyartillery;

import com.heavyartillery.crafting.HAIngredientTypes;
import com.heavyartillery.registry.HACreativeTabs;
import com.heavyartillery.registry.HAItems;
import com.heavyartillery.registry.HAParticles;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(HeavyArtillery.MODID)
public final class HeavyArtillery {
    public static final String MODID = "artifice_heavy_artillery";
    public static final Logger LOGGER = LogUtils.getLogger();

    public HeavyArtillery(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        HAItems.register(modEventBus);
        HAParticles.register(modEventBus);
        HAIngredientTypes.register(modEventBus);
        HACreativeTabs.register(modEventBus);
        context.getContainer().addConfig(new ModConfig(ModConfig.Type.SERVER, HAConfig.SPEC, context.getContainer()));
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }
}
