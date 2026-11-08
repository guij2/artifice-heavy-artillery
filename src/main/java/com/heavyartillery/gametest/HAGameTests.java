package com.heavyartillery.gametest;

import com.heavyartillery.HeavyArtillery;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.gametest.GameTestHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Game tests, run with {@code ./gradlew runGameTestServer} (report in {@code build/gametest-report.xml}, written by the
 * parent's reporter).
 * <p>
 * Add a test: write a {@code static void name(GameTestHelper)} body and list it in {@link #TESTS}.
 */
@GameTestHolder(HeavyArtillery.MODID)
public final class HAGameTests {
    /**
     * A small empty room, like Iron's Arms 'n Artifice's box_small ({@code tools/gen_test_arena.py}). Ours, as tests only
     * run if their structure is in an enabled game test namespace.
     */
    private static final ResourceLocation ARENA = HeavyArtillery.id("arena");
    private static final int MAX_TICKS = 40;
    private static final String BATCH = "defaultBatch";

    private static final Map<String, Consumer<GameTestHelper>> TESTS = new LinkedHashMap<>();

    static {
        TESTS.put("enchanted_magazine_free_reload_with_one_bullet", EnchantedMagazineTests::freeReloadWithOneBullet);
        TESTS.put("enchanted_magazine_free_reload_from_bullet_box", EnchantedMagazineTests::freeReloadFromBulletBox);
        TESTS.put("enchanted_magazine_free_reload_with_no_bullets", EnchantedMagazineTests::freeReloadWithNoBullets);
        TESTS.put("no_bullets_no_reload_without_modifier", EnchantedMagazineTests::noBulletsNoReloadWithoutModifier);
        TESTS.put("enchanted_magazine_free_reload_through_full_flow", EnchantedMagazineTests::freeReloadThroughFullFlow);
        TESTS.put("enchanted_magazine_description_is_one_line", EnchantedMagazineTests::descriptionIsOneLine);
        TESTS.put("reload_without_modifier_consumes", EnchantedMagazineTests::reloadWithoutModifierConsumes);
        TESTS.put("reload_without_modifier_is_capped", EnchantedMagazineTests::reloadWithoutModifierIsCapped);
        TESTS.put("enchanted_magazine_works_on_parent_guns", EnchantedMagazineTests::worksOnParentGuns);
        TESTS.put("full_bullet_box_ingredient", EnchantedMagazineTests::fullBulletBoxIngredient);
        TESTS.put("infinity_book_ingredient", EnchantedMagazineTests::infinityBookIngredient);
        TESTS.put("enchanted_magazine_recipe", EnchantedMagazineTests::recipeCraftsEnchantedMagazine);
        TESTS.put("crucible_spray_range_is_short", CrucibleTests::sprayRangeIsShort);
        TESTS.put("crucible_spray_burns_target", CrucibleTests::sprayBurnsTarget);
        TESTS.put("crucible_spray_leaves_blocks_alone", CrucibleTests::sprayLeavesBlocksAlone);
        TESTS.put("crucible_breaching_shell_burns_blocks", CrucibleTests::breachingShellBurnsBlocks);
        TESTS.put("crucible_spray_quenched_underwater", CrucibleTests::sprayIsQuenchedUnderwater);
        TESTS.put("crucible_tooltip_shows_spray_range", CrucibleTests::tooltipShowsSprayRange);
        TESTS.put("congreve_rocket_bursts_on_impact", CongreveTests::rocketBurstsOnImpact);
        TESTS.put("congreve_blackpowder_charge_widens_burst", CongreveTests::blackpowderChargeWidensBurst);
        TESTS.put("congreve_rocket_costs_eight_bullets", CongreveTests::rocketCostsEightBullets);
        TESTS.put("congreve_too_few_bullets_for_a_rocket", CongreveTests::tooFewBulletsForARocket);
        TESTS.put("congreve_enchanted_magazine_makes_rockets_free", CongreveTests::enchantedMagazineMakesRocketsFree);
        TESTS.put("congreve_blowback_depends_on_footing", CongreveTests::blowbackDependsOnFooting);
        TESTS.put("congreve_rocket_has_a_model_in_flight", CongreveTests::rocketHasAModelInFlight);
        TESTS.put("congreve_rocket_drops_less_than_a_bullet", CongreveTests::rocketDropsLessThanABullet);
        TESTS.put("congreve_tooltip", CongreveTests::tooltipShowsCostOnly);
        TESTS.put("congreve_recipe", CongreveTests::recipeCraftsCongreve);
        TESTS.put("tripod_mounted_cuts_spread_and_recoil", TripodTests::mountedCutsSpreadAndRecoil);
        TESTS.put("tripod_no_effect_standing", TripodTests::noEffectStanding);
        TESTS.put("tripod_mounting_needs_sneak_and_ground", TripodTests::mountingNeedsSneakAndGround);
        TESTS.put("tripod_stacks_with_brace", TripodTests::stacksWithBrace);
        TESTS.put("tripod_fits_alongside_other_attachments", TripodTests::fitsAlongsideOtherAttachments);
        TESTS.put("tripod_does_not_stack", TripodTests::secondTripodAddsNothing);
        TESTS.put("tripod_description", TripodTests::descriptionSaysWhatItDoes);
        TESTS.put("tripod_recipe", TripodTests::recipeCraftsTripod);
        TESTS.put("sight_use_aims_down_sight", SightTests::useAimsDownSight);
        TESTS.put("sight_plain_gun_does_not_aim", SightTests::withoutSightUseDoesNotAim);
        TESTS.put("sight_aiming_cuts_spread_and_recoil", SightTests::aimingCutsSpreadAndRecoil);
        TESTS.put("sight_no_effect_from_the_hip", SightTests::noEffectFromTheHip);
        TESTS.put("sight_does_not_stack", SightTests::secondSightAddsNothing);
        TESTS.put("sight_fits_the_optic_socket", SightTests::fitsTheOpticSocket);
        TESTS.put("sight_scope_takes_over", SightTests::scopeTakesOver);
        TESTS.put("sight_description", SightTests::descriptionSaysWhatItDoes);
        TESTS.put("sight_recipe", SightTests::recipeCraftsSight);
        TESTS.put("balance_heavy_guns_out_slot_parent_guns", BalanceTests::heavyGunsOutSlotParentGuns);
        TESTS.put("balance_crucible_full_strength_up_close", BalanceTests::crucibleFullStrengthUpClose);
        TESTS.put("balance_congreve_craters_unchanged", BalanceTests::congreveCratersUnchanged);
        TESTS.put("drum_enlarges_magazine", ModifierTests::drumEnlargesMagazine);
        TESTS.put("drums_slow_reload_and_stack", ModifierTests::drumsSlowReloadAndStack);
        TESTS.put("drum_reload_through_full_flow", ModifierTests::drumReloadThroughFullFlow);
        TESTS.put("drum_fitted_mid_reload", ModifierTests::drumFittedMidReload);
        TESTS.put("rifled_shot_hits_harder_after_flying", ModifierTests::rifledShotHitsHarderAfterFlying);
        TESTS.put("rifling_caps_and_stacks", ModifierTests::riflingCapsAndStacks);
        TESTS.put("powder_ram_charges_with_time", ModifierTests::powderRamChargesWithTime);
        TESTS.put("new_modifier_descriptions", ModifierTests::descriptions);
        TESTS.put("new_modifier_recipes", ModifierTests::recipes);
        TESTS.put("config_gun_stats", ConfigTests::gunStatsFollowConfig);
        TESTS.put("config_magazine_and_reload", ConfigTests::magazineAndReloadFollowConfig);
        TESTS.put("config_movement_penalty", ConfigTests::movementPenaltyFollowsConfig);
        TESTS.put("config_weapon_numbers", ConfigTests::weaponNumbersFollowConfig);
        TESTS.put("config_modifier_numbers", ConfigTests::modifierNumbersFollowConfig);
        TESTS.put("config_modifier_slots", ConfigTests::modifierSlotsFollowConfig);
    }

    private HAGameTests() {
    }

    @GameTestGenerator
    public static Collection<TestFunction> generateTests() {
        List<TestFunction> functions = new ArrayList<>();
        TESTS.forEach((name, body) -> functions.add(new TestFunction(BATCH, HeavyArtillery.MODID + "." + name, ARENA.toString(), MAX_TICKS, 0L, true, body)));
        return functions;
    }
}
