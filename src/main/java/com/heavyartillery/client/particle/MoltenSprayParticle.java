package com.heavyartillery.client.particle;

import com.heavyartillery.HeavyArtillery;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;
import org.jspecify.annotations.NonNull;

/**
 * A billow of molten spray. It keeps the velocity it is given (a shot's trail moves it along the shot), slows down,
 * swells and rises while its sprite cools from white-hot through orange and red into smoke. It glows until it cools.
 * <p>
 * Swollen, cooling billows that fill a big part of the view fade out, so a flamethrower's own spray doesn't blind its
 * user, while fresh ones stay solid. The user's spray starts at the gun rather than their eye: in first person at
 * the gun on screen here, in third person at the muzzle of the gun in their hands ({@link
 * com.heavyartillery.client.MuzzleTrails}, as for every heavy gun's trail).
 */
public class MoltenSprayParticle extends SingleQuadParticle {
    /**
     * Translucent particles, without depth writes. Particles are drawn oldest first and unsorted, so with depth writes a
     * cooling billow near the camera would hide the fresh spray behind it (showing the background through its faded
     * quad); without them, newer billows are always drawn over older ones. Registered in {@code HAClient}.
     */
    public static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
            .withLocation(HeavyArtillery.id("pipeline/molten_spray"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
            .build();
    private static final Layer LAYER = new Layer(true, TextureAtlas.LOCATION_PARTICLES, PIPELINE);
    /** Fraction of its life a billow glows at full brightness before fading to world light (the sprite turns to smoke). */
    private static final float GLOW_END = 0.55f;
    /** Billows closer to the camera than this (inside the held gun) are invisible, fading in over the next {@link #CAMERA_FADE} blocks. */
    private static final double CAMERA_CLEARANCE = 0.4;
    private static final double CAMERA_FADE = 0.3;
    /**
     * Apparent size (quad size / distance) from which a billow starts to fade, and the size at which it is faintest
     * ({@link #MIN_VIEW_ALPHA}). Fresh billows are spared this until {@link #FRESH_END} of their life, fully by
     * {@link #COOL_START}, as they are small and the hot core of the spray.
     */
    private static final float VIEW_FADE_FROM = 0.15f;
    private static final float VIEW_FADE_TO = 0.4f;
    private static final float MIN_VIEW_ALPHA = 0.25f;
    private static final float FRESH_END = 0.25f;
    private static final float COOL_START = 0.5f;
    /** Cooling billows give off wisps of smoke, though not right in front of the camera. */
    private static final float SMOKE_CHANCE = 0.08f;
    private static final double SMOKE_CLEARANCE = 3;
    /**
     * Shots leave from the eye. Seen in first person, billows this close ahead are pulled toward the held gun's nozzle
     * (down and to the side of the main hand), less the farther out they are, so the spray comes out of the gun.
     */
    private static final double NOZZLE_BLEND = 4;
    private static final double NOZZLE_SIDE = 0.35;
    private static final double NOZZLE_DOWN = 0.3;

    private final SpriteSet sprites;
    private final float startSize;
    private final float endSize;
    private final float spin;

    protected MoltenSprayParticle(ClientLevel level, double x, double y, double z, double xa, double ya, double za, SpriteSet sprites) {
        super(level, x, y, z, sprites.first());
        this.sprites = sprites;
        this.xd = xa + (random.nextFloat() - 0.5f) * 0.06f;
        this.yd = ya + (random.nextFloat() - 0.5f) * 0.06f;
        this.zd = za + (random.nextFloat() - 0.5f) * 0.06f;
        this.friction = 0.88f;
        this.gravity = -0.05f;
        this.lifetime = 12 + random.nextInt(9);
        this.startSize = 0.12f + random.nextFloat() * 0.06f;
        this.endSize = 0.32f + random.nextFloat() * 0.14f;
        this.quadSize = startSize;
        this.roll = this.oRoll = random.nextFloat() * Mth.TWO_PI;
        this.spin = (random.nextFloat() - 0.5f) * 0.25f;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.oRoll = this.roll;
        this.roll += spin;
        this.setSpriteFromAge(sprites);
        if (age > lifetime * 0.6f && random.nextFloat() < SMOKE_CHANCE
                && Minecraft.getInstance().gameRenderer.getMainCamera().position().distanceToSqr(x, y, z) > SMOKE_CLEARANCE * SMOKE_CLEARANCE) {
            level.addParticle(ParticleTypes.SMOKE, x, y, z, xd * 0.5, 0.02, zd * 0.5);
        }
    }

    private float life(float partialTick) {
        return Mth.clamp((age + partialTick) / lifetime, 0, 1);
    }

    @Override
    public float getQuadSize(float a) {
        float t = life(a);
        return Mth.lerp(1 - (1 - t) * (1 - t), startSize, endSize);
    }

    @Override
    public void extract(@NonNull QuadParticleRenderState state, @NonNull Camera camera, float partialTickTime) {
        float t = life(partialTickTime);
        double distance = Math.sqrt(camera.position().distanceToSqr(Mth.lerp(partialTickTime, xo, x), Mth.lerp(partialTickTime, yo, y), Mth.lerp(partialTickTime, zo, z)));
        float clearance = (float) Mth.clamp((distance - CAMERA_CLEARANCE) / CAMERA_FADE, 0, 1);
        float apparentSize = (float) (getQuadSize(partialTickTime) / Math.max(distance, 0.01));
        float viewFade = Mth.clamp(1 - (apparentSize - VIEW_FADE_FROM) / (VIEW_FADE_TO - VIEW_FADE_FROM) * (1 - MIN_VIEW_ALPHA), MIN_VIEW_ALPHA, 1);
        float cooled = Mth.clamp((t - FRESH_END) / (COOL_START - FRESH_END), 0, 1);
        float coolFade = t < 0.7f ? 1 : 1 - (t - 0.7f) / 0.3f;
        this.alpha = clearance * Mth.lerp(cooled, 1, viewFade) * coolFade;
        if (alpha <= 0.02f) {
            return;
        }
        super.extract(state, camera, partialTickTime);
    }

    @Override
    protected int getLightCoords(float a) {
        float t = life(a);
        float glow = t < GLOW_END ? 1 : 1 - Mth.clamp((t - GLOW_END) / 0.25f, 0, 1);
        return LightCoordsUtil.addSmoothBlockEmission(super.getLightCoords(a), glow);
    }

    @Override
    protected @NonNull Layer getLayer() {
        return LAYER;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(@NonNull SimpleParticleType options, @NonNull ClientLevel level, double x, double y, double z,
                                       double xa, double ya, double za, @NonNull RandomSource random) {
            Vec3 at = fromNozzle(new Vec3(x, y, z));
            return new MoltenSprayParticle(level, at.x, at.y, at.z, xa, ya, za, sprites);
        }

        private static Vec3 fromNozzle(Vec3 at) {
            Minecraft mc = Minecraft.getInstance();
            Camera camera = mc.gameRenderer.getMainCamera();
            if (mc.player == null || camera.entity() != mc.player || camera.isDetached()) {
                return at;
            }
            Vec3 offset = at.subtract(camera.position());
            Vector3fc forward = camera.forwardVector();
            double ahead = offset.dot(new Vec3(forward.x(), forward.y(), forward.z()));
            // only the user's own spray: just ahead of the eye, close to the line of sight
            if (ahead <= 0 || ahead >= NOZZLE_BLEND || offset.length() > ahead * 1.3 + 0.3) {
                return at;
            }
            double blend = 1 - ahead / NOZZLE_BLEND;
            double side = mc.player.getMainArm() == HumanoidArm.RIGHT ? -1 : 1;
            Vector3fc left = camera.leftVector();
            Vector3fc up = camera.upVector();
            return at.add(left.x() * side * NOZZLE_SIDE * blend - up.x() * NOZZLE_DOWN * blend,
                    left.y() * side * NOZZLE_SIDE * blend - up.y() * NOZZLE_DOWN * blend,
                    left.z() * side * NOZZLE_SIDE * blend - up.z() * NOZZLE_DOWN * blend);
        }
    }
}
