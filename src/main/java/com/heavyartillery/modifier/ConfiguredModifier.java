package com.heavyartillery.modifier;

import io.redspace.irons_artifice.data.ComponentType;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.Value;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.modifier.ValueStackModifier;
import io.redspace.irons_artifice.utils.Utils;
import net.minecraft.network.chat.Component;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A {@link ValueStackModifier} whose amounts come from {@code HAConfig}: they are read each time a shot is composed or
 * the description shown, so a config change applies to modifiers already fitted.
 */
public class ConfiguredModifier extends ValueStackModifier {
    private final Supplier<Map<ComponentType<Value>, ValueModifier>> modifiers;

    public ConfiguredModifier(Supplier<Map<ComponentType<Value>, ValueModifier>> modifiers) {
        super(Map.of());
        this.modifiers = modifiers;
    }

    @Override
    public void apply(ShotComponentMap components) {
        modifiers.get().forEach(components::modifyValue);
    }

    /** The parent's stat lines, worded as {@link ValueStackModifier} words them. */
    @Override
    public void getDescriptionText(Consumer<Component> builder) {
        modifiers.get().entrySet().stream()
                .sorted(Comparator.comparingDouble(entry -> -Math.abs(entry.getValue().amount())))
                .map(entry -> Utils.formatValueModifierDescription(entry.getValue(), entry.getKey()))
                .forEach(builder);
    }
}
