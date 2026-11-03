package com.heavyartillery.registry;

import com.heavyartillery.HAConfig;
import com.heavyartillery.HeavyArtillery;
import com.heavyartillery.modifier.DrumMagazineModifier;
import com.heavyartillery.modifier.EnchantedMagazineModifier;
import com.heavyartillery.modifier.PortableTripodModifier;
import com.heavyartillery.modifier.PowderRamModifier;
import com.heavyartillery.modifier.ReflexSightModifier;
import com.heavyartillery.modifier.RifledBarrelModifier;
import com.heavyartillery.weapon.HeavyGunItem;
import com.heavyartillery.weapon.HeavyWeapons;
import io.redspace.irons_artifice.modifier.ModifierItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Every item in the mod.
 * <p>
 * Weapons: register a {@link HeavyGunItem} with a profile from {@link HeavyWeapons} and its {@link HAConfig} section. The item's registry name is used to
 * look up its GeckoLib assets ({@code geo/item/<name>.geo.json}, {@code animations/item/<name>.animation.json},
 * {@code textures/item/<name>.png}); Iron's Arms 'n Artifice wires up the renderer, arm poses and modifier menu automatically.
 * <p>
 * Modifiers: register an {@code io.redspace.irons_artifice.modifier.ModifierItem} wrapping a {@code GunModifier}
 * (see {@code com.heavyartillery.modifier}). Any modifier works in any gun, ours or the parent mod's.
 */
public final class HAItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HeavyArtillery.MODID);

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }

    /* **********************************
     * Weapons
     ********************************** */
    public static final DeferredItem<HeavyGunItem> GUNSTEEL_GATLING = ITEMS.registerItem("gunsteel_gatling",
            properties -> new HeavyGunItem(properties, HeavyWeapons.GUNSTEEL_GATLING, HeavyWeapons.GUNSTEEL_GATLING_TRAITS, HAConfig.GATLING)
    );
    public static final DeferredItem<HeavyGunItem> GUNSTEEL_CRUCIBLE = ITEMS.registerItem("gunsteel_crucible",
            properties -> new HeavyGunItem(properties, HeavyWeapons.GUNSTEEL_CRUCIBLE, HeavyWeapons.GUNSTEEL_CRUCIBLE_TRAITS, HAConfig.CRUCIBLE)
    );

    public static final DeferredItem<HeavyGunItem> GUNSTEEL_CONGREVE = ITEMS.registerItem("gunsteel_congreve",
            properties -> new HeavyGunItem(properties, HeavyWeapons.GUNSTEEL_CONGREVE, HeavyWeapons.GUNSTEEL_CONGREVE_TRAITS, HAConfig.CONGREVE)
    );

    /* **********************************
     * Crafting components
     ********************************** */
    /**
     * Tier above Iron's Arms 'n Artifice's Clockwork Components (simple mechanical -> mechanical -> clockwork -> heavy).
     * Forged gunsteel, copper pipework and forge heat; the shared ingredient of every Heavy Artillery weapon.
     */
    public static final DeferredItem<Item> HEAVY_COMPONENTS = ITEMS.registerSimpleItem("heavy_components");

    /* **********************************
     * Modifiers
     ********************************** */
    /**
     * Infinite ammo: reloads are free and need no bullets in the inventory.
     */
    public static final DeferredItem<ModifierItem> ENCHANTED_MAGAZINE = ITEMS.registerItem("enchanted_magazine_modifier",
            properties -> new ModifierItem(properties.stacksTo(1).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true), new EnchantedMagazineModifier())
    );
    /**
     * Crouch to mount the gun on a tripod: much steadier, but you can't move. Named like the parent's attachments.
     */
    public static final DeferredItem<ModifierItem> PORTABLE_TRIPOD = ITEMS.registerItem("portable_tripod_attachment_modifier",
            properties -> new ModifierItem(properties.stacksTo(1), new PortableTripodModifier())
    );
    /**
     * Hold use to aim down a 1x sight (no zoom): a little less spread and recoil while aiming. Named like the parent's
     * attachments.
     */
    public static final DeferredItem<ModifierItem> REFLEX_SIGHT = ITEMS.registerItem("reflex_sight_attachment_modifier",
            properties -> new ModifierItem(properties.stacksTo(1), new ReflexSightModifier())
    );
    /**
     * +50% magazine capacity (rounded up), slower reloads. Stacks.
     */
    public static final DeferredItem<ModifierItem> DRUM_MAGAZINE = ITEMS.registerItem("drum_magazine_modifier",
            properties -> new ModifierItem(properties.stacksTo(1), new DrumMagazineModifier())
    );
    /**
     * Damage grows with the distance a shot flies, slower reloads. Stacks.
     */
    public static final DeferredItem<ModifierItem> RIFLED_BARREL = ITEMS.registerItem("rifled_barrel_modifier",
            properties -> new ModifierItem(properties.stacksTo(1), new RifledBarrelModifier())
    );
    /**
     * Damage grows with the time since the last shot. Stacks.
     */
    public static final DeferredItem<ModifierItem> POWDER_RAM = ITEMS.registerItem("powder_ram_modifier",
            properties -> new ModifierItem(properties.stacksTo(1), new PowderRamModifier())
    );
}
