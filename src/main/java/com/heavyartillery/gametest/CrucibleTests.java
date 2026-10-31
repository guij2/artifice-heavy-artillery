package com.heavyartillery.gametest;

import com.heavyartillery.registry.HAItems;
import com.heavyartillery.weapon.HeavyShotComponents;
import com.heavyartillery.weapon.HeavyWeapons;
import com.heavyartillery.weapon.trait.MoltenShot;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.item.GunplayManager;
import io.redspace.irons_artifice.registry.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Gunsteel Crucible: a short-ranged molten spray that sets what it hits alight.
 */
final class CrucibleTests {
    private CrucibleTests() {
    }

    static void sprayRangeIsShort(GameTestHelper helper) {
        double range = MoltenShot.range(crucibleShot());
        helper.assertTrue(range > 5 && range < 9, "the spray stops after 5-9 blocks, was " + range);
        helper.succeed();
    }

    static void sprayBurnsTarget(GameTestHelper helper) {
        Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(3, 1, 5));
        Vec3 target = pig.getBoundingBox().getCenter();
        fire(helper, crucibleShot(), target.add(0, 0, -3), target);
        helper.succeedWhen(() -> {
            helper.assertTrue(pig.isOnFire(), "the target is set alight");
            helper.assertTrue(pig.getHealth() < pig.getMaxHealth(), "and hurt");
        });
    }

    static void sprayIgnitesBlocks(GameTestHelper helper) {
        BlockPos floor = new BlockPos(1, 1, 1);
        helper.setBlock(floor, Blocks.STONE);
        ShotProfile shot = crucibleShot();
        shot.modifyValue(HeavyShotComponents.IGNITE_BLOCK_CHANCE, new ValueModifier(1, ValueModifier.Operation.ADD, ValueModifier.Type.BENEFICIAL));
        Vec3 top = helper.absoluteVec(Vec3.atCenterOf(floor)).add(0, 0.5, 0);
        fire(helper, shot, top.add(0, 2, 0), top);
        helper.succeedWhen(() -> helper.assertBlockPresent(Blocks.FIRE, floor.above()));
    }

    static void sprayIsQuenchedUnderwater(GameTestHelper helper) {
        for (int y = 1; y <= 3; y++) {
            for (int z = 1; z <= 4; z++) {
                helper.setBlock(new BlockPos(3, y, z), Blocks.WATER);
            }
        }
        Vec3 from = helper.absoluteVec(Vec3.atCenterOf(new BlockPos(3, 2, 1)));
        Bullet bullet = fire(helper, crucibleShot(), from, from.add(0, 0, 3));
        helper.runAfterDelay(3, () -> {
            helper.assertTrue(bullet.isRemoved(), "a molten shot fizzles out at once underwater");
            helper.succeed();
        });
    }

    @SuppressWarnings("deprecation")
    static void tooltipShowsSprayRange(GameTestHelper helper) {
        ItemStack stack = new ItemStack(HAItems.GUNSTEEL_CRUCIBLE.get());
        List<Component> lines = new ArrayList<>();
        stack.getItem().appendHoverText(stack, Item.TooltipContext.of(helper.getLevel()), TooltipDisplay.DEFAULT, lines::add, TooltipFlag.NORMAL);
        String text = String.join(" | ", lines.stream().map(Component::getString).toList());
        helper.assertTrue(lines.stream().anyMatch(line -> line.getString().contains("7 blocks")), "the tooltip gives the spray's reach (7 blocks): " + text);
        helper.succeed();
    }

    private static ShotProfile crucibleShot() {
        return GunplayManager.compose(null, HeavyWeapons.GUNSTEEL_CRUCIBLE, new ItemStack(HAItems.GUNSTEEL_CRUCIBLE.get()));
    }

    /** One glob, the way GunplayManager fires them, aimed exactly (absolute positions). */
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
}
