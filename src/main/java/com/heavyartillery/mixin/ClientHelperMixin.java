package com.heavyartillery.mixin;

import com.heavyartillery.client.MuzzleTrails;
import io.redspace.irons_artifice.client.ClientHelper;
import io.redspace.irons_artifice.network.packets.ClientboundBulletTrailPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// Cosmetic hooks (require = 0): if another mod reworks a target, only the effect is lost, not the game. Dev runs still
// fail on a miss (mixin.debug.countInjections).
@Mixin(ClientHelper.class)
public abstract class ClientHelperMixin {
    /** Each trail particle's position (locals: from, to, dir, pos): from a held heavy gun's muzzle. */
    @ModifyVariable(method = "handleBulletTrail", at = @At("STORE"), ordinal = 3, require = 0)
    private static Vec3 artifice_heavy_artillery$trailFromMuzzle(Vec3 pos, ClientboundBulletTrailPacket msg) {
        var level = Minecraft.getInstance().level;
        return level == null ? pos : MuzzleTrails.fromHeldMuzzle(level, pos);
    }
}
