package com.heavyartillery.gametest;

import com.heavyartillery.registry.HAItems;
import com.heavyartillery.weapon.HeavyWeapons;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.gun.GunProfile;
import io.redspace.irons_artifice.gun.Guns;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.item.GunplayManager;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * The balance pass (README, Balance): heavy weapons out-slot every parent gun, the Crucible hits at full strength up
 * close, and the Congreve's harder rocket still digs the craters it used to.
 */
final class BalanceTests {
    private static final List<GunProfile> PARENT_GUNS = List.of(Guns.FLINTLOCK_PISTOL, Guns.MUSKET, Guns.BLUNDERBUSS,
            Guns.BLACKPOWDER_REVOLVER, Guns.SIX_SHOOTER, Guns.ARQUEBUS, Guns.CLOCKWORK_RIFLE);
    private static final List<GunProfile> HEAVY_GUNS = List.of(HeavyWeapons.GUNSTEEL_GATLING, HeavyWeapons.GUNSTEEL_CRUCIBLE,
            HeavyWeapons.GUNSTEEL_CONGREVE);

    private BalanceTests() {
    }

    static void heavyGunsOutSlotParentGuns(GameTestHelper helper) {
        int parentMost = PARENT_GUNS.stream().mapToInt(GunProfile::modifierSlots).max().orElseThrow();
        for (GunProfile heavy : HEAVY_GUNS) {
            helper.assertValueEqual(heavy.modifierSlots(), HeavyWeapons.MODIFIER_SLOTS, "heavy weapon slots");
            helper.assertTrue(heavy.modifierSlots() > parentMost, "more slots than any parent gun (" + parentMost + ")");
        }
        helper.succeed();
    }

    static void crucibleFullStrengthUpClose(GameTestHelper helper) {
        ShotProfile shot = GunplayManager.compose(null, HeavyWeapons.GUNSTEEL_CRUCIBLE, new ItemStack(HAItems.GUNSTEEL_CRUCIBLE.get()));
        // Bullet.resolveDamage scales by speed up to 2 blocks/tick: a glob leaving at 2 hits in full on its first tick
        helper.assertTrue(shot.value(ShotComponents.BULLET_SPEED) >= 2, "globs leave the shroud fast enough for full damage");
        helper.succeed();
    }

    static void congreveCratersUnchanged(GameTestHelper helper) {
        ShotProfile shot = GunplayManager.compose(null, HeavyWeapons.GUNSTEEL_CONGREVE, new ItemStack(HAItems.GUNSTEEL_CONGREVE.get()));
        double blockDamage = shot.value(ShotComponents.DAMAGE) * shot.value(ShotComponents.BLOCK_DAMAGE_MULTIPLIER);
        helper.assertTrue(shot.value(ShotComponents.DAMAGE) >= 80, "a rocket hits for 80, was " + shot.value(ShotComponents.DAMAGE));
        helper.assertTrue(blockDamage > 25 && blockDamage < 35, "but breaks blocks like the 28-damage rocket did, was " + blockDamage);
        helper.succeed();
    }
}
