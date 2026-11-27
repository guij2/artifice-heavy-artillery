package com.heavyartillery.crafting;

import com.heavyartillery.HeavyArtillery;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegisterEvent;

/**
 * Custom recipe ingredients, usable in any recipe JSON as an ingredient object with {@code "type": "<id>"}.
 */
public final class HAIngredientTypes {
    public static final ResourceLocation FULL_BULLET_CONTAINER = HeavyArtillery.id("full_bullet_container");
    public static final ResourceLocation STORED_ENCHANTMENT = HeavyArtillery.id("stored_enchantment");

    private HAIngredientTypes() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener((RegisterEvent event) -> {
            if (event.getRegistryKey().equals(Registries.RECIPE_SERIALIZER)) {
                CraftingHelper.register(FULL_BULLET_CONTAINER, FullBulletContainerIngredient.SERIALIZER);
                CraftingHelper.register(STORED_ENCHANTMENT, StoredEnchantmentIngredient.SERIALIZER);
            }
        });
    }
}
