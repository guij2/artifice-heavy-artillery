package com.heavyartillery.registry;

import com.heavyartillery.HeavyArtillery;
import io.redspace.irons_artifice.IronsArtifice;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class HACreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HeavyArtillery.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = CREATIVE_MODE_TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + HeavyArtillery.MODID))
            .withTabsBefore(IronsArtifice.CREATIVE_TAB.getKey())
            .icon(() -> HAItems.GUNSTEEL_GATLING.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                for (var item : HAItems.ITEMS.getEntries()) {
                    output.accept(item.get());
                }
            }).build());

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
