package com.heavyartillery.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import java.util.stream.Stream;

/**
 * Matches an enchanted book that stores {@code enchantment} at {@code min_level} or higher, whatever else it carries.
 * (NeoForge's data component ingredient can only match the stored enchantments exactly.)
 * <p>
 * JSON: {@code {"neoforge:ingredient_type": "artifice_heavy_artillery:stored_enchantment", "enchantment": "minecraft:infinity"}}
 */
public record StoredEnchantmentIngredient(Holder<Enchantment> enchantment, int minLevel) implements ICustomIngredient {
    public static final MapCodec<StoredEnchantmentIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Enchantment.CODEC.fieldOf("enchantment").forGetter(StoredEnchantmentIngredient::enchantment),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("min_level", 1).forGetter(StoredEnchantmentIngredient::minLevel)
    ).apply(instance, StoredEnchantmentIngredient::new));

    @Override
    public boolean test(ItemStack stack) {
        return stack.is(Items.ENCHANTED_BOOK)
                && stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).getLevel(enchantment) >= minLevel;
    }

    @Override
    public Stream<Holder<Item>> items() {
        return Stream.of(BuiltInRegistries.ITEM.wrapAsHolder(Items.ENCHANTED_BOOK));
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return HAIngredientTypes.STORED_ENCHANTMENT.get();
    }

    @Override
    public SlotDisplay display() {
        ItemStack book = EnchantmentHelper.createBook(new EnchantmentInstance(enchantment, minLevel));
        return new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(book));
    }
}
