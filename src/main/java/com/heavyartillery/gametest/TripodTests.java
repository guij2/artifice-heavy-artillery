package com.heavyartillery.gametest;

import com.heavyartillery.HAConfig;
import com.heavyartillery.HeavyArtillery;
import com.heavyartillery.modifier.PortableTripodModifier;
import com.heavyartillery.registry.HAItems;
import com.heavyartillery.weapon.HeavyShotComponents;
import com.heavyartillery.weapon.trait.TripodMount;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.item.AttachmentMap;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.GunplayManager;
import io.redspace.irons_artifice.menu.GunContainer;
import io.redspace.irons_artifice.registry.DataComponentRegistry;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;

import java.util.ArrayList;
import java.util.List;

/**
 * Portable Tripod: crouching on the ground mounts the gun (much steadier), on our guns and the parent's, and it fits
 * alongside other attachments.
 */
final class TripodTests {
    private TripodTests() {
    }

    static void mountedCutsSpreadAndRecoil(GameTestHelper helper) {
        // against the same gun crouched without a tripod, which has the parent mod's own crouch bonus
        Player plain = holding(helper, gunWith(ItemRegistry.CLOCKWORK_RIFLE.get()));
        mount(plain);
        Player tripod = holding(helper, gunWith(ItemRegistry.CLOCKWORK_RIFLE.get(), HAItems.PORTABLE_TRIPOD.get()));
        mount(tripod);
        helper.assertTrue(TripodMount.isMounted(tripod), "crouching on the ground mounts the tripod");
        ShotProfile crouched = compose(plain);
        ShotProfile mounted = compose(tripod);
        helper.assertTrue(crouched.value(ShotComponents.SPREAD) > 0, "the clockwork rifle has some spread to cut");
        assertNear(helper, mounted.value(ShotComponents.SPREAD),
                crouched.value(ShotComponents.SPREAD) * (1 - HAConfig.get(HAConfig.TRIPOD_SPREAD_REDUCTION)), "spread while mounted");
        assertNear(helper, mounted.value(ShotComponents.CAMERA_RECOIL_MULTIPLIER),
                crouched.value(ShotComponents.CAMERA_RECOIL_MULTIPLIER) * (1 - HAConfig.get(HAConfig.TRIPOD_RECOIL_REDUCTION)), "recoil while mounted");
        helper.succeed();
    }

    static void noEffectStanding(GameTestHelper helper) {
        Player plain = holding(helper, gunWith(ItemRegistry.CLOCKWORK_RIFLE.get()));
        Player tripod = holding(helper, gunWith(ItemRegistry.CLOCKWORK_RIFLE.get(), HAItems.PORTABLE_TRIPOD.get()));
        helper.assertFalse(TripodMount.isMounted(tripod), "standing is not mounted");
        assertNear(helper, compose(tripod).value(ShotComponents.SPREAD), compose(plain).value(ShotComponents.SPREAD), "spread standing");
        assertNear(helper, compose(tripod).value(ShotComponents.CAMERA_RECOIL_MULTIPLIER), compose(plain).value(ShotComponents.CAMERA_RECOIL_MULTIPLIER),
                "recoil standing");
        helper.succeed();
    }

    static void mountingNeedsSneakAndGround(GameTestHelper helper) {
        Player player = holding(helper, gunWith(HAItems.GUNSTEEL_GATLING.get(), HAItems.PORTABLE_TRIPOD.get()));
        mount(player);
        player.setOnGround(false);
        helper.assertFalse(TripodMount.isMounted(player), "not mounted in the air");
        player.setOnGround(true);
        player.setShiftKeyDown(false);
        helper.assertFalse(TripodMount.isMounted(player), "not mounted when only squeezed into a crouch by a low ceiling");
        player.setShiftKeyDown(true);
        helper.assertTrue(TripodMount.isMounted(player), "mounted when sneaking on the ground");
        player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        helper.assertFalse(TripodMount.isMounted(player), "not mounted with the gun put away");
        helper.succeed();
    }

    static void stacksWithBrace(GameTestHelper helper) {
        Player plain = holding(helper, gunWith(HAItems.GUNSTEEL_GATLING.get()));
        mount(plain);
        Player tripod = holding(helper, gunWith(HAItems.GUNSTEEL_GATLING.get(), HAItems.PORTABLE_TRIPOD.get()));
        mount(tripod);
        assertNear(helper, compose(tripod).value(ShotComponents.SPREAD),
                compose(plain).value(ShotComponents.SPREAD) * (1 - HAConfig.get(HAConfig.TRIPOD_SPREAD_REDUCTION)), "the tripod cuts the braced Gatling's spread further");
        helper.succeed();
    }

