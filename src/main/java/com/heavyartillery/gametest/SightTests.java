package com.heavyartillery.gametest;

import com.heavyartillery.HAConfig;
import com.heavyartillery.HeavyArtillery;
import com.heavyartillery.modifier.ReflexSightModifier;
import com.heavyartillery.registry.HAItems;
import com.heavyartillery.weapon.HeavyShotComponents;
import com.heavyartillery.weapon.trait.SightAim;
import io.redspace.irons_artifice.api.GunBones;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.item.AttachmentMap;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.GunplayManager;
import io.redspace.irons_artifice.menu.GunContainer;
import io.redspace.irons_artifice.registry.DataComponentRegistry;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.UseEffects;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;

import java.util.ArrayList;
import java.util.List;

/**
 * Reflex Sight: holding use aims down it (no zoom), a little steadier, on our guns and the parent's.
 */
final class SightTests {
    private SightTests() {
    }

    static void useAimsDownSight(GameTestHelper helper) {
        Player player = holding(helper, gunWith(ItemRegistry.MUSKET.get(), HAItems.REFLEX_SIGHT.get()));
        aim(player);
        helper.assertTrue(player.isUsingItem(), "using the gun");
        helper.assertTrue(SightAim.isAiming(player), "aiming down the sight");
        helper.assertFalse(GunItem.isScoping(player), "not scoping: no zoom, no overlay");
        helper.assertValueEqual(player.getUseItemRemainingTicks(), SightAim.USE_DURATION, "aims for as long as use is held");
        player.releaseUsingItem();
        helper.assertFalse(SightAim.isAiming(player), "letting go stops aiming");
        helper.succeed();
    }

    static void withoutSightUseDoesNotAim(GameTestHelper helper) {
        Player player = holding(helper, gunWith(ItemRegistry.MUSKET.get()));
        aim(player);
        helper.assertFalse(SightAim.isAiming(player), "a plain gun doesn't aim");
        helper.succeed();
    }

    static void aimingCutsSpreadAndRecoil(GameTestHelper helper) {
        Player plain = holding(helper, gunWith(ItemRegistry.CLOCKWORK_RIFLE.get()));
        Player sighted = holding(helper, gunWith(ItemRegistry.CLOCKWORK_RIFLE.get(), HAItems.REFLEX_SIGHT.get()));
        aim(sighted);
        ShotProfile hip = compose(plain);
        ShotProfile aimed = compose(sighted);
        helper.assertTrue(hip.value(ShotComponents.SPREAD) > 0, "the clockwork rifle has some spread to cut");
        assertNear(helper, aimed.value(ShotComponents.SPREAD), hip.value(ShotComponents.SPREAD) * (1 - HAConfig.get(HAConfig.SIGHT_SPREAD_REDUCTION)), "spread while aiming");
        assertNear(helper, aimed.value(ShotComponents.CAMERA_RECOIL_MULTIPLIER),
                hip.value(ShotComponents.CAMERA_RECOIL_MULTIPLIER) * (1 - HAConfig.get(HAConfig.SIGHT_RECOIL_REDUCTION)), "recoil while aiming");
        helper.succeed();
    }

    static void noEffectFromTheHip(GameTestHelper helper) {
        Player plain = holding(helper, gunWith(HAItems.GUNSTEEL_GATLING.get()));
        Player sighted = holding(helper, gunWith(HAItems.GUNSTEEL_GATLING.get(), HAItems.REFLEX_SIGHT.get()));
        assertNear(helper, compose(sighted).value(ShotComponents.SPREAD), compose(plain).value(ShotComponents.SPREAD), "spread from the hip");
        assertNear(helper, compose(sighted).value(ShotComponents.CAMERA_RECOIL_MULTIPLIER), compose(plain).value(ShotComponents.CAMERA_RECOIL_MULTIPLIER),
                "recoil from the hip");
        helper.succeed();
    }

