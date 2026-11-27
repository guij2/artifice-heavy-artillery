package com.heavyartillery.gametest;

import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** What the tests need that 1.20.1's {@link GameTestHelper} lacks. */
final class TestSupport {
    /** Fails unless {@code actual} equals {@code expected}, like the helper's {@code assertValueEqual} in later versions. */
    static void assertValueEqual(GameTestHelper helper, Object actual, Object expected, String name) {
        if (!Objects.equals(actual, expected)) {
            throw new GameTestAssertException("Expected " + name + " to be " + expected + ", but was " + actual);
        }
    }

    /** A crafting grid of {@code width} x {@code height} holding {@code items}, row by row. */
    static CraftingContainer grid(int width, int height, List<ItemStack> items) {
        AbstractContainerMenu menu = new AbstractContainerMenu(null, -1) {
            @Override
            public ItemStack quickMoveStack(Player player, int index) {
                return ItemStack.EMPTY;
            }

            @Override
            public boolean stillValid(Player player) {
                return true;
            }
        };
        CraftingContainer grid = new TransientCraftingContainer(menu, width, height);
        for (int slot = 0; slot < items.size(); slot++) {
            grid.setItem(slot, items.get(slot));
        }
        return grid;
    }

    /** The id of the crafting recipe the grid makes, if any. */
    static Optional<ResourceLocation> craft(GameTestHelper helper, CraftingContainer grid) {
        Optional<CraftingRecipe> recipe = helper.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, helper.getLevel());
        return recipe.map(CraftingRecipe::getId);
    }

    private TestSupport() {
    }
}
