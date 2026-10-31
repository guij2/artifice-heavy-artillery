package com.heavyartillery.mixin;

import com.heavyartillery.registry.HAAttachments;
import com.heavyartillery.weapon.HeavyShotComponents;
import com.heavyartillery.weapon.ShotModelHolder;
import com.heavyartillery.weapon.trait.MoltenShot;
import com.heavyartillery.weapon.trait.Rifling;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.gun.ShotProfile;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Molten shots splash and sizzle where they land instead of playing the parent's break sound and stone chips
 * (see {@link MoltenShot#blockImpact}). Block damage and on-hit effects are untouched.
 * <p>
 * Also attaches a shot's {@link HeavyShotComponents#SHOT_MODEL} ({@link HAAttachments#SHOT_MODEL}, synced to clients), for
 * {@code client.ShotModelRenderer}.
 * <p>
 * And remembers where the shot was fired from and where it is hitting, so {@link Rifling} can scale its damage by
 * the distance flown.
 */
@Mixin(Bullet.class)
public abstract class BulletMixin extends Projectile implements ShotModelHolder {
    protected BulletMixin(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
    }

    @Unique
    private @Nullable Vec3 artifice_heavy_artillery$origin;
    @Unique
    private @Nullable Vec3 artifice_heavy_artillery$impact;

    /** The first tick starts where the gun put the shot */
    @Inject(method = "tick", at = @At("HEAD"))
    private void artifice_heavy_artillery$recordOrigin(CallbackInfo ci) {
        if (this.artifice_heavy_artillery$origin == null) {
            this.artifice_heavy_artillery$origin = this.position();
        }
    }

    /** The hit point, as the shot's own position is still where this tick's move began */
    @Inject(method = "onHit", at = @At("HEAD"))
    private void artifice_heavy_artillery$recordImpact(HitResult hitResult, CallbackInfo ci) {
        this.artifice_heavy_artillery$impact = hitResult.getLocation();
    }

    @ModifyReturnValue(method = "resolveDamage", at = @At("RETURN"))
    private float artifice_heavy_artillery$rifling(float damage) {
        Vec3 impact = this.artifice_heavy_artillery$impact != null ? this.artifice_heavy_artillery$impact : this.position();
        return Rifling.apply((Bullet) (Object) this, damage, this.artifice_heavy_artillery$origin, impact);
    }

    @Inject(method = "playBlockHitEffects", at = @At("HEAD"), cancellable = true)
    private void artifice_heavy_artillery$moltenImpact(BlockHitResult hitResult, CallbackInfo ci) {
        if (MoltenShot.blockImpact((Bullet) (Object) this, hitResult)) {
            ci.cancel();
        }
    }

    @Inject(method = "applyProfile", at = @At("TAIL"))
    private void artifice_heavy_artillery$syncShotModel(ShotProfile profile, CallbackInfo ci) {
        profile.peek(HeavyShotComponents.SHOT_MODEL).ifPresent(model -> this.setData(HAAttachments.SHOT_MODEL, model.toString()));
    }

    @Override
    public @Nullable Identifier artifice_heavy_artillery$shotModel() {
        String model = this.getExistingDataOrNull(HAAttachments.SHOT_MODEL);
        return model == null || model.isEmpty() ? null : Identifier.tryParse(model);
    }
}
