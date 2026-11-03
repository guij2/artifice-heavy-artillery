package com.heavyartillery.gametest;

import com.heavyartillery.HAConfig;
import com.heavyartillery.modifier.PowderRamModifier;
import com.heavyartillery.modifier.ReflexSightModifier;
import com.heavyartillery.modifier.RifledBarrelModifier;
import com.heavyartillery.registry.HAAttachments;
import com.heavyartillery.registry.HAItems;
import com.heavyartillery.weapon.HeavyShotComponents;
import com.heavyartillery.weapon.HeavyWeapons;
import com.heavyartillery.weapon.trait.MagazineSize;
import com.heavyartillery.weapon.trait.MovementPenalty;
import com.heavyartillery.weapon.trait.PowderRam;
import com.heavyartillery.weapon.trait.Rifling;
import io.redspace.irons_artifice.data.ReloadResult;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.gun.Guns;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.GunplayManager;
import io.redspace.irons_artifice.item.MagazineContents;
import io.redspace.irons_artifice.item.ReloadState;
import io.redspace.irons_artifice.menu.GunContainer;
import io.redspace.irons_artifice.menu.GunModifierMenu;
import io.redspace.irons_artifice.modifier.GunModifier;
import io.redspace.irons_artifice.modifier.on_hit_handlers.BlackpowderChargeOnHit;
import io.redspace.irons_artifice.registry.EntityRegistry;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;

/**
 * The server config ({@link HAConfig}) reaches the guns: each test sets some values, checks guns made before the
 * change follow them, and puts the old values back before returning. Test bodies run on the server thread, so no other
 * test sees the changed values.
 */
final class ConfigTests {
    private static final List<Runnable> RESTORE = new ArrayList<>();

    private ConfigTests() {
    }

    static void gunStatsFollowConfig(GameTestHelper helper) {
        ItemStack gun = new ItemStack(HAItems.GUNSTEEL_GATLING.get());
        try {
            set(HAConfig.GATLING.damage, 12.0);
            set(HAConfig.GATLING.spread, 1.0);
            set(HAConfig.GATLING.fireDelay, 4);
            set(HAConfig.GATLING_SPIN_UP_SHOTS, 5);
            ShotProfile shot = GunplayManager.compose(null, HeavyWeapons.GUNSTEEL_GATLING, gun);
            assertNear(helper, shot.value(ShotComponents.DAMAGE), 12, "damage");
            assertNear(helper, shot.value(ShotComponents.SPREAD), 1, "spread");
            helper.assertValueEqual(shot.fireDelayTicks(), 4, "fire delay");
            assertNear(helper, shot.value(HeavyShotComponents.SPIN_UP_SHOTS), 5, "spin-up shots");
        } finally {
            restore();
        }
        helper.succeed();
    }

