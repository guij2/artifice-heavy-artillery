package com.heavyartillery.registry;

import com.heavyartillery.HeavyArtillery;
import com.heavyartillery.weapon.trait.FiringStreak;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class HAAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, HeavyArtillery.MODID);

    /**
     * Transient, unsynced. Each side tracks it independently off {@code GunAboutToShootEvent}, which fires on both
     * the client and the server, so both agree on fire delays without extra packets.
     */
    public static final Supplier<AttachmentType<FiringStreak>> FIRING_STREAK =
            ATTACHMENT_TYPES.register("firing_streak",
                    () -> AttachmentType.builder(() -> FiringStreak.NONE).build());

    /**
     * Game time of the entity's last shot, for {@code trait.PowderRam}. Transient and unsynced, tracked on both sides
     * like {@link #FIRING_STREAK}.
     */
    public static final Supplier<AttachmentType<Long>> LAST_SHOT =
            ATTACHMENT_TYPES.register("last_shot",
                    () -> AttachmentType.builder(() -> Long.MIN_VALUE).build());

    /**
     * A shot's {@code HeavyShotComponents.SHOT_MODEL} (an item model id), set on the parent's bullets that have one and
     * synced to the clients that see them ({@code BulletMixin}, {@code client.ShotModelRenderer}).
     */
    public static final Supplier<AttachmentType<String>> SHOT_MODEL =
            ATTACHMENT_TYPES.register("shot_model",
                    () -> AttachmentType.builder(() -> "").sync(ByteBufCodecs.STRING_UTF8).build());

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
