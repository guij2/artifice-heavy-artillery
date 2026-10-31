package com.heavyartillery;

import com.heavyartillery.crafting.HAIngredientTypes;
import com.heavyartillery.registry.HAAttachments;
import com.heavyartillery.registry.HACreativeTabs;
import com.heavyartillery.registry.HAItems;
import com.heavyartillery.registry.HAParticles;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(HeavyArtillery.MODID)
public final class HeavyArtillery {
    public static final String MODID = "artifice_heavy_artillery";
    public static final Logger LOGGER = LogUtils.getLogger();

    public HeavyArtillery(IEventBus modEventBus, ModContainer modContainer) {
        HAItems.register(modEventBus);
        HAParticles.register(modEventBus);
        HAIngredientTypes.register(modEventBus);
        HAAttachments.register(modEventBus);
        HACreativeTabs.register(modEventBus);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