    static void magazineAndReloadFollowConfig(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(HAItems.GUNSTEEL_GATLING.get()));
        player.getInventory().add(new ItemStack(ItemRegistry.BULLET.get(), 200));
        ItemStack gun = player.getMainHandItem();
        GunItem.setMagazine(gun, new MagazineContents(0));
        try {
            set(HAConfig.GATLING.magazineCapacity, 100);
            set(HAConfig.GATLING.reloadTime, 40);
            helper.assertValueEqual(GunplayManager.attemptStartReload(player, gun), ReloadResult.STARTING_RELOAD, "reload starts");
            helper.assertValueEqual(ReloadState.get(gun).durationTicks(), 40, "reload time");
            helper.assertValueEqual(GunplayManager.attemptFinishReload(player, gun, 0), ReloadResult.FINISHED_RELOAD, "reload finishes");
            helper.assertValueEqual(GunItem.getMagazine(gun).count(), 100, "magazine capacity");
        } finally {
            restore();
        }
        helper.succeed();
    }

    static void movementPenaltyFollowsConfig(GameTestHelper helper) {
        ItemStack gun = new ItemStack(HAItems.GUNSTEEL_CRUCIBLE.get());
        try {
            set(HAConfig.CRUCIBLE.movementPenalty, 0.5);
            assertNear(helper, penalty(gun), 0.5, "configured penalty");
            set(HAConfig.CRUCIBLE.movementPenalty, 0.0);
            assertNear(helper, penalty(gun), 0, "no penalty");
        } finally {
            restore();
        }
        assertNear(helper, penalty(gun), 0.15, "default penalty");
        helper.succeed();
    }

    static void weaponNumbersFollowConfig(GameTestHelper helper) {
        try {
            set(HAConfig.CRUCIBLE_GLOBS, 5);
            ShotProfile spray = GunplayManager.compose(null, HeavyWeapons.GUNSTEEL_CRUCIBLE, new ItemStack(HAItems.GUNSTEEL_CRUCIBLE.get()));
            assertNear(helper, spray.value(ShotComponents.PROJECTILE_COUNT), 5, "globs per shot");

            set(HAConfig.CONGREVE.damage, 160.0);
            set(HAConfig.CONGREVE_BULLETS_PER_ROCKET, 2);
            set(HAConfig.CONGREVE_BLAST_RADIUS, 6);
            ShotProfile rocket = GunplayManager.compose(null, HeavyWeapons.GUNSTEEL_CONGREVE, new ItemStack(HAItems.GUNSTEEL_CONGREVE.get()));
            assertNear(helper, rocket.value(HeavyShotComponents.RESERVE_AMMO_COST), 2, "bullets per rocket");
            double radius = rocket.peek(ShotComponents.ON_HIT).all().stream()
                    .filter(BlackpowderChargeOnHit.class::isInstance).map(BlackpowderChargeOnHit.class::cast)
                    .findFirst().map(BlackpowderChargeOnHit::radius).orElse(0f);
            assertNear(helper, radius, 6, "blast radius");
            assertNear(helper, rocket.value(ShotComponents.DAMAGE) * rocket.value(ShotComponents.BLOCK_DAMAGE_MULTIPLIER),
                    HeavyWeapons.CONGREVE_BLOCK_DAMAGE, "block damage whatever the damage");
        } finally {
            restore();
        }
        helper.succeed();
    }

    static void modifierNumbersFollowConfig(GameTestHelper helper) {
        ItemStack gun = gunWith(HAItems.GUNSTEEL_GATLING.get(), HAItems.DRUM_MAGAZINE.get(), HAItems.RIFLED_BARREL.get(),
                HAItems.POWDER_RAM.get(), HAItems.REFLEX_SIGHT.get(), HAItems.PORTABLE_TRIPOD.get());
        try {
            set(HAConfig.DRUM_CAPACITY_BONUS, 1.0);
            set(HAConfig.DRUM_RELOAD_PENALTY, 0.5);
            set(HAConfig.RIFLED_DAMAGE_PER_BLOCK, 0.05);
            set(HAConfig.RIFLED_MAX_DISTANCE, 10);
            set(HAConfig.RIFLED_RELOAD_PENALTY, 0.3);
            set(HAConfig.POWDER_RAM_DAMAGE_BONUS, 2.0);
            set(HAConfig.POWDER_RAM_CHARGE_TICKS, 60);
            set(HAConfig.SIGHT_SPREAD_REDUCTION, 0.6);
            set(HAConfig.SIGHT_RECOIL_REDUCTION, 0.1);
            set(HAConfig.TRIPOD_SPREAD_REDUCTION, 0.9);
            set(HAConfig.TRIPOD_RECOIL_REDUCTION, 0.2);
            ShotProfile shot = GunplayManager.compose(null, HeavyWeapons.GUNSTEEL_GATLING, gun);
            helper.assertValueEqual(MagazineSize.capacity(gun, 60), 120, "drum capacity");
            assertNear(helper, shot.value(ShotComponents.RELOAD_SPEED_MULTIPLIER), 0.5 * 0.7, "drum and barrel reload speed");
            assertNear(helper, shot.value(HeavyShotComponents.RIFLING), 0.05, "rifling per block");
            assertNear(helper, shot.value(HeavyShotComponents.POWDER_RAM), 2, "powder ram bonus");
            assertNear(helper, shot.value(HeavyShotComponents.AIMED_SPREAD_REDUCTION), 0.6, "aimed spread reduction");
            assertNear(helper, shot.value(HeavyShotComponents.AIMED_RECOIL_REDUCTION), 0.1, "aimed recoil reduction");
            assertNear(helper, shot.value(HeavyShotComponents.MOUNTED_SPREAD_REDUCTION), 0.9, "mounted spread reduction");
            assertNear(helper, shot.value(HeavyShotComponents.MOUNTED_RECOIL_REDUCTION), 0.2, "mounted recoil reduction");

            Bullet bullet = new Bullet(EntityRegistry.BULLET.get(), helper.getLevel());
            bullet.applyProfile(shot.copy());
            assertNear(helper, Rifling.apply(bullet, 10, Vec3.ZERO, new Vec3(0, 0, 20)), 15, "rifling counts 10 blocks");
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setData(HAAttachments.LAST_SHOT, helper.getLevel().getGameTime() - 30);
            assertNear(helper, shot.value(HeavyShotComponents.POWDER_RAM_CHARGE), 60, "powder ram charge time");
            assertNear(helper, PowderRam.charge(player, shot.value(HeavyShotComponents.POWDER_RAM_CHARGE)), 0.5, "powder ram half charged after 30 of 60 ticks");

            assertDescription(helper, new RifledBarrelModifier(), "+5% Damage per block the shot travels, up to +50% | 70% Reload Speed");
            assertDescription(helper, new PowderRamModifier(), "Up to +200% Damage, building over 3s since your last shot");
            assertDescription(helper, new ReflexSightModifier(), "Enables Aiming Down Sights | -60% Spread and -10% Recoil while aiming");
        } finally {
            restore();
        }
        helper.succeed();
    }

    static void modifierSlotsFollowConfig(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemSlot(EquipmentSlot.MAINHAND, gunWith(HAItems.GUNSTEEL_GATLING.get(), HAItems.DRUM_MAGAZINE.get(),
                HAItems.DRUM_MAGAZINE.get(), HAItems.DRUM_MAGAZINE.get(), HAItems.RIFLED_BARREL.get()));
        ItemStack gun = player.getMainHandItem();
        try {
            set(HAConfig.GATLING.modifierSlots, 2);
            helper.assertValueEqual(HeavyWeapons.GUNSTEEL_GATLING.modifierSlots(), 2, "configured slots");
            helper.assertValueEqual(Guns.MUSKET.modifierSlots(), 5, "the parent's guns keep theirs");
            double drum = 1 - HAConfig.get(HAConfig.DRUM_RELOAD_PENALTY);
            assertNear(helper, GunplayManager.compose(null, HeavyWeapons.GUNSTEEL_GATLING, gun).value(ShotComponents.RELOAD_SPEED_MULTIPLIER),
                    drum * drum, "only the modifiers in the remaining slots count");
            helper.assertValueEqual(installed(gun), 4, "the rest stay on the gun until its menu opens");

            new GunModifierMenu(0, player.getInventory());
            helper.assertValueEqual(player.getInventory().countItem(HAItems.DRUM_MAGAZINE.get()), 1, "the drum past the slots is given back");
            helper.assertValueEqual(player.getInventory().countItem(HAItems.RIFLED_BARREL.get()), 1, "and the barrel");
            helper.assertValueEqual(installed(gun), 2, "the gun keeps the modifiers in its slots");
        } finally {
            restore();
        }
        helper.assertValueEqual(HeavyWeapons.GUNSTEEL_GATLING.modifierSlots(), 8, "default slots");
        helper.succeed();
    }

    private static int installed(ItemStack gun) {
        ItemContainerContents contents = gun.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        int count = 0;
        for (int slot = 0; slot < contents.getSlots(); slot++) {
            if (!contents.getStackInSlot(slot).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private static ItemStack gunWith(Item gun, Item... modifiers) {
        GunContainer container = new GunContainer(new ItemStack(gun));
        for (int slot = 0; slot < modifiers.length; slot++) {
            container.setItem(slot, new ItemStack(modifiers[slot]));
        }
        container.setChanged();
        return container.getGunStack();
    }

    private static void assertDescription(GameTestHelper helper, GunModifier modifier, String expected) {
        List<Component> lines = new ArrayList<>();
        modifier.getDescriptionText(lines::add);
        String text = String.join(" | ", lines.stream().map(Component::getString).toList());
        helper.assertTrue(text.equals(expected), "description: expected \"" + expected + "\", was \"" + text + "\"");
    }

    private static double penalty(ItemStack gun) {
        return gun.getAttributeModifiers().modifiers().stream()
                .filter(entry -> entry.modifier().id().equals(MovementPenalty.ID))
                .mapToDouble(entry -> -entry.modifier().amount())
                .sum();
    }

    private static <T> void set(ModConfigSpec.ConfigValue<T> value, T setting) {
        T before = value.get();
        value.set(setting);
        RESTORE.add(() -> value.set(before));
    }

    private static void restore() {
        RESTORE.reversed().forEach(Runnable::run);
        RESTORE.clear();
    }

    private static void assertNear(GameTestHelper helper, double actual, double expected, String what) {
        helper.assertTrue(Math.abs(actual - expected) < 1e-4, what + ": expected " + expected + ", was " + actual);
    }
}
