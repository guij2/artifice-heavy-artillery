package com.heavyartillery.mixin;

import com.heavyartillery.weapon.HeavyShotComponents;
import com.heavyartillery.weapon.ShotModelHolder;
import com.heavyartillery.weapon.trait.MoltenShot;
import com.heavyartillery.weapon.trait.Rifling;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.redspace.irons_artifice.entity.Bullet;
import io.redspace.irons_artifice.gun.ShotProfile;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Molten shots splash and sizzle where they land instead of playing the parent's break sound and stone chips
 * (see {@link MoltenShot#blockImpact}), and never damage blocks (they burn them instead). On-hit effects are untouched.
 * <p>
 * Also gives a shot its {@link HeavyShotComponents#SHOT_MODEL} (in synced entity data, so clients see it), for
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

    /** The shot's model id, or empty. Defined after the parent's own entries, the same on both sides. */
    @Unique
    private static final EntityDataAccessor<String> artifice_heavy_artillery$SHOT_MODEL = SynchedEntityData.defineId(Bullet.class, EntityDataSerializers.STRING);

    @Unique
    private @Nullable Vec3 artifice_heavy_artillery$origin;
    @Unique
    private @Nullable Vec3 artifice_heavy_artillery$impact;

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void artifice_heavy_artillery$defineShotModel(CallbackInfo ci) {
        this.entityData.define(artifice_heavy_artillery$SHOT_MODEL, "");
    }

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

    @ModifyReturnValue(method = "resolveDamage", at = @At("RETURN"), remap = false)
    private float artifice_heavy_artillery$rifling(float damage) {
        Vec3 impact = this.artifice_heavy_artillery$impact != null ? this.artifice_heavy_artillery$impact : this.position();
        return Rifling.apply((Bullet) (Object) this, damage, this.artifice_heavy_artillery$origin, impact);
    }

    @Inject(method = "playBlockHitEffects", at = @At("HEAD"), cancellable = true, remap = false)
    private void artifice_heavy_artillery$moltenImpact(BlockHitResult hitResult, CallbackInfo ci) {
        if (MoltenShot.blockImpact((Bullet) (Object) this, hitResult)) {
            ci.cancel();
        }
    }

    @Inject(method = "attemptApplyBlockDamage", at = @At("HEAD"), cancellable = true, remap = false)
    private void artifice_heavy_artillery$moltenKeepsBlocks(BlockHitResult hitResult, CallbackInfoReturnable<Boolean> cir) {
        if (MoltenShot.keepsBlocks((Bullet) (Object) this)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "applyProfile", at = @At("TAIL"), remap = false)
    private void artifice_heavy_artillery$syncShotModel(ShotProfile profile, CallbackInfo ci) {
        profile.peek(HeavyShotComponents.SHOT_MODEL).ifPresent(model -> this.entityData.set(artifice_heavy_artillery$SHOT_MODEL, model.toString()));
    }

    @Override
    public @Nullable ResourceLocation artifice_heavy_artillery$shotModel() {
        String model = this.entityData.get(artifice_heavy_artillery$SHOT_MODEL);
        return model.isEmpty() ? null : ResourceLocation.tryParse(model);
    }
}
