package com.heavyartillery.datagen;

import com.heavyartillery.HeavyArtillery;
import com.heavyartillery.crafting.FullBulletContainerIngredient;
import com.heavyartillery.crafting.StoredEnchantmentIngredient;
import com.heavyartillery.registry.HAItems;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.concurrent.CompletableFuture;

public class HARecipeProvider extends RecipeProvider {
    public HARecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output, HolderLookup.Provider registries) {
        /* **********************************
         * Components
         ********************************** */
        // Heavy Components: next tier after Clockwork Components. Forged steel, forge heat (blaze), copper pipework.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, HAItems.HEAVY_COMPONENTS.get())
                .pattern("IBI")
                .pattern("CMC")
                .pattern("IBI")
                .define('I', commonTag("ingots/iron"))
                .define('B', Items.BLAZE_ROD)
                .define('C', commonTag("ingots/copper"))
                .define('M', ItemRegistry.CLOCKWORK_COMPONENTS)
                .unlockedBy("has_clockwork_components", has(ItemRegistry.CLOCKWORK_COMPONENTS))
                .save(output);

        /* **********************************
         * Weapons
         ********************************** */
        // Gunsteel Gatling: barrels (iron blocks), top magazine (hopper), heavy components for the drive, netherite receiver, wooden stock
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, HAItems.GUNSTEEL_GATLING.get())
                .pattern("BBH")
                .pattern("MNM")
                .pattern(" L ")
                .define('B', commonTag("storage_blocks/iron"))
                .define('H', Items.HOPPER)
                .define('M', HAItems.HEAVY_COMPONENTS)
                .define('N', commonTag("ingots/netherite"))
                .define('L', ItemTags.LOGS)
                .unlockedBy("has_heavy_components", has(HAItems.HEAVY_COMPONENTS))
                .save(output);
        // Gunsteel Crucible: nozzle (iron block), a charge of lava, a blast furnace firebox, heavy components, netherite, wooden grip
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, HAItems.GUNSTEEL_CRUCIBLE.get())
                .pattern("ILF")
                .pattern("MNM")
                .pattern(" W ")
                .define('I', commonTag("storage_blocks/iron"))
                .define('L', Items.LAVA_BUCKET)
                .define('F', Items.BLAST_FURNACE)
                .define('M', HAItems.HEAVY_COMPONENTS)
                .define('N', commonTag("ingots/netherite"))
                .define('W', ItemTags.LOGS)
                .unlockedBy("has_heavy_components", has(HAItems.HEAVY_COMPONENTS))
                .save(output);

        // Gunsteel Congreve: the tube (iron blocks), a firework rocket, heavy components, netherite, wooden grips
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, HAItems.GUNSTEEL_CONGREVE.get())
                .pattern("IIR")
                .pattern("MNM")
                .pattern(" W ")
                .define('I', commonTag("storage_blocks/iron"))
                .define('R', Items.FIREWORK_ROCKET)
                .define('M', HAItems.HEAVY_COMPONENTS)
                .define('N', commonTag("ingots/netherite"))
                .define('W', ItemTags.LOGS)
                .unlockedBy("has_heavy_components", has(HAItems.HEAVY_COMPONENTS))
                .save(output);

        /* **********************************
         * Modifiers
         ********************************** */
        // Enchanted Magazine: four full bullet boxes (1024 rounds, consumed), four enchanted bullets, bound by Infinity
        HolderLookup.RegistryLookup<Enchantment> enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, HAItems.ENCHANTED_MAGAZINE.get())
                .pattern("BEB")
                .pattern("EIE")
                .pattern("BEB")
                .define('B', FullBulletContainerIngredient.INSTANCE.toVanilla())
                .define('E', ItemRegistry.ENCHANTED_BULLET_MODIFIER)
                .define('I', new StoredEnchantmentIngredient(enchantments.getOrThrow(Enchantments.INFINITY), 1).toVanilla())
                .unlockedBy("has_enchanted_bullet_modifier", has(ItemRegistry.ENCHANTED_BULLET_MODIFIER))
                .save(output);
        // Portable Tripod: shaped like one, a Heavy Components head on three iron legs
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, HAItems.PORTABLE_TRIPOD.get())
                .pattern(" H ")
                .pattern(" I ")
                .pattern("I I")
                .define('H', HAItems.HEAVY_COMPONENTS)
                .define('I', commonTag("ingots/iron"))
                .unlockedBy("has_heavy_components", has(HAItems.HEAVY_COMPONENTS))
                .save(output);
        // Reflex Sight: a glass pane in a copper frame, on a simple mechanism (the parent's scope's tier)
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, HAItems.REFLEX_SIGHT.get())
                .pattern("CGC")
                .pattern(" M ")
                .define('C', commonTag("ingots/copper"))
                .define('G', commonTag("glass_panes"))
                .define('M', ItemRegistry.SIMPLE_MECHANICAL_COMPONENTS)
                .unlockedBy("has_simple_mechanical_components", has(ItemRegistry.SIMPLE_MECHANICAL_COMPONENTS))
                .save(output);
        // Drum Magazine: a copper-banded iron drum round the heavy components that wind it
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, HAItems.DRUM_MAGAZINE.get())
                .pattern("ICI")
                .pattern("CHC")
                .pattern("ICI")
                .define('I', commonTag("ingots/iron"))
                .define('C', commonTag("ingots/copper"))
                .define('H', HAItems.HEAVY_COMPONENTS)
                .unlockedBy("has_heavy_components", has(HAItems.HEAVY_COMPONENTS))
                .save(output);
        // Rifled Barrel: a long barrel (iron blocks) with heavy components to cut the grooves
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, HAItems.RIFLED_BARREL.get())
                .pattern("B")
                .pattern("H")
                .pattern("B")
                .define('B', commonTag("storage_blocks/iron"))
                .define('H', HAItems.HEAVY_COMPONENTS)
                .unlockedBy("has_heavy_components", has(HAItems.HEAVY_COMPONENTS))
                .save(output);
        // Powder Ram: a rammer head on a staff, packed with blackpowder
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, HAItems.POWDER_RAM.get())
                .pattern("PHP")
                .pattern("PSP")
                .pattern(" S ")
                .define('P', ItemRegistry.BLACKPOWDER)
                .define('H', HAItems.HEAVY_COMPONENTS)
                .define('S', Items.STICK)
                .unlockedBy("has_heavy_components", has(HAItems.HEAVY_COMPONENTS))
                .save(output);
    }

    private static TagKey<Item> commonTag(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
    }
}