    static void fitsAlongsideOtherAttachments(GameTestHelper helper) {
        ItemStack gun = gunWith(ItemRegistry.MUSKET.get(), HAItems.PORTABLE_TRIPOD.get(), ItemRegistry.SCOPE_ATTACHMENT_MODIFIER.get());
        AttachmentMap attachments = gun.get(DataComponentRegistry.ATTACHMENT.get());
        helper.assertTrue(attachments != null && attachments.attachments().containsKey(TripodMount.SOCKET), "the tripod is attached");
        helper.assertTrue(attachments.attachments().size() == 2, "and so is the scope: " + attachments.attachments());
        helper.assertTrue(TripodMount.hasTripod(gun), "hasTripod");
        helper.assertFalse(TripodMount.hasTripod(gunWith(ItemRegistry.MUSKET.get(), ItemRegistry.SCOPE_ATTACHMENT_MODIFIER.get())), "a scope alone is no tripod");
        helper.succeed();
    }

    static void secondTripodAddsNothing(GameTestHelper helper) {
        Player one = holding(helper, gunWith(ItemRegistry.CLOCKWORK_RIFLE.get(), HAItems.PORTABLE_TRIPOD.get()));
        mount(one);
        Player two = holding(helper, gunWith(ItemRegistry.CLOCKWORK_RIFLE.get(), HAItems.PORTABLE_TRIPOD.get(), HAItems.PORTABLE_TRIPOD.get()));
        mount(two);
        assertNear(helper, compose(two).value(HeavyShotComponents.MOUNTED_SPREAD_REDUCTION), HAConfig.get(HAConfig.TRIPOD_SPREAD_REDUCTION), "mounted spread reduction with two tripods");
        assertNear(helper, compose(two).value(HeavyShotComponents.MOUNTED_RECOIL_REDUCTION), HAConfig.get(HAConfig.TRIPOD_RECOIL_REDUCTION), "mounted recoil reduction with two tripods");
        assertNear(helper, compose(two).value(ShotComponents.SPREAD), compose(one).value(ShotComponents.SPREAD), "spread with two tripods");
        assertNear(helper, compose(two).value(ShotComponents.CAMERA_RECOIL_MULTIPLIER), compose(one).value(ShotComponents.CAMERA_RECOIL_MULTIPLIER),
                "recoil with two tripods");
        helper.succeed();
    }

    static void descriptionSaysWhatItDoes(GameTestHelper helper) {
        List<Component> lines = new ArrayList<>();
        new PortableTripodModifier().getDescriptionText(lines::add);
        String text = String.join(" | ", lines.stream().map(Component::getString).toList());
        helper.assertTrue(text.equals("Enables Tripod Mounting"), "description: " + text);
        helper.assertTrue(new ItemStack(HAItems.PORTABLE_TRIPOD.get()).getHoverName().getString().equals("Tripod Attachment Modifier"), "item name");
        helper.succeed();
    }

    static void recipeCraftsTripod(GameTestHelper helper) {
        ItemStack heavy = new ItemStack(HAItems.HEAVY_COMPONENTS.get());
        ItemStack iron = new ItemStack(Items.IRON_INGOT);
        CraftingInput grid = CraftingInput.of(3, 3, List.of(
                ItemStack.EMPTY, heavy, ItemStack.EMPTY,
                ItemStack.EMPTY, iron.copy(), ItemStack.EMPTY,
                iron.copy(), ItemStack.EMPTY, iron.copy()));
        var match = helper.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, helper.getLevel());
        helper.assertTrue(match.isPresent() && match.get().id().equals(HeavyArtillery.id("portable_tripod_attachment_modifier")),
                "Heavy Components on three iron ingots craft the Tripod Attachment Modifier");
        helper.succeed();
    }

    /* **********************************
     * Fixtures
     ********************************** */

    private static Player holding(GameTestHelper helper, ItemStack gun) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemSlot(EquipmentSlot.MAINHAND, gun);
        player.setOnGround(true);
        return player;
    }

    /** Sneaking on the ground. */
    private static void mount(Player player) {
        player.setShiftKeyDown(true);
        player.setPose(Pose.CROUCHING);
        player.setOnGround(true);
    }

    private static ShotProfile compose(Player player) {
        ItemStack gun = player.getMainHandItem();
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
        helper.assertTrue(Math.abs(actual - expected) < 1e-6, what + ": expected " + expected + ", was " + actual);
    }
}
