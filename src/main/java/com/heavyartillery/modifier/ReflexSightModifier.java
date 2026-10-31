package com.heavyartillery.modifier;

import com.heavyartillery.weapon.HeavyShotComponents;
import com.heavyartillery.weapon.trait.SightAim;
import io.redspace.irons_artifice.api.GunBones;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.item.AttachmentMap;
import io.redspace.irons_artifice.modifier.ValueStackModifier;
import io.redspace.irons_artifice.registry.DataComponentRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.UseEffects;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A 1x sight: holding use aims down it without zooming, so it works up close (see {@link SightAim}). Spread and recoil
 * drop a little while aiming, and its holder walks slower and can't sprint. Like the parent's scope it fills the optic
 * socket, so a gun has one sight however many are fitted: only the first one counts. A gun with a scope as well scopes.
 */
public final class ReflexSightModifier extends ValueStackModifier {
    public static final double SPREAD_REDUCTION = 0.3;
    public static final double RECOIL_REDUCTION = 0.3;
    /** Walking speed while aiming (vanilla item use is 0.2, sneaking 0.3). */
    public static final float AIMED_SPEED = 0.6f;

    public ReflexSightModifier() {
        super(Map.of(
                HeavyShotComponents.AIMED_SPREAD_REDUCTION, new ValueModifier(SPREAD_REDUCTION, ValueModifier.Operation.ADD, ValueModifier.Type.BENEFICIAL),
                HeavyShotComponents.AIMED_RECOIL_REDUCTION, new ValueModifier(RECOIL_REDUCTION, ValueModifier.Operation.ADD, ValueModifier.Type.BENEFICIAL)
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
                Math.round(SPREAD_REDUCTION * 100), Math.round(RECOIL_REDUCTION * 100)).withStyle(ChatFormatting.GREEN));
    }

    @Override
    public Optional<DataComponentPatch> getPatch() {
        return Optional.of(DataComponentPatch.builder()
                .set(DataComponents.USE_EFFECTS, new UseEffects(false, false, AIMED_SPEED))
                .set(DataComponentRegistry.ATTACHMENT.get(), new AttachmentMap(Map.of(
                        GunBones.SOCKET_OPTIC, SightAim.ATTACHMENT
                )))
                .build());
    }
}
