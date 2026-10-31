package com.heavyartillery.gametest;

import com.heavyartillery.HeavyArtillery;
import com.heavyartillery.crafting.FullBulletContainerIngredient;
import com.heavyartillery.crafting.StoredEnchantmentIngredient;
import com.heavyartillery.modifier.EnchantedMagazineModifier;
import com.heavyartillery.registry.HAItems;
import io.redspace.irons_artifice.data.ReloadResult;
import io.redspace.irons_artifice.item.BulletContainerContents;
import io.redspace.irons_artifice.item.BulletContainerItem;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.GunplayManager;
import io.redspace.irons_artifice.item.MagazineContents;
import io.redspace.irons_artifice.menu.GunContainer;
import io.redspace.irons_artifice.registry.DataComponentRegistry;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.List;

/**
 * Enchanted Magazine: free reloads (no bullets needed), on our guns and the parent's, and its recipe.
 */
final class EnchantedMagazineTests {
    private EnchantedMagazineTests() {
    }

    static void freeReloadWithOneBullet(GameTestHelper helper) {
        Player player = survivalPlayer(helper, gunWith(HAItems.GUNSTEEL_GATLING.get(), 0, HAItems.ENCHANTED_MAGAZINE.get()));
        player.getInventory().add(new ItemStack(ItemRegistry.BULLET.get(), 1));
        ItemStack gun = player.getMainHandItem();

        helper.assertValueEqual(GunplayManager.attemptFinishReload(player, gun, 0), ReloadResult.FINISHED_RELOAD, "reload result");
        helper.assertValueEqual(GunItem.getMagazine(gun).count(), magazineCapacity(gun), "a single carried bullet fills the whole magazine");
        helper.assertValueEqual(GunplayManager.countBullets(player), 1, "the carried bullet is not consumed");
        helper.succeed();
    }

    static void freeReloadFromBulletBox(GameTestHelper helper) {
        Player player = survivalPlayer(helper, gunWith(HAItems.GUNSTEEL_GATLING.get(), 0, HAItems.ENCHANTED_MAGAZINE.get()));
        ItemStack box = fullBulletBox();
        player.getInventory().add(box);
        ItemStack gun = player.getMainHandItem();

        helper.assertValueEqual(GunplayManager.attemptFinishReload(player, gun, 0), ReloadResult.FINISHED_RELOAD, "reload result");
        helper.assertValueEqual(GunItem.getMagazine(gun).count(), magazineCapacity(gun), "magazine filled");
        helper.assertValueEqual(GunplayManager.countBullets(player), 256, "the bullet box is not drained");
        helper.succeed();
    }

    static void freeReloadWithNoBullets(GameTestHelper helper) {
        Player player = survivalPlayer(helper, gunWith(HAItems.GUNSTEEL_GATLING.get(), 0, HAItems.ENCHANTED_MAGAZINE.get()));
        ItemStack gun = player.getMainHandItem();

        helper.assertValueEqual(GunplayManager.attemptStartReload(player, gun), ReloadResult.STARTING_RELOAD, "a reload starts with no bullets at all");
        helper.assertValueEqual(GunplayManager.attemptFinishReload(player, gun, 0), ReloadResult.FINISHED_RELOAD, "and finishes");
        helper.assertValueEqual(GunItem.getMagazine(gun).count(), magazineCapacity(gun), "magazine filled from nothing");
        helper.succeed();
    }

    static void noBulletsNoReloadWithoutModifier(GameTestHelper helper) {
        Player player = survivalPlayer(helper, gunWith(HAItems.GUNSTEEL_GATLING.get(), 0));
        ItemStack gun = player.getMainHandItem();

        helper.assertValueEqual(GunplayManager.attemptStartReload(player, gun), ReloadResult.NO_AMMO, "without the modifier, no bullets means no reload");
        helper.assertValueEqual(GunItem.getMagazine(gun).count(), 0, "magazine still empty");
        helper.succeed();
    }

