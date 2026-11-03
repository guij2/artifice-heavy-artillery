package com.heavyartillery.gametest;

import com.heavyartillery.HAConfig;
import com.heavyartillery.HeavyArtillery;
import com.heavyartillery.modifier.DrumMagazineModifier;
import com.heavyartillery.modifier.PowderRamModifier;
import com.heavyartillery.modifier.RifledBarrelModifier;
import com.heavyartillery.registry.HAAttachments;
import com.heavyartillery.registry.HAItems;
import com.heavyartillery.weapon.HeavyShotComponents;
import com.heavyartillery.weapon.trait.Rifling;
import io.redspace.irons_artifice.data.ReloadResult;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.GunplayManager;
import io.redspace.irons_artifice.item.MagazineContents;
import io.redspace.irons_artifice.menu.GunContainer;
import io.redspace.irons_artifice.modifier.GunModifier;
import io.redspace.irons_artifice.registry.EntityRegistry;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Drum Magazine, Rifled Barrel and Powder Ram: on our guns and the parent's.
 */
final class ModifierTests {
    private ModifierTests() {
    }

    /* **********************************
     * Drum Magazine
     ********************************** */

    static void drumEnlargesMagazine(GameTestHelper helper) {
        helper.assertValueEqual(reloadFully(helper, gunWith(HAItems.GUNSTEEL_GATLING.get(), HAItems.DRUM_MAGAZINE.get())), 90, "a drummed Gatling holds 60 + 50%");
        helper.assertValueEqual(reloadFully(helper, gunWith(ItemRegistry.MUSKET.get(), HAItems.DRUM_MAGAZINE.get())), 2, "a drummed musket holds 2 (1.5 rounded up)");
        helper.assertValueEqual(reloadFully(helper, gunWith(ItemRegistry.SIX_SHOOTER.get(), HAItems.DRUM_MAGAZINE.get())), 9, "a drummed Six-Shooter holds 9");
        helper.assertValueEqual(reloadFully(helper, gunWith(ItemRegistry.SIX_SHOOTER.get())), 6, "without a drum, 6");
        helper.succeed();
    }

    static void drumsSlowReloadAndStack(GameTestHelper helper) {
        ItemStack one = gunWith(HAItems.GUNSTEEL_GATLING.get(), HAItems.DRUM_MAGAZINE.get());
        ItemStack two = gunWith(HAItems.GUNSTEEL_GATLING.get(), HAItems.DRUM_MAGAZINE.get(), HAItems.DRUM_MAGAZINE.get());
        double slower = 1 - HAConfig.get(HAConfig.DRUM_RELOAD_PENALTY);
        assertNear(helper, compose(null, one).value(ShotComponents.RELOAD_SPEED_MULTIPLIER), slower, "reload speed");
        assertNear(helper, compose(null, two).value(ShotComponents.RELOAD_SPEED_MULTIPLIER), slower * slower, "reload speed with two drums");
        helper.assertValueEqual(reloadFully(helper, two), 135, "two drums: 60 x 1.5 x 1.5");
        helper.succeed();
    }

    /* **********************************
     * Rifled Barrel
     ********************************** */

