package com.heavyartillery.gametest;

import com.heavyartillery.HAConfig;
import com.heavyartillery.HeavyArtillery;
import com.heavyartillery.registry.HAItems;
import com.heavyartillery.weapon.HeavyShotComponents;
import com.heavyartillery.weapon.HeavyWeapons;
import com.heavyartillery.weapon.ShotModelHolder;
import io.redspace.irons_artifice.data.ReloadResult;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.GunplayManager;
import io.redspace.irons_artifice.item.MagazineContents;
import io.redspace.irons_artifice.menu.GunContainer;
import io.redspace.irons_artifice.modifier.on_hit_handlers.BlackpowderChargeOnHit;
import io.redspace.irons_artifice.registry.EntityRegistry;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Gunsteel Congreve: rockets that burst on impact, cost several bullets each, show a model in flight, and push the
 * shooter back depending on footing.
 */
final class CongreveTests {
    private static final double BLOWBACK = 0.9;

    private CongreveTests() {
    }

    static void rocketBurstsOnImpact(GameTestHelper helper) {
        BlockPos wall = new BlockPos(3, 2, 3);
        helper.setBlock(wall, Blocks.STONE);
        Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(3, 2, 5));
        Vec3 face = helper.absoluteVec(new Vec3(3.5, 2.5, 3));
        fire(helper, congreveShot(), face.add(0, 0, -3), face);
        helper.succeedWhen(() -> {
            helper.assertTrue(pig.getHealth() < pig.getMaxHealth(), "a pig behind the wall is caught in the burst");
            helper.assertBlockPresent(Blocks.STONE, wall);
        });
    }

    static void blackpowderChargeWidensBurst(GameTestHelper helper) {
        helper.assertTrue(Math.abs(radius(congreveShot()) - 4) < 1e-6, "the rocket bursts 4 blocks wide, was " + radius(congreveShot()));
        ItemStack gun = gunWith(0, ItemRegistry.BLACKPOWDER_CHARGE.get());
        double widened = radius(GunplayManager.compose(null, HeavyWeapons.GUNSTEEL_CONGREVE, gun));
        helper.assertTrue(Math.abs(widened - 5) < 1e-6, "a Blackpowder Charge adds a block, was " + widened);
        helper.succeed();
    }

    static void rocketCostsEightBullets(GameTestHelper helper) {
        Player player = survivalPlayer(helper, gunWith(0));
        player.getInventory().add(new ItemStack(ItemRegistry.BULLET.get(), 20));
        ItemStack gun = player.getMainHandItem();
        helper.assertValueEqual(GunplayManager.attemptFinishReload(player, gun, 0), ReloadResult.FINISHED_RELOAD, "reload result");
        helper.assertValueEqual(GunItem.getMagazine(gun).count(), 1, "one rocket loaded");
        helper.assertValueEqual(GunplayManager.countBullets(player), 12, "eight bullets packed into it");
        helper.succeed();
    }

    static void tooFewBulletsForARocket(GameTestHelper helper) {
        Player player = survivalPlayer(helper, gunWith(0));
        player.getInventory().add(new ItemStack(ItemRegistry.BULLET.get(), HAConfig.get(HAConfig.CONGREVE_BULLETS_PER_ROCKET) - 1));
        ItemStack gun = player.getMainHandItem();
        helper.assertValueEqual(GunplayManager.attemptStartReload(player, gun), ReloadResult.NO_AMMO, "seven bullets don't make a rocket");
        helper.assertValueEqual(GunplayManager.countBullets(player), HAConfig.get(HAConfig.CONGREVE_BULLETS_PER_ROCKET) - 1, "and none are used");
        helper.succeed();
    }

    static void enchantedMagazineMakesRocketsFree(GameTestHelper helper) {
        Player player = survivalPlayer(helper, gunWith(0, HAItems.ENCHANTED_MAGAZINE.get()));
        ItemStack gun = player.getMainHandItem();
        helper.assertValueEqual(GunplayManager.attemptStartReload(player, gun), ReloadResult.STARTING_RELOAD, "a reload starts with no bullets");
        helper.assertValueEqual(GunplayManager.attemptFinishReload(player, gun, 0), ReloadResult.FINISHED_RELOAD, "and finishes");
        helper.assertValueEqual(GunItem.getMagazine(gun).count(), 1, "rocket loaded for free");
        helper.succeed();
    }

    static void blowbackDependsOnFooting(GameTestHelper helper) {
        Player player = survivalPlayer(helper, gunWith(1));
        player.setOnGround(true);
        assertNear(helper, blowback(player), BLOWBACK, "standing");
        player.setOnGround(false);
        assertNear(helper, blowback(player), BLOWBACK * 1.75, "in the air");
        player.setOnGround(true);
        player.setShiftKeyDown(true);
        player.setPose(Pose.CROUCHING);
        assertNear(helper, blowback(player), BLOWBACK * 0.5, "braced");
        player.setItemSlot(EquipmentSlot.MAINHAND, gunWith(1, HAItems.PORTABLE_TRIPOD.get()));
        assertNear(helper, blowback(player), 0, "mounted on a tripod");
        helper.succeed();
    }

    static void rocketHasAModelInFlight(GameTestHelper helper) {
        Bullet rocket = fire(helper, congreveShot(), helper.absoluteVec(new Vec3(1, 4, 1)), helper.absoluteVec(new Vec3(1, 4, 4)));
        helper.assertValueEqual(((ShotModelHolder) rocket).artifice_heavy_artillery$shotModel(), HeavyArtillery.id("congreve_rocket"), "rocket model");
        ShotProfile musket = GunplayManager.compose(null, io.redspace.irons_artifice.gun.Guns.MUSKET, new ItemStack(ItemRegistry.MUSKET.get()));
        Bullet ball = fire(helper, musket, helper.absoluteVec(new Vec3(2, 4, 1)), helper.absoluteVec(new Vec3(2, 4, 4)));
        helper.assertTrue(((ShotModelHolder) ball).artifice_heavy_artillery$shotModel() == null, "a musket ball stays invisible");
        helper.succeed();
    }

    /**
     * Drop per block flown goes as gravity / speed^2: the rocket is slower than a bullet, so it needs far less gravity
     * a tick to fall less over the same distance.
     */
    static void rocketDropsLessThanABullet(GameTestHelper helper) {
        ShotProfile rocket = congreveShot();
        for (var gun : List.of(io.redspace.irons_artifice.gun.Guns.MUSKET, io.redspace.irons_artifice.gun.Guns.CLOCKWORK_RIFLE)) {
            ShotProfile bullet = GunplayManager.compose(null, gun, ItemStack.EMPTY);
            helper.assertTrue(dropPerBlock(rocket) < dropPerBlock(bullet),
                    "rocket " + dropPerBlock(rocket) + " vs bullet " + dropPerBlock(bullet));
        }
        helper.succeed();
    }

    private static double dropPerBlock(ShotProfile shot) {
        double speed = shot.value(ShotComponents.BULLET_SPEED);
        return shot.value(ShotComponents.GRAVITY) / (speed * speed);
    }

    @SuppressWarnings("deprecation")
    static void tooltipShowsCostOnly(GameTestHelper helper) {
        ItemStack stack = new ItemStack(HAItems.GUNSTEEL_CONGREVE.get());
        List<Component> lines = new ArrayList<>();
        stack.getItem().appendHoverText(stack, Item.TooltipContext.of(helper.getLevel()), lines, TooltipFlag.NORMAL);
        String text = String.join(" | ", lines.stream().map(Component::getString).toList());
        helper.assertTrue(text.contains("Shoots rockets packed from 8 Bullets"), "the tooltip gives the bullets per rocket: " + text);
        helper.assertFalse(text.contains("Heavy Weapon") || text.contains("block radius") || text.contains("harder in the air"),
                "and nothing else about how it works: " + text);
        helper.succeed();
    }

    static void recipeCraftsCongreve(GameTestHelper helper) {
        ItemStack iron = new ItemStack(Items.IRON_BLOCK);
        ItemStack heavy = new ItemStack(HAItems.HEAVY_COMPONENTS.get());
        CraftingInput grid = CraftingInput.of(3, 3, List.of(
                iron.copy(), iron.copy(), new ItemStack(Items.FIREWORK_ROCKET),
                heavy.copy(), new ItemStack(Items.NETHERITE_INGOT), heavy.copy(),
                ItemStack.EMPTY, new ItemStack(Items.OAK_LOG), ItemStack.EMPTY));
        var match = helper.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, helper.getLevel());
        helper.assertTrue(match.isPresent() && match.get().id().equals(HeavyArtillery.id("gunsteel_congreve")),
                "iron blocks, a firework rocket, heavy components, netherite and a log craft the Congreve");
        helper.succeed();
    }

    /* **********************************
     * Fixtures
     ********************************** */

    private static ShotProfile congreveShot() {
        return GunplayManager.compose(null, HeavyWeapons.GUNSTEEL_CONGREVE, new ItemStack(HAItems.GUNSTEEL_CONGREVE.get()));
    }

    private static double radius(ShotProfile profile) {
        return profile.peek(ShotComponents.ON_HIT).all().stream()
                .filter(BlackpowderChargeOnHit.class::isInstance).map(BlackpowderChargeOnHit.class::cast)
                .findFirst().map(BlackpowderChargeOnHit::radius).orElse(0f);
    }

    private static double blowback(Player player) {
        ItemStack gun = player.getMainHandItem();
        return GunplayManager.compose(player, ((GunItem) gun.getItem()).getGun(), gun).value(ShotComponents.CHARACTER_BLOWBACK);
    }

    /** One shot, the way GunplayManager fires them, aimed exactly (absolute positions). */
    private static Bullet fire(GameTestHelper helper, ShotProfile shot, Vec3 from, Vec3 to) {
        ServerLevel level = helper.getLevel();
        Bullet bullet = new Bullet(EntityRegistry.BULLET.get(), level);
        bullet.applyProfile(shot.copy());
        bullet.setPos(from);
        Vec3 direction = to.subtract(from);
        bullet.shoot(direction.x, direction.y, direction.z, (float) shot.value(ShotComponents.BULLET_SPEED), 0);
        level.addFreshEntity(bullet);
        return bullet;
    }

    /** See EnchantedMagazineTests: a mock player that is never logged in. */
    private static Player survivalPlayer(GameTestHelper helper, ItemStack gun) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemSlot(EquipmentSlot.MAINHAND, gun);
        return player;
    }

    private static ItemStack gunWith(int rounds, Item... modifiers) {
        ItemStack stack = new ItemStack(HAItems.GUNSTEEL_CONGREVE.get());
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

    private static void assertNear(GameTestHelper helper, double actual, double expected, String what) {
        helper.assertTrue(Math.abs(actual - expected) < 1e-6, what + ": expected " + expected + ", was " + actual);
    }
}