    static void descriptionIsOneLine(GameTestHelper helper) {
        List<Component> lines = new ArrayList<>();
        new EnchantedMagazineModifier().getDescriptionText(lines::add);
        helper.assertValueEqual(lines.size(), 1, "the description is just the effect line");
        helper.assertTrue(lines.getFirst().getContents() instanceof TranslatableContents contents
                && contents.getKey().equals("artifice_heavy_artillery.modifier.enchanted_magazine"), "the line is the effect text");
        helper.succeed();
    }

    static void reloadWithoutModifierConsumes(GameTestHelper helper) {
        Player player = survivalPlayer(helper, gunWith(HAItems.GUNSTEEL_GATLING.get(), 0));
        player.getInventory().add(new ItemStack(ItemRegistry.BULLET.get(), 64));
        player.getInventory().add(new ItemStack(ItemRegistry.BULLET.get(), 36));
        ItemStack gun = player.getMainHandItem();
        int capacity = magazineCapacity(gun);

        helper.assertValueEqual(GunplayManager.attemptFinishReload(player, gun, 0), ReloadResult.FINISHED_RELOAD, "reload result");
        helper.assertValueEqual(GunItem.getMagazine(gun).count(), capacity, "magazine filled");
        helper.assertValueEqual(GunplayManager.countBullets(player), 100 - capacity, "a normal reload pays one bullet per round");
        helper.succeed();
    }

    static void reloadWithoutModifierIsCapped(GameTestHelper helper) {
        Player player = survivalPlayer(helper, gunWith(HAItems.GUNSTEEL_GATLING.get(), 0));
        player.getInventory().add(new ItemStack(ItemRegistry.BULLET.get(), 10));
        ItemStack gun = player.getMainHandItem();

        GunplayManager.attemptFinishReload(player, gun, 0);
        helper.assertValueEqual(GunItem.getMagazine(gun).count(), 10, "a normal reload loads only what is carried");
        helper.assertValueEqual(GunplayManager.countBullets(player), 0, "all carried bullets used");
        helper.succeed();
    }

    static void worksOnParentGuns(GameTestHelper helper) {
        Player player = survivalPlayer(helper, gunWith(ItemRegistry.CLOCKWORK_RIFLE.get(), 0, HAItems.ENCHANTED_MAGAZINE.get()));
        player.getInventory().add(new ItemStack(ItemRegistry.BULLET.get(), 1));
        ItemStack gun = player.getMainHandItem();

        helper.assertValueEqual(GunplayManager.attemptFinishReload(player, gun, 0), ReloadResult.FINISHED_RELOAD, "reload result");
        helper.assertValueEqual(GunItem.getMagazine(gun).count(), magazineCapacity(gun), "clockwork rifle filled");
        helper.assertValueEqual(GunplayManager.countBullets(player), 1, "the carried bullet is not consumed");
        helper.succeed();
    }

    static void fullBulletBoxIngredient(GameTestHelper helper) {
        FullBulletContainerIngredient ingredient = FullBulletContainerIngredient.INSTANCE;
        helper.assertFalse(ingredient.test(new ItemStack(ItemRegistry.BULLET_BOX.get())), "an empty bullet box is rejected");
        helper.assertFalse(ingredient.test(bulletBox(255)), "a bullet box one short of full is rejected");
        helper.assertTrue(ingredient.test(fullBulletBox()), "a full bullet box is accepted");
        helper.assertFalse(ingredient.test(new ItemStack(ItemRegistry.BULLET.get(), 64)), "loose bullets are rejected");
        helper.succeed();
    }

    static void infinityBookIngredient(GameTestHelper helper) {
        StoredEnchantmentIngredient ingredient = new StoredEnchantmentIngredient(enchantment(helper, Enchantments.INFINITY), 1);
        helper.assertTrue(ingredient.test(book(helper, Enchantments.INFINITY)), "an Infinity book is accepted");
        helper.assertTrue(ingredient.test(book(helper, Enchantments.INFINITY, Enchantments.MENDING)), "a book with Infinity and more is accepted");
        helper.assertFalse(ingredient.test(book(helper, Enchantments.MENDING)), "a book without Infinity is rejected");
        helper.assertFalse(ingredient.test(new ItemStack(Items.BOOK)), "a plain book is rejected");
        helper.succeed();
    }

