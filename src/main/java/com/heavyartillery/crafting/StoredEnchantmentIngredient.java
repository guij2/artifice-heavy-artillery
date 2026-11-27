package com.heavyartillery.crafting;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Matches an enchanted book that stores {@code enchantment} at {@code min_level} or higher, whatever else it carries.
 * (Forge's NBT ingredients can only match a book by its exact tag.)
 * <p>
 * JSON: {@code {"type": "artifice_heavy_artillery:stored_enchantment", "enchantment": "minecraft:infinity"}}
 */
public final class StoredEnchantmentIngredient extends AbstractIngredient {
    public static final IIngredientSerializer<StoredEnchantmentIngredient> SERIALIZER = new IIngredientSerializer<>() {
        @Override
        public StoredEnchantmentIngredient parse(FriendlyByteBuf buffer) {
            return new StoredEnchantmentIngredient(enchantment(buffer.readResourceLocation()), buffer.readVarInt());
        }

        @Override
        public StoredEnchantmentIngredient parse(JsonObject json) {
            int minLevel = GsonHelper.getAsInt(json, "min_level", 1);
            if (minLevel < 1) {
                throw new JsonSyntaxException("min_level must be at least 1, was " + minLevel);
            }
            return new StoredEnchantmentIngredient(enchantment(new ResourceLocation(GsonHelper.getAsString(json, "enchantment"))), minLevel);
        }

        @Override
        public void write(FriendlyByteBuf buffer, StoredEnchantmentIngredient ingredient) {
            buffer.writeResourceLocation(ForgeRegistries.ENCHANTMENTS.getKey(ingredient.enchantment));
            buffer.writeVarInt(ingredient.minLevel);
        }

        private static Enchantment enchantment(ResourceLocation id) {
            Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(id);
            if (enchantment == null) {
                throw new JsonSyntaxException("Unknown enchantment " + id);
            }
            return enchantment;
        }
    };

    private final Enchantment enchantment;
    private final int minLevel;

    public StoredEnchantmentIngredient(Enchantment enchantment, int minLevel) {
        super(Stream.of(new Ingredient.Value() {
            @Override
            public Collection<ItemStack> getItems() {
                return List.of(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, minLevel)));
            }

            @Override
            public JsonObject serialize() {
                return json(enchantment, minLevel);
            }
        }));
        this.enchantment = enchantment;
        this.minLevel = minLevel;
    }

    public Enchantment enchantment() {
        return enchantment;
    }

    public int minLevel() {
        return minLevel;
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        return stack != null && stack.is(Items.ENCHANTED_BOOK)
                && EnchantmentHelper.getEnchantments(stack).getOrDefault(enchantment, 0) >= minLevel;
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public JsonObject toJson() {
        return json(enchantment, minLevel);
    }

    private static JsonObject json(Enchantment enchantment, int minLevel) {
        JsonObject json = new JsonObject();
        json.addProperty("type", HAIngredientTypes.STORED_ENCHANTMENT.toString());
        json.addProperty("enchantment", String.valueOf(ForgeRegistries.ENCHANTMENTS.getKey(enchantment)));
        if (minLevel != 1) {
            json.addProperty("min_level", minLevel);
        }
        return json;
    }
}
