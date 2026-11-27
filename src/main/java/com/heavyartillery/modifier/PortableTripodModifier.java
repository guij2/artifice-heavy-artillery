package com.heavyartillery.modifier;

import com.heavyartillery.HAConfig;
import com.heavyartillery.HeavyArtillery;
import com.heavyartillery.weapon.HeavyShotComponents;
import com.heavyartillery.weapon.trait.TripodMount;
import io.redspace.irons_artifice.data.ShotComponentMap;
import io.redspace.irons_artifice.data.ValueModifier;
import io.redspace.irons_artifice.item.AttachmentMap;
import io.redspace.irons_artifice.utils.ModifierPatchHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Crouching on the ground mounts the gun on a tripod: much less spread and recoil, but no moving (see
 * {@link TripodMount}). Like the parent's scope and bayonet, it is an attachment: the folded tripod is drawn under any
 * gun it is fitted to, and unfolds onto the ground when mounted ({@code client.PortableTripodRenderer}). A gun has one
 * tripod however many are fitted: only the first one counts.
 */
public final class PortableTripodModifier extends ConfiguredModifier {
    public PortableTripodModifier() {
        super(() -> Map.of(
                HeavyShotComponents.MOUNTED_SPREAD_REDUCTION, new ValueModifier(HAConfig.get(HAConfig.TRIPOD_SPREAD_REDUCTION), ValueModifier.Operation.ADD),
                HeavyShotComponents.MOUNTED_RECOIL_REDUCTION, new ValueModifier(HAConfig.get(HAConfig.TRIPOD_RECOIL_REDUCTION), ValueModifier.Operation.ADD)
        ));
    }

    @Override
    public void apply(ShotComponentMap components) {
        if (components.getOrDefault(HeavyShotComponents.TRIPOD_FITTED)) {
            return;
        }
        components.set(HeavyShotComponents.TRIPOD_FITTED, true);
        super.apply(components);
    }

    /**
     * Worded like the parent's bayonet ("Enables Bayonet Charge") rather than two "+75% Mounted ... Reduction" stat lines.
     */
    @Override
    public void getDescriptionText(Consumer<Component> builder) {
        builder.accept(Component.translatable("artifice_heavy_artillery.modifier.portable_tripod").withStyle(ChatFormatting.AQUA));
    }

    @Override
    public Optional<CompoundTag> getPatch() {
        CompoundTag patch = new CompoundTag();
        ModifierPatchHandler.set(patch, AttachmentMap.TAG_KEY, AttachmentMap.CODEC, new AttachmentMap(Map.of(
                        TripodMount.SOCKET, HeavyArtillery.id("portable_tripod")
                )));
        return Optional.of(patch);
    }
}