    static void recipeCraftsEnchantedMagazine(GameTestHelper helper) {
        ResourceLocation expected = HeavyArtillery.id("enchanted_magazine_modifier");
        var recipes = helper.getLevel().getServer().getRecipeManager();

        CraftingInput valid = recipeGrid(helper, fullBulletBox());
        var match = recipes.getRecipeFor(RecipeType.CRAFTING, valid, helper.getLevel());
        helper.assertTrue(match.isPresent() && match.get().id().equals(expected), "four full boxes, four enchanted bullets and an Infinity book craft the Enchanted Magazine");

        CraftingInput shortBox = recipeGrid(helper, bulletBox(255));
        helper.assertTrue(recipes.getRecipeFor(RecipeType.CRAFTING, shortBox, helper.getLevel()).isEmpty(), "a box that is not full does not craft");
        helper.succeed();
    }

    /* **********************************
     * Fixtures
     ********************************** */

    /**
     * A survival player that is never added to the server's player list: {@code makeMockServerPlayerInLevel} logs in a
     * fake connection, and Iron's Lib's login sync throws on it.
     */
    private static Player survivalPlayer(GameTestHelper helper, ItemStack gun) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemSlot(EquipmentSlot.MAINHAND, gun);
        helper.assertFalse(player.hasInfiniteMaterials(), "test player must pay for ammo");
        return player;
    }

    private static ItemStack gunWith(Item gun, int rounds, Item... modifiers) {
        ItemStack stack = new ItemStack(gun);
        GunItem.setMagazine(stack, new MagazineContents(rounds));
        if (modifiers.length == 0) {
            return stack;
        }
        GunContainer container = new GunContainer(stack);
        for (int slot = 0; slot < modifiers.length; slot++) {
            container.setItem(slot, new ItemStack(modifiers[slot]));
        }
        container.setChanged();
        return container.getGunStack();
    }

    private static int magazineCapacity(ItemStack gun) {
        return ((GunItem) gun.getItem()).magazineCapacity();
    }

    private static ItemStack fullBulletBox() {
        return bulletBox(BulletContainerItem.contents(new ItemStack(ItemRegistry.BULLET_BOX.get())).capacity());
    }

    private static ItemStack bulletBox(int bullets) {
        ItemStack box = new ItemStack(ItemRegistry.BULLET_BOX.get());
        int capacity = BulletContainerItem.contents(box).capacity();
        List<ItemStack> stacks = new ArrayList<>();
        for (int remaining = bullets; remaining > 0; remaining -= 64) {
            stacks.add(new ItemStack(ItemRegistry.BULLET.get(), Math.min(64, remaining)));
        }
        box.set(DataComponentRegistry.BULLET_POUCH.get(), new BulletContainerContents(capacity, stacks));
        return box;
    }

    private static Holder<Enchantment> enchantment(GameTestHelper helper, ResourceKey<Enchantment> key) {
        return helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
    }

    @SafeVarargs
    private static ItemStack book(GameTestHelper helper, ResourceKey<Enchantment>... keys) {
        ItemEnchantments.Mutable stored = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        for (ResourceKey<Enchantment> key : keys) {
            stored.set(enchantment(helper, key), 1);
        }
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        book.set(DataComponents.STORED_ENCHANTMENTS, stored.toImmutable());
        return book;
    }

    /**
     * B E B / E I E / B E B, with {@code box} in every B.
     */
    private static CraftingInput recipeGrid(GameTestHelper helper, ItemStack box) {
        ItemStack bullet = new ItemStack(ItemRegistry.ENCHANTED_BULLET_MODIFIER.get());
        ItemStack infinity = book(helper, Enchantments.INFINITY);
        List<ItemStack> grid = List.of(
                box.copy(), bullet.copy(), box.copy(),
                bullet.copy(), infinity, bullet.copy(),
                box.copy(), bullet.copy(), box.copy());
        return CraftingInput.of(3, 3, grid);
    }
}
