package com.heavyartillery.datagen;

import com.geckolib.renderer.internal.GeckolibItemSpecialRenderer;
import com.heavyartillery.HeavyArtillery;
import com.heavyartillery.registry.HAItems;
import com.heavyartillery.weapon.HeavyGunItem;
import io.redspace.irons_artifice.item.GunItem;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.stream.Stream;

public class HAItemModelProvider extends ModelProvider {
    /**
     * Display transforms (hand, GUI, ground, frame) for heavy guns; see {@code models/item/heavy_gun_display.json}.
     */
    public static final Identifier HEAVY_GUN_DISPLAY = HeavyArtillery.id("item/heavy_gun_display");
    /**
     * The same for heavy guns shouldered in the parent's rifle pose (no {@code HeavyHold}): held level in third person.
     */
    public static final Identifier HEAVY_SHOULDERED_DISPLAY = HeavyArtillery.id("item/heavy_shouldered_display");

    public HAItemModelProvider(PackOutput output) {
        super(output, HeavyArtillery.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        for (var holder : HAItems.ITEMS.getEntries()) {
            Item item = holder.get();
            if (item instanceof GunItem) {
                Identifier display = item instanceof HeavyGunItem heavy && heavy.traits().hold() == null ? HEAVY_SHOULDERED_DISPLAY : HEAVY_GUN_DISPLAY;
                itemModels.itemModelOutput.accept(item,
                        ItemModelUtils.specialModel(display, new GeckolibItemSpecialRenderer.Unbaked<>()));
            } else {
                Identifier model = ModelLocationUtils.getModelLocation(item);
                ModelTemplates.FLAT_ITEM.create(model,
                        TextureMapping.layer0(new Material(holder.getId().withPrefix("item/"))),
                        itemModels.modelOutput);
                itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(model));
            }
        }
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return Stream.empty();
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return HAItems.ITEMS.getEntries().stream().map(holder -> holder);
    }
}