    /** Two golems 5 blocks off, one shot each from the same musket, one of them rifled: the rifled one hurts more. */
    static void rifledShotHitsHarderAfterFlying(GameTestHelper helper) {
        IronGolem plainTarget = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new BlockPos(1, 1, 5));
        IronGolem rifledTarget = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new BlockPos(4, 1, 5));
        float full = plainTarget.getMaxHealth();
        shoot(helper, compose(null, gunWith(ItemRegistry.MUSKET.get())), plainTarget);
        shoot(helper, compose(null, gunWith(ItemRegistry.MUSKET.get(), HAItems.RIFLED_BARREL.get())), rifledTarget);
        helper.succeedWhen(() -> {
            float plain = full - plainTarget.getHealth();
            float rifled = full - rifledTarget.getHealth();
            helper.assertTrue(plain > 0 && rifled > 0, "both golems are hit");
            // about 4.5 blocks flown: +9%
            helper.assertTrue(rifled > plain * 1.04 && rifled < plain * 1.15, "the rifled shot hits harder: " + rifled + " vs " + plain);
        });
    }

    static void riflingCapsAndStacks(GameTestHelper helper) {
        ShotProfile one = compose(null, gunWith(ItemRegistry.MUSKET.get(), HAItems.RIFLED_BARREL.get()));
        ShotProfile two = compose(null, gunWith(ItemRegistry.MUSKET.get(), HAItems.RIFLED_BARREL.get(), HAItems.RIFLED_BARREL.get()));
        assertNear(helper, two.value(HeavyShotComponents.RIFLING), 2 * HAConfig.get(HAConfig.RIFLED_DAMAGE_PER_BLOCK), "rifling with two barrels");
        double slower = one.value(ShotComponents.RELOAD_SPEED_MULTIPLIER);
        assertNear(helper, two.value(ShotComponents.RELOAD_SPEED_MULTIPLIER), slower * slower, "reload with two barrels");
        Bullet bullet = new Bullet(EntityRegistry.BULLET.get(), helper.getLevel());
        bullet.applyProfile(one.copy());
        assertNear(helper, Rifling.apply(bullet, 10, Vec3.ZERO, new Vec3(0, 0, 10)), 12, "10 blocks: +20%");
        assertNear(helper, Rifling.apply(bullet, 10, Vec3.ZERO, new Vec3(0, 0, 100)), 16, "capped at +60% (30 blocks)");
        bullet.applyProfile(two.copy());
        assertNear(helper, Rifling.apply(bullet, 10, Vec3.ZERO, new Vec3(0, 0, 10)), 14, "two barrels, 10 blocks: +40%");
        assertNear(helper, Rifling.apply(bullet, 10, Vec3.ZERO, new Vec3(0, 0, 100)), 22, "two barrels cap at +120%");
        helper.succeed();
    }

    /* **********************************
     * Powder Ram
     ********************************** */

    static void powderRamChargesWithTime(GameTestHelper helper) {
        Player player = holding(helper, gunWith(ItemRegistry.MUSKET.get(), HAItems.POWDER_RAM.get()));
        double base = compose(null, gunWith(ItemRegistry.MUSKET.get())).value(ShotComponents.DAMAGE);
        assertNear(helper, compose(player, player.getMainHandItem()).value(ShotComponents.DAMAGE), base * 1.5, "not fired yet: fully charged");
        long now = helper.getLevel().getGameTime();
        player.setData(HAAttachments.LAST_SHOT, now);
        assertNear(helper, compose(player, player.getMainHandItem()).value(ShotComponents.DAMAGE), base, "just fired: no bonus");
        int chargeTicks = HAConfig.get(HAConfig.POWDER_RAM_CHARGE_TICKS);
        player.setData(HAAttachments.LAST_SHOT, now - chargeTicks / 2);
        assertNear(helper, compose(player, player.getMainHandItem()).value(ShotComponents.DAMAGE), base * 1.25, "half charged");
        player.setData(HAAttachments.LAST_SHOT, now - 200);
        assertNear(helper, compose(player, player.getMainHandItem()).value(ShotComponents.DAMAGE), base * 1.5, "long after: fully charged");
        Player twoRams = holding(helper, gunWith(ItemRegistry.MUSKET.get(), HAItems.POWDER_RAM.get(), HAItems.POWDER_RAM.get()));
        assertNear(helper, compose(twoRams, twoRams.getMainHandItem()).value(ShotComponents.DAMAGE), base * 2, "two rams stack");
        twoRams.setData(HAAttachments.LAST_SHOT, now - chargeTicks);
        assertNear(helper, compose(twoRams, twoRams.getMainHandItem()).value(ShotComponents.DAMAGE), base * 1.5,
                "two rams take twice as long to charge: half way after one ram's charge time");
        twoRams.setData(HAAttachments.LAST_SHOT, now - 2L * chargeTicks);
        assertNear(helper, compose(twoRams, twoRams.getMainHandItem()).value(ShotComponents.DAMAGE), base * 2, "and fully charged after two");
        helper.succeed();
    }

    /* **********************************
     * Descriptions and recipes
     ********************************** */

    static void descriptions(GameTestHelper helper) {
        assertDescription(helper, new DrumMagazineModifier(), "150% Magazine Capacity | 80% Reload Speed");
        assertDescription(helper, new RifledBarrelModifier(), "+2% Damage per block the shot travels, up to +60% | 85% Reload Speed");
        assertDescription(helper, new PowderRamModifier(), "Up to +50% Damage, building over 1.5s since your last shot");
        helper.assertTrue(new ItemStack(HAItems.POWDER_RAM.get()).getHoverName().getString().equals("Powder Ram Modifier"), "item name");
        helper.succeed();
    }

    static void recipes(GameTestHelper helper) {
        ItemStack heavy = new ItemStack(HAItems.HEAVY_COMPONENTS.get());
        ItemStack iron = new ItemStack(Items.IRON_INGOT);
        ItemStack copper = new ItemStack(Items.COPPER_INGOT);
        ItemStack ironBlock = new ItemStack(Items.IRON_BLOCK);
        ItemStack powder = new ItemStack(ItemRegistry.BLACKPOWDER.get());
        ItemStack stick = new ItemStack(Items.STICK);
        ItemStack none = ItemStack.EMPTY;
        assertRecipe(helper, "drum_magazine_modifier", CraftingInput.of(3, 3, List.of(iron, copper, iron, copper, heavy, copper, iron, copper, iron)));
        assertRecipe(helper, "rifled_barrel_modifier", CraftingInput.of(1, 3, List.of(ironBlock, heavy, ironBlock)));
        assertRecipe(helper, "powder_ram_modifier", CraftingInput.of(3, 3, List.of(powder, heavy, powder, powder, stick, powder, none, stick, none)));
        helper.succeed();
    }

    /* **********************************
     * Fixtures
     ********************************** */

    private static int reloadFully(GameTestHelper helper, ItemStack gun) {
        Player player = holding(helper, gun);
        player.getInventory().add(new ItemStack(ItemRegistry.BULLET.get(), 200));
        GunItem.setMagazine(player.getMainHandItem(), new MagazineContents(0));
        helper.assertValueEqual(GunplayManager.attemptFinishReload(player, player.getMainHandItem(), 0), ReloadResult.FINISHED_RELOAD, "reload");
        return GunItem.getMagazine(player.getMainHandItem()).count();
    }

    /** One shot from 5 blocks in front of the target, aimed at its middle, the way GunplayManager fires them. */
    private static void shoot(GameTestHelper helper, ShotProfile shot, IronGolem target) {
        ServerLevel level = helper.getLevel();
        Vec3 aim = target.getBoundingBox().getCenter();
        Vec3 from = aim.add(0, 0, -5);
        Bullet bullet = new Bullet(EntityRegistry.BULLET.get(), level);
        bullet.applyProfile(shot.copy());
        bullet.setPos(from);
        bullet.shoot(0, 0, 1, (float) shot.value(ShotComponents.BULLET_SPEED), 0);
        level.addFreshEntity(bullet);
    }

    private static void assertDescription(GameTestHelper helper, GunModifier modifier, String expected) {
        List<Component> lines = new ArrayList<>();
        modifier.getDescriptionText(lines::add);
        String text = String.join(" | ", lines.stream().map(Component::getString).toList());
        helper.assertTrue(text.equals(expected), "description: expected \"" + expected + "\", was \"" + text + "\"");
    }

    private static void assertRecipe(GameTestHelper helper, String id, CraftingInput grid) {
        var match = helper.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, helper.getLevel());
        helper.assertTrue(match.isPresent() && match.get().id().equals(ResourceKey.create(Registries.RECIPE, HeavyArtillery.id(id))),
                "recipe for " + id + ", got " + match.map(m -> m.id().identifier().toString()).orElse("nothing"));
    }

    private static Player holding(GameTestHelper helper, ItemStack gun) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemSlot(EquipmentSlot.MAINHAND, gun);
        player.setOnGround(true);
        return player;
    }

    private static ShotProfile compose(Player player, ItemStack gun) {
        return GunplayManager.compose(player, ((GunItem) gun.getItem()).getGun(), gun);
    }

    private static ItemStack gunWith(Item gun, Item... modifiers) {
        ItemStack stack = new ItemStack(gun);
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

    private static void assertNear(GameTestHelper helper, double actual, double expected, String what) {
        helper.assertTrue(Math.abs(actual - expected) < 1e-4, what + ": expected " + expected + ", was " + actual);
    }
}