    static void secondSightAddsNothing(GameTestHelper helper) {
        Player one = holding(helper, gunWith(ItemRegistry.CLOCKWORK_RIFLE.get(), HAItems.REFLEX_SIGHT.get()));
        Player two = holding(helper, gunWith(ItemRegistry.CLOCKWORK_RIFLE.get(), HAItems.REFLEX_SIGHT.get(), HAItems.REFLEX_SIGHT.get()));
        aim(one);
        aim(two);
        assertNear(helper, compose(two).value(HeavyShotComponents.AIMED_SPREAD_REDUCTION), HAConfig.get(HAConfig.SIGHT_SPREAD_REDUCTION), "aimed spread reduction with two sights");
        assertNear(helper, compose(two).value(ShotComponents.SPREAD), compose(one).value(ShotComponents.SPREAD), "spread with two sights");
        assertNear(helper, compose(two).value(ShotComponents.CAMERA_RECOIL_MULTIPLIER), compose(one).value(ShotComponents.CAMERA_RECOIL_MULTIPLIER),
                "recoil with two sights");
        helper.succeed();
    }

    static void fitsTheOpticSocket(GameTestHelper helper) {
        ItemStack gun = gunWith(ItemRegistry.MUSKET.get(), HAItems.REFLEX_SIGHT.get(), HAItems.PORTABLE_TRIPOD.get());
        AttachmentMap attachments = gun.get(DataComponentRegistry.ATTACHMENT.get());
        helper.assertTrue(attachments != null && SightAim.ATTACHMENT.equals(attachments.attachments().get(GunBones.SOCKET_OPTIC)),
                "the sight sits in the optic socket: " + attachments);
        helper.assertTrue(attachments.attachments().size() == 2, "alongside the tripod: " + attachments.attachments());
        UseEffects effects = gun.get(DataComponents.USE_EFFECTS);
        helper.assertTrue(effects != null && !effects.canSprint() && effects.speedMultiplier() == HAConfig.get(HAConfig.SIGHT_AIMED_SPEED).floatValue(),
                "aiming walks at " + HAConfig.get(HAConfig.SIGHT_AIMED_SPEED).floatValue() + " and can't sprint: " + effects);
        helper.succeed();
    }

    static void scopeTakesOver(GameTestHelper helper) {
        Player player = holding(helper, gunWith(ItemRegistry.MUSKET.get(), HAItems.REFLEX_SIGHT.get(), ItemRegistry.SCOPE_ATTACHMENT_MODIFIER.get()));
        aim(player);
        helper.assertTrue(GunItem.isScoping(player), "a gun with a scope as well scopes");
        helper.assertFalse(SightAim.isAiming(player), "and doesn't aim down the sight");
        helper.succeed();
    }

    static void descriptionSaysWhatItDoes(GameTestHelper helper) {
        List<Component> lines = new ArrayList<>();
        new ReflexSightModifier().getDescriptionText(lines::add);
        String text = String.join(" | ", lines.stream().map(Component::getString).toList());
        helper.assertTrue(text.equals("Enables Aiming Down Sights | -30% Spread and -30% Recoil while aiming"), "description: " + text);
        helper.assertTrue(new ItemStack(HAItems.REFLEX_SIGHT.get()).getHoverName().getString().equals("Reflex Sight Attachment Modifier"), "item name");
        helper.succeed();
    }

    static void recipeCraftsSight(GameTestHelper helper) {
        ItemStack copper = new ItemStack(Items.COPPER_INGOT);
        CraftingInput grid = CraftingInput.of(3, 2, List.of(
                copper.copy(), new ItemStack(Items.GLASS_PANE), copper.copy(),
                ItemStack.EMPTY, new ItemStack(ItemRegistry.SIMPLE_MECHANICAL_COMPONENTS.get()), ItemStack.EMPTY));
        var match = helper.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, helper.getLevel());
        helper.assertTrue(match.isPresent() && match.get().id().equals(ResourceKey.create(Registries.RECIPE, HeavyArtillery.id("reflex_sight_attachment_modifier"))),
                "a glass pane between copper ingots, on simple mechanical components, crafts the Reflex Sight Attachment Modifier");
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

    /** Holds use with the gun, as right-clicking does. */
    private static void aim(Player player) {
        player.getMainHandItem().use(player.level(), player, InteractionHand.MAIN_HAND);
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
