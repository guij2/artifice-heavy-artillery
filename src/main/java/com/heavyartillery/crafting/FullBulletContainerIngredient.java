package com.heavyartillery.crafting;

import com.google.gson.JsonObject;
import io.redspace.irons_artifice.item.BulletContainerContents;
import io.redspace.irons_artifice.item.BulletContainerItem;
import io.redspace.irons_artifice.registry.IronsArtificeItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Matches any Iron's Arms 'n Artifice bullet container (the Bullet Box) that is filled to capacity.
 * <p>
 * JSON: {@code {"type": "artifice_heavy_artillery:full_bullet_container"}}
 */
public final class FullBulletContainerIngredient extends AbstractIngredient {
    public static final FullBulletContainerIngredient INSTANCE = new FullBulletContainerIngredient();
    public static final IIngredientSerializer<FullBulletContainerIngredient> SERIALIZER = new IIngredientSerializer<>() {
        @Override
        public FullBulletContainerIngredient parse(FriendlyByteBuf buffer) {
            return INSTANCE;
        }

        @Override
        public FullBulletContainerIngredient parse(JsonObject json) {
            return INSTANCE;
        }

        @Override
        public void write(FriendlyByteBuf buffer, FullBulletContainerIngredient ingredient) {
        }
    };

    private FullBulletContainerIngredient() {
        // Each container filled to the brim, so recipe viewers make the requirement obvious. Worked out when first
        // shown, as the items may not be registered yet when this is made.
        super(Stream.of(new Ingredient.Value() {
            @Override
            public Collection<ItemStack> getItems() {
                return ForgeRegistries.ITEMS.getValues().stream()
                        .filter(BulletContainerItem.class::isInstance)
                        .map(FullBulletContainerIngredient::full)
                        .toList();
            }

            @Override
            public JsonObject serialize() {
                return json();
            }
        }));
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof BulletContainerItem)) {
            return false;
        }
        BulletContainerContents contents = BulletContainerItem.contents(stack);
        return contents.capacity() > 0 && contents.count() >= contents.capacity();
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
        return json();
    }

    private static JsonObject json() {
        JsonObject json = new JsonObject();
        json.addProperty("type", HAIngredientTypes.FULL_BULLET_CONTAINER.toString());
        return json;
    }

    private static ItemStack full(Item container) {
        ItemStack stack = new ItemStack(container);
        BulletContainerContents empty = BulletContainerItem.contents(stack);
        if (empty.capacity() <= 0) {
            return stack;
        }
        Item bullet = IronsArtificeItems.BULLET.get();
        int stackSize = new ItemStack(bullet).getMaxStackSize();
        List<ItemStack> stacks = new ArrayList<>();
        for (int remaining = empty.capacity(); remaining > 0; remaining -= stackSize) {
            stacks.add(new ItemStack(bullet, Math.min(stackSize, remaining)));
        }
        BulletContainerContents.set(stack, new BulletContainerContents(empty.capacity(), stacks));
        return stack;
    }
}
