package com.heavyartillery.weapon;

import com.heavyartillery.HeavyArtillery;
import com.heavyartillery.weapon.trait.MoltenShot;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.gun.GunProfile;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.GunplayManager;
import io.redspace.irons_artifice.modifier.on_hit_handlers.BlackpowderChargeOnHit;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A {@link GunItem} with {@link HeavyTraits}. Everything gun-related (firing, reloading, modifiers, rendering) is
 * handled by Iron's Arms 'n Artifice; this class only adds the carried-weapon traits and a one-line tooltip.
 */
public class HeavyGunItem extends GunItem {
    public static final ResourceLocation MOVEMENT_PENALTY_ID = HeavyArtillery.id("heavy_weapon_movement_penalty");

    private final HeavyTraits traits;

    public HeavyGunItem(Properties properties, GunProfile gunProfile, HeavyTraits traits) {
        super(applyTraits(properties, traits), gunProfile);
        this.traits = traits;
    }

    private static Properties applyTraits(Properties properties, HeavyTraits traits) {
        if (traits.movementSpeedPenalty() > 0) {
            properties.attributes(ItemAttributeModifiers.builder()
                    .add(Attributes.MOVEMENT_SPEED,
                            new AttributeModifier(MOVEMENT_PENALTY_ID, -traits.movementSpeedPenalty(), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                            EquipmentSlotGroup.MAINHAND)
                    .build());
        }
        return properties;
    }

    public HeavyTraits traits() {
        return traits;
    }

    /**
     * The parent's tooltip, then one line for the thing about this gun its stats don't show: the Gatling's top fire
     * rate, the Crucible's reach, the Congreve's rockets and their cost (the first of these that applies).
     */
    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, context, tooltip, tooltipFlag);
        Component line = signatureLine(itemStack);
        if (line != null) {
            tooltip.add(line.copy().withStyle(ChatFormatting.DARK_AQUA));
        }
    }

    private @Nullable Component signatureLine(ItemStack itemStack) {
        // composed without an entity, so no ComposeShotEvent runs: these are the gun's "ideal" stats (fully spun, unbraced)
        ShotProfile profile = GunplayManager.compose(null, getGun(), itemStack);
        if (profile.value(HeavyShotComponents.SPIN_UP_DELAY) > 0) {
            String maxFireRate = ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(20.0 / Math.max(1, profile.fireDelayTicks()));
            int shots = (int) Math.ceil(profile.value(HeavyShotComponents.SPIN_UP_SHOTS));
            return Component.translatable("artifice_heavy_artillery.tooltip.spin_up", maxFireRate, shots);
        }
        if (profile.peek(HeavyShotComponents.MOLTEN)) {
            double range = MoltenShot.range(profile);
            if (Double.isFinite(range)) {
                return Component.translatable("artifice_heavy_artillery.tooltip.molten", Math.round(range));
            }
        }
        double ammoCost = profile.value(HeavyShotComponents.RESERVE_AMMO_COST);
        if (ammoCost > 1) {
            String bullets = ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(ammoCost);
            boolean explosive = profile.peek(ShotComponents.ON_HIT).all().stream().anyMatch(BlackpowderChargeOnHit.class::isInstance);
            return Component.translatable(explosive ? "artifice_heavy_artillery.tooltip.rockets" : "artifice_heavy_artillery.tooltip.ammo_cost", bullets);
        }
        return null;
    }
}
