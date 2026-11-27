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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

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
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HeavyArtillery.MODID);

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }

    /* **********************************
     * Weapons
     ********************************** */
    public static final RegistryObject<HeavyGunItem> GUNSTEEL_GATLING = ITEMS.register("gunsteel_gatling", () -> new HeavyGunItem(new Item.Properties(), HeavyWeapons.GUNSTEEL_GATLING, HeavyWeapons.GUNSTEEL_GATLING_TRAITS, HAConfig.GATLING)
    );
    public static final RegistryObject<HeavyGunItem> GUNSTEEL_CRUCIBLE = ITEMS.register("gunsteel_crucible", () -> new HeavyGunItem(new Item.Properties(), HeavyWeapons.GUNSTEEL_CRUCIBLE, HeavyWeapons.GUNSTEEL_CRUCIBLE_TRAITS, HAConfig.CRUCIBLE)
    );

    public static final RegistryObject<HeavyGunItem> GUNSTEEL_CONGREVE = ITEMS.register("gunsteel_congreve", () -> new HeavyGunItem(new Item.Properties(), HeavyWeapons.GUNSTEEL_CONGREVE, HeavyWeapons.GUNSTEEL_CONGREVE_TRAITS, HAConfig.CONGREVE)
    );

    /* **********************************
     * Crafting components
     ********************************** */
    /**
     * Tier above Iron's Arms 'n Artifice's Clockwork Components (simple mechanical -> mechanical -> clockwork -> heavy).
     * Forged gunsteel, copper pipework and forge heat; the shared ingredient of every Heavy Artillery weapon.
     */
    public static final RegistryObject<Item> HEAVY_COMPONENTS = ITEMS.register("heavy_components", () -> new Item(new Item.Properties()));

    /* **********************************
     * Modifiers
     ********************************** */
    /**
     * Infinite ammo: reloads are free and need no bullets in the inventory.
     */
    public static final RegistryObject<ModifierItem> ENCHANTED_MAGAZINE = ITEMS.register("enchanted_magazine_modifier", () -> new ModifierItem(new Item.Properties().stacksTo(1), new EnchantedMagazineModifier()) {
                @Override
                public boolean isFoil(ItemStack stack) {
                    return true;
                }
            }
    );
    /**
     * Crouch to mount the gun on a tripod: much steadier, but you can't move. Named like the parent's attachments.
     */
    public static final RegistryObject<ModifierItem> PORTABLE_TRIPOD = ITEMS.register("portable_tripod_attachment_modifier", () -> new ModifierItem(new Item.Properties().stacksTo(1), new PortableTripodModifier())
    );
    /**
     * Hold use to aim down a 1x sight (no zoom): a little less spread and recoil while aiming. Named like the parent's
     * attachments.
     */
    public static final RegistryObject<ModifierItem> REFLEX_SIGHT = ITEMS.register("reflex_sight_attachment_modifier", () -> new ModifierItem(new Item.Properties().stacksTo(1), new ReflexSightModifier())
    );
    /**
     * +50% magazine capacity (rounded up), slower reloads. Stacks.
     */
    public static final RegistryObject<ModifierItem> DRUM_MAGAZINE = ITEMS.register("drum_magazine_modifier", () -> new ModifierItem(new Item.Properties().stacksTo(1), new DrumMagazineModifier())
    );
    /**
     * Damage grows with the distance a shot flies, slower reloads. Stacks.
     */
    public static final RegistryObject<ModifierItem> RIFLED_BARREL = ITEMS.register("rifled_barrel_modifier", () -> new ModifierItem(new Item.Properties().stacksTo(1), new RifledBarrelModifier())
    );
    /**
     * Damage grows with the time since the last shot. Stacks.
     */
    public static final RegistryObject<ModifierItem> POWDER_RAM = ITEMS.register("powder_ram_modifier", () -> new ModifierItem(new Item.Properties().stacksTo(1), new PowderRamModifier())
    );
}
