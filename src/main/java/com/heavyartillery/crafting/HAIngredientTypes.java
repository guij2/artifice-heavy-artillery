package com.heavyartillery.crafting;

import com.heavyartillery.HeavyArtillery;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Custom recipe ingredients, usable in any recipe JSON via {@code "neoforge:ingredient_type"}.
 */
public final class HAIngredientTypes {
    public static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, HeavyArtillery.MODID);

    public static final DeferredHolder<IngredientType<?>, IngredientType<FullBulletContainerIngredient>> FULL_BULLET_CONTAINER =
            INGREDIENT_TYPES.register("full_bullet_container", () -> new IngredientType<>(FullBulletContainerIngredient.CODEC));
    public static final DeferredHolder<IngredientType<?>, IngredientType<StoredEnchantmentIngredient>> STORED_ENCHANTMENT =
            INGREDIENT_TYPES.register("stored_enchantment", () -> new IngredientType<>(StoredEnchantmentIngredient.CODEC));

    private HAIngredientTypes() {
    }

    public static void register(IEventBus modEventBus) {
        INGREDIENT_TYPES.register(modEventBus);
    }
}
