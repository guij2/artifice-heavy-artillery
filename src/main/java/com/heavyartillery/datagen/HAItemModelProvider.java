package com.heavyartillery.datagen;

import com.heavyartillery.HeavyArtillery;
import com.heavyartillery.registry.HAItems;
import com.heavyartillery.weapon.HeavyGunItem;
import io.redspace.irons_artifice.item.GunItem;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class HAItemModelProvider extends ItemModelProvider {
    /**
     * Display transforms (hand, GUI, ground, frame) for heavy guns; see {@code models/item/heavy_gun_display.json}. Its
     * {@code builtin/entity} root hands rendering to the gun's GeckoLib renderer.
     */
    public static final ResourceLocation HEAVY_GUN_DISPLAY = HeavyArtillery.id("item/heavy_gun_display");
    /**
     * The same for heavy guns shouldered in the parent's rifle pose (no {@code HeavyHold}): held level in third person.
     */
    public static final ResourceLocation HEAVY_SHOULDERED_DISPLAY = HeavyArtillery.id("item/heavy_shouldered_display");

    public HAItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, HeavyArtillery.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (var holder : HAItems.ITEMS.getEntries()) {
            Item item = holder.get();
            if (item instanceof GunItem) {
                ResourceLocation display = item instanceof HeavyGunItem heavy && heavy.traits().hold() == null ? HEAVY_SHOULDERED_DISPLAY : HEAVY_GUN_DISPLAY;
                withExistingParent(holder.getId().toString(), display);
            } else {
                basicItem(holder.getId());
            }
        }
    }
}
