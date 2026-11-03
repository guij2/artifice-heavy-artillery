package com.heavyartillery.modifier;

import com.heavyartillery.HAConfig;
import com.heavyartillery.weapon.HeavyShotComponents;
import com.heavyartillery.weapon.trait.SightAim;
import io.redspace.irons_artifice.api.GunBones;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.item.AttachmentMap;
import io.redspace.irons_artifice.registry.DataComponentRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A 1x sight: holding use aims down it without zooming, so it works up close (see {@link SightAim}). Spread and recoil
 * drop a little while aiming, and its holder walks slower and can't sprint. Like the parent's scope it fills the optic
 * socket, so a gun has one sight however many are fitted: only the first one counts. A gun with a scope as well scopes.
 */
public final class ReflexSightModifier extends ConfiguredModifier {
    public ReflexSightModifier() {
        super(() -> Map.of(
                HeavyShotComponents.AIMED_SPREAD_REDUCTION, new ValueModifier(HAConfig.get(HAConfig.SIGHT_SPREAD_REDUCTION), ValueModifier.Operation.ADD, ValueModifier.Type.BENEFICIAL),
                HeavyShotComponents.AIMED_RECOIL_REDUCTION, new ValueModifier(HAConfig.get(HAConfig.SIGHT_RECOIL_REDUCTION), ValueModifier.Operation.ADD, ValueModifier.Type.BENEFICIAL)
        ));
    }

    @Override
    public void apply(ShotComponentMap components) {
        if (components.getOrDefault(HeavyShotComponents.SIGHT_FITTED)) {
            return;
        }
        components.set(HeavyShotComponents.SIGHT_FITTED, true);
        super.apply(components);
    }

    /**
     * Worded like the parent's scope ("Enables Scoping"), with the aimed bonus on one line rather than two
     * "+30% Aimed ... Reduction" stat lines.
     */
    @Override
    public void getDescriptionText(Consumer<Component> builder) {
        builder.accept(Component.translatable("artifice_heavy_artillery.modifier.reflex_sight").withStyle(ChatFormatting.AQUA));
        builder.accept(Component.translatable("artifice_heavy_artillery.modifier.reflex_sight.aimed",
                ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(HAConfig.get(HAConfig.SIGHT_SPREAD_REDUCTION) * 100),
                ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(HAConfig.get(HAConfig.SIGHT_RECOIL_REDUCTION) * 100)).withStyle(ChatFormatting.GREEN));
    }

    @Override
    public Optional<DataComponentPatch> getPatch() {
        return Optional.of(DataComponentPatch.builder()
                .set(DataComponentRegistry.ATTACHMENT.get(), new AttachmentMap(Map.of(
                        GunBones.SOCKET_OPTIC, SightAim.ATTACHMENT
                )))
                .build());
    }
}
