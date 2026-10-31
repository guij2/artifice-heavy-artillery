package com.heavyartillery.crafting;

import com.mojang.serialization.MapCodec;
import io.redspace.irons_artifice.item.BulletContainerContents;
import io.redspace.irons_artifice.item.BulletContainerItem;
import io.redspace.irons_artifice.registry.DataComponentRegistry;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Matches any Iron's Arms 'n Artifice bullet container (the Bullet Box) that is filled to capacity.
 * <p>
 * JSON: {@code {"neoforge:ingredient_type": "artifice_heavy_artillery:full_bullet_container"}}
 */
public final class FullBulletContainerIngredient implements ICustomIngredient {
    public static final FullBulletContainerIngredient INSTANCE = new FullBulletContainerIngredient();
    public static final MapCodec<FullBulletContainerIngredient> CODEC = MapCodec.unit(INSTANCE);

    private FullBulletContainerIngredient() {
    }

    @Override
    public boolean test(ItemStack stack) {
        if (!(stack.getItem() instanceof BulletContainerItem)) {
            return false;
        }
        BulletContainerContents contents = BulletContainerItem.contents(stack);
        return contents.capacity() > 0 && contents.count() >= contents.capacity();
    }

    @Override
    public Stream<Holder<Item>> items() {
        return BuiltInRegistries.ITEM.listElements()
                .filter(holder -> holder.value() instanceof BulletContainerItem)
                .map(holder -> holder);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return HAIngredientTypes.FULL_BULLET_CONTAINER.get();
    }

    /**
     * Shows each container filled to the brim, so recipe viewers make the requirement obvious.
     */
    @Override
    public SlotDisplay display() {
        return new SlotDisplay.Composite(items().map(FullBulletContainerIngredient::fullDisplay).toList());
    }

    private static SlotDisplay fullDisplay(Holder<Item> container) {
        BulletContainerContents empty = container.value().components().get(DataComponentRegistry.BULLET_POUCH.get());
        if (empty == null || empty.capacity() <= 0) {
            return new SlotDisplay.ItemSlotDisplay(container);
        }
        Item bullet = ItemRegistry.BULLET.get();
        int stackSize = bullet.getDefaultMaxStackSize();
        List<ItemStackTemplate> stacks = new ArrayList<>();
        for (int remaining = empty.capacity(); remaining > 0; remaining -= stackSize) {
            stacks.add(new ItemStackTemplate(bullet, Math.min(stackSize, remaining)));
        }
        DataComponentPatch full = DataComponentPatch.builder()
                .set(DataComponentRegistry.BULLET_POUCH.get(), new BulletContainerContents(empty.capacity(), stacks))
                .build();
        return new SlotDisplay.ItemStackSlotDisplay(new ItemStackTemplate(container, full));
    }
}
