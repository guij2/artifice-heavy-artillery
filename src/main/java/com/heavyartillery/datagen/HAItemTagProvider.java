package com.heavyartillery.datagen;

import com.heavyartillery.registry.HAItems;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.utils.IronsArtificeTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.world.item.Item;

import java.util.concurrent.CompletableFuture;

public class HAItemTagProvider extends IntrinsicHolderTagsProvider<Item> {
    public HAItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.ITEM, lookupProvider, item -> item.builtInRegistryHolder().key());
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        // join the parent mod's gun tag so anything keyed off it (recipes, loot, other addons) sees our weapons
        var guns = this.tag(IronsArtificeTags.GUNS);
        for (var holder : HAItems.ITEMS.getEntries()) {
            if (holder.get() instanceof GunItem) {
                guns.add(holder.get());
            }
        }
    }
}
