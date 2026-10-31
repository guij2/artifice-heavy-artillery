package com.heavyartillery.client;

import com.google.common.base.Suppliers;
import com.heavyartillery.HeavyArtillery;
import com.heavyartillery.registry.HAItems;
import com.heavyartillery.weapon.trait.TripodMount;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.irons_artifice.api.GunBones;
import io.redspace.irons_artifice.client.HeldItemRenderContext;
import io.redspace.irons_artifice.client.gun.SimpleItemGeoModel;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.registry.ItemRegistry;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoObjectRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.util.RenderUtil;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Draws the Portable Tripod on any gun fitted with one, the parent mod's included: folded under the barrel, and
 * unfolded onto the ground while its holder is mounted ({@link TripodMount}).
 * <p>
 * The tripod is one GeckoLib model ({@code portable_tripod.geo.json}), modelled standing open and drawn a part at a
 * time: the {@code saddle} clamped under the gun, the {@code hub} (the tripod head) under it, and three legs
 * ({@link #LEGS}), each with a foot that slides out of it on a telescoping tube. The saddle always moves with the gun.
 * Folded, the hub and legs do too, the legs laid forward along the barrel. Mounted, the hub stays level, turned the way
 * its holder looks, and the legs open as modelled (each leg bone's pivot is its hinge, its rest rotation the way it
 * stands), each finding its own footing every frame (see {@link #footing}): it slides out exactly as far as the ground,
 * however high or low the gun is held, or splays out wider when the ground is closer than the leg pushed fully in. The parts swing
 * from one pose to the other as {@link TripodClient#deployed} goes from 0 to 1. Everything keeps the gun's own scale
 * (times the mount's), so the tripod is sized like the gun in every view.
 * <p>
 * Where it clamps on: a gun model's {@link TripodMount#SOCKET} bone if it has one, else the spot in {@link #MOUNTS},
 * else a spot a little behind its {@code attachment_bayonet} socket.
 */
public final class PortableTripodRenderer {
    /**
     * Where the tripod clamps onto a gun model and how big it is.
     *
     * @param anchor the middle of the gun's underside where the saddle's top goes, in the gun model's Blockbench pixels
     *               (geo.json coordinates), or null to use the model's tripod socket or the fallback
     * @param scale  the tripod's size relative to the model
     * @param foldLift how steeply the folded legs rise forward towards the barrel (rise per pixel forward)
     */
    public record Mount(@Nullable Vec3 anchor, float scale, float foldLift) {
        public Mount(@Nullable Vec3 anchor, float scale) {
            this(anchor, scale, 0.3f);
        }
    }

    /**
     * Where the tripod goes on known guns (models without a tripod socket), and how big it is. On the parent's guns,
     * under the forestock in front of the trigger guard, smaller on pistols. On the Gatling, under the barrels ahead
     * of the ammo box; on the Crucible, under the boiler, its legs folding flat beneath the fuel capsule.
     */
    private static final Map<Item, Mount> MOUNTS = Map.of(
            ItemRegistry.FLINTLOCK_PISTOL.get(), new Mount(new Vec3(0, -0.5, -12.5), 0.6f),
            ItemRegistry.MUSKET.get(), new Mount(new Vec3(0, 0.25, -15), 1),
            ItemRegistry.ARQUEBUS.get(), new Mount(new Vec3(0, 1, -14), 1),
            ItemRegistry.BLUNDERBUSS.get(), new Mount(new Vec3(0, 0.25, -12), 1),
            ItemRegistry.SIX_SHOOTER.get(), new Mount(new Vec3(0, 2.75, -6.5), 0.5f),
            ItemRegistry.BLACKPOWDER_REVOLVER.get(), new Mount(new Vec3(0, 4, -11.5), 0.5f),
            ItemRegistry.CLOCKWORK_RIFLE.get(), new Mount(new Vec3(0, 1.5, -17), 1),
            HAItems.GUNSTEEL_GATLING.get(), new Mount(new Vec3(0, 2.5, -17), 1),
            HAItems.GUNSTEEL_CRUCIBLE.get(), new Mount(new Vec3(0, -4, -3), 1, 0.05f)
    );
    private static final Mount DEFAULT_MOUNT = new Mount(null, 1);
    /** Without a socket or a known mount: this far behind the bayonet socket, in pixels. */
    private static final float FALLBACK_BEHIND_BAYONET = 6;

    /** Folded, the legs point forward along the barrel (tilted up by the mount's fold lift), fanned out a little to each side. */
    private static final float FOLDED_FAN = 0.08f;
    /** A leg's length from hinge to sole with its tube pushed fully in, and the tube's length, in pixels. */
    private static final float LEG_LENGTH = 15.5f;
    private static final float TUBE_LENGTH = 14;
    /** When the ground is too close for a leg even pushed fully in, it splays out further, up to this far from straight down. */
    private static final float MAX_SPLAY = 80 * Mth.DEG_TO_RAD;
    /** Splay angles tried on the way out to {@link #MAX_SPLAY}, then halvings to settle between the last two. */
    private static final int SPLAY_STEPS = 10;
    private static final int SPLAY_REFINE = 5;
    /** How far below a hinge the ground is looked for, in blocks; with none, the legs hang pushed in. */
    private static final double MAX_REACH = 4;

    private static final String SADDLE = "saddle";
    private static final String HUB = "hub";
    /** The legs: modelled hanging from their hinges, opened by their rest rotations as they stand on the ground. */
    private static final String[] LEGS = {"leg_front", "leg_right", "leg_left"};
    /**
     * Each leg's foot, {@code <leg>_lower}, slides out of it on its tube, {@code <leg>_tube}, which stretches to match
     * (its texture is the same all along it, so the stretch doesn't show).
     */
    private static final String LOWER = "_lower";
    private static final String TUBE = "_tube";
    private static final String[] PARTS = {SADDLE, HUB, LEGS[0], LEGS[1], LEGS[2]};

    private static final Supplier<TripodGeoRenderer> RENDERER = Suppliers.memoize(TripodGeoRenderer::new);

    private PortableTripodRenderer() {
    }

    /**
     * What a gun's render needs to draw its tripod: the bone it rides on, where on that bone (from its pivot, in
     * blocks), its mount, and (in its holder's hand) how far it has unfolded and where its holder looks.
     */
    public record Plan(String bone, Vector3f offset, Mount mount, float deployed, float yaw, boolean firstPerson) {
    }

    /**
     * The tripod for a gun about to be drawn, or null if it has none. Called from {@code GunInHandRenderer.preRender}.
     */
    public static @Nullable Plan plan(GunItem gun, BakedGeoModel model, ItemStack stack, @Nullable ItemDisplayContext perspective, float partialTick) {
        if (!TripodMount.hasTripod(stack)) {
            return null;
        }
        Mount mount = MOUNTS.getOrDefault(gun, DEFAULT_MOUNT);
        GeoBone bone;
        Vector3f offset;
        Optional<GeoBone> socket = model.getBone(TripodMount.SOCKET);
        if (socket.isPresent()) {
            bone = socket.get();
            offset = new Vector3f();
        } else {
            Optional<GeoBone> gunBone = model.getBone(GunBones.GUN).or(() -> model.getBone(GunBones.ROOT));
            Vec3 anchor = mount.anchor() != null ? mount.anchor() : fallbackAnchor(model);
            if (gunBone.isEmpty() || anchor == null) {
                return null;
            }
            bone = gunBone.get();
            // baked pivots have X flipped from the geo.json
            offset = new Vector3f((float) -anchor.x - bone.getPivotX(), (float) anchor.y - bone.getPivotY(), (float) anchor.z - bone.getPivotZ()).div(16);
        }
        float deployed = 0;
        float yaw = 0;
        LivingEntity owner = HeldItemRenderContext.holder();
        if (isHeld(perspective) && owner != null && owner.getMainHandItem() == stack) {
            deployed = smoothstep(TripodClient.deployed(owner.getId(), partialTick));
            yaw = owner.getViewYRot(partialTick);
        }
        return new Plan(bone.getName(), offset, mount, deployed, yaw, isFirstPerson(perspective));
    }

    /**
     * Draws the tripod if {@code bone} is the one it rides on. Called from {@code GunInHandRenderer.handleAttachmentRendering}
     * for each bone, with the pose stack on the bone's parent.
     */
    public static void renderAtBone(Plan plan, PoseStack poseStack, GeoBone bone, MultiBufferSource bufferSource, float partialTick, int packedLight) {
        if (!plan.bone().equals(bone.getName())) {
            return;
        }
        poseStack.pushPose();
        RenderUtil.prepMatrixForBone(poseStack, bone);
        RenderUtil.translateToPivotPoint(poseStack, bone);
        render(poseStack, bufferSource, partialTick, packedLight, plan.offset(), plan.mount(), plan.deployed(), plan.yaw(), plan.firstPerson());
        poseStack.popPose();
    }

    /** A little behind the bayonet socket (which sits under the muzzle), in geo.json pixels. */
    private static @Nullable Vec3 fallbackAnchor(BakedGeoModel model) {
        return model.getBone(GunBones.SOCKET_BAYONET)
                .map(bayonet -> new Vec3(-bayonet.getPivotX(), bayonet.getPivotY(), bayonet.getPivotZ() + FALLBACK_BEHIND_BAYONET))
                .orElse(null);
    }

    /**
     * Draws the tripod. The pose stack is at the mount bone's pivot, in the space the gun is drawn in: world-aligned and
     * camera-relative in third person, the camera's own space in first person.
     */
    private static void render(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight, Vector3f offset,
                               Mount mount, float deployed, float yaw, boolean firstPerson) {
        Matrix4f saddle = new Matrix4f(poseStack.last().pose()).translate(offset).scale(mount.scale());
        float determinant = saddle.determinant3x3();
        boolean mirrored = determinant < 0;  // left-handed
        float scale = (float) Math.cbrt(Math.abs(determinant));
        Matrix3f gunRotation = new Matrix3f(saddle).scale(1 / scale);
        if (mirrored) {
            gunRotation.scale(-1, 1, 1);
        }
        Quaternionf folded = gunRotation.getNormalizedRotation(new Quaternionf());

        // world <-> render space
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Quaternionf toWorld = firstPerson ? new Quaternionf(camera.rotation()) : new Quaternionf();
        Quaternionf toRender = new Quaternionf(toWorld).conjugate();
        // the tripod's own frame when standing: level, facing where its holder looks (model forward is -Z)
        Quaternionf standing = new Quaternionf(toRender).rotateY(Mth.PI - yaw * Mth.DEG_TO_RAD);

        BakedGeoModel tripod = RENDERER.get().model();
        GeoBone hubBone = tripod.getBone(HUB).orElse(null);
        if (hubBone == null) {
            return;
        }
        float side = mirrored ? -1 : 1;
        Draw draw = new Draw(poseStack, bufferSource, partialTick, packedLight);
        drawPart(draw, SADDLE, 0, saddle, null);

        Vector3f hubPivot = pivot(hubBone);
        Matrix4f hub = pose(saddle.transformPosition(hubPivot.div(16, new Vector3f())), new Quaternionf(folded).slerp(standing, deployed), scale, mirrored);
        drawPart(draw, HUB, 0, hub, hubBone);

        for (String name : LEGS) {
            GeoBone legBone = tripod.getBone(name).orElse(null);
            if (legBone == null) {
                continue;
            }
            Vector3f hinge = hub.transformPosition(pivot(legBone).sub(hubPivot).div(16));
            // open: as modelled, in the hub's standing frame; folded: forward along the barrel, fanned out to the leg's side
            Vector3f open = restRotation(legBone).transform(new Vector3f(0, -1, 0)).mul(side, 1, 1).rotate(standing);
            Vector3f foldedDirection = new Vector3f(Math.signum(legBone.getPivotX()) * FOLDED_FAN, mount.foldLift(), -1)
                    .mul(side, 1, 1).normalize().rotate(folded);
            Footing footing = deployed > 0 ? footing(camera.getPosition(), toWorld, hinge, open, scale) : new Footing(open, LEG_LENGTH);
            Vector3f standingDirection = footing.direction();
            Quaternionf legFolded = new Quaternionf().rotationTo(0, -1, 0, foldedDirection.x, foldedDirection.y, foldedDirection.z);
            Quaternionf legOpen = new Quaternionf().rotationTo(0, -1, 0, standingDirection.x, standingDirection.y, standingDirection.z);
            float extension = deployed * (footing.length() - LEG_LENGTH);
            Matrix4f leg = pose(hinge, legFolded.slerp(legOpen, deployed), scale, mirrored);
            drawPart(draw, name, extension, leg, legBone);
        }
    }

    /** A bone's pivot in the baked model, in pixels. */
    private static Vector3f pivot(GeoBone bone) {
        return new Vector3f(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
    }

    /** A bone's rest rotation, as GeckoLib applies it (about Z, then Y, then X). */
    private static Quaternionf restRotation(GeoBone bone) {
        var rest = bone.getInitialSnapshot();
        return new Quaternionf().rotationZ(rest.getRotZ()).rotateY(rest.getRotY()).rotateX(rest.getRotX());
    }

    /**
     * How a standing leg meets the ground.
     *
     * @param direction its direction, in render space
     * @param length    from hinge to sole, in pixels (at least {@link #LEG_LENGTH})
     */
    private record Footing(Vector3f direction, float length) {
    }

    /**
     * Where a standing leg plants its foot. It keeps the modelled splay if, along it, the ground is at least as far as
     * the leg pushed fully in, and slides out to meet it. Otherwise (the gun is held low, or the leg would land on a
     * step) it splays out further, to the narrowest angle where it finds ground that far, up to {@link #MAX_SPLAY}.
     * Where no angle does, it takes the one that clips least; with no ground in reach at all, it hangs pushed in.
     *
     * @param hinge the leg's hinge, in render space
     * @param open  the modelled direction of the leg, in render space
     * @param scale render units per model block
     */
    private static Footing footing(Vec3 cameraPos, Quaternionf toWorld, Vector3f hinge, Vector3f open, float scale) {
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return new Footing(open, LEG_LENGTH);
        }
        Vector3f hingeWorld = toWorld.transform(hinge, new Vector3f());
        Vec3 start = cameraPos.add(hingeWorld.x, hingeWorld.y, hingeWorld.z);
        Vector3f openWorld = toWorld.transform(open, new Vector3f()).normalize();
        Vec3 outward = new Vec3(openWorld.x, 0, openWorld.z);
        outward = outward.lengthSqr() > 1e-6 ? outward.normalize() : new Vec3(0, 0, 1);
        double shortest = LEG_LENGTH / 16 * scale;
        double preferred = Math.acos(Mth.clamp(-openWorld.y, -1, 1));

        double splay = -1;
        double length = -1;
        double previous = preferred;
        double bestSplay = preferred;
        double bestLength = -1;
        for (int i = 0; i <= SPLAY_STEPS; i++) {
            double tried = preferred + (MAX_SPLAY - preferred) * i / SPLAY_STEPS;
            double ground = groundDistance(level, start, legDirection(outward, tried));
            if (ground >= shortest) {
                splay = tried;
                length = ground;
                break;
            }
            if (ground > bestLength) {
                bestSplay = tried;
                bestLength = ground;
            }
            previous = tried;
        }
        if (splay < 0) {
            if (bestLength < 0) {
                return new Footing(open, LEG_LENGTH);
            }
            splay = bestSplay;
            length = bestLength;
        } else if (splay > preferred) {
            // narrow it down between the last angle that fell short and the first that reached, so it opens smoothly
            double low = previous;
            for (int i = 0; i < SPLAY_REFINE; i++) {
                double mid = (low + splay) / 2;
                double ground = groundDistance(level, start, legDirection(outward, mid));
                if (ground >= shortest) {
                    splay = mid;
                    length = ground;
                } else {
                    low = mid;
                }
            }
        }
        Vec3 direction = legDirection(outward, splay);
        Vector3f directionRender = toWorld.transformInverse(new Vector3f((float) direction.x, (float) direction.y, (float) direction.z));
        return new Footing(directionRender, Math.max(LEG_LENGTH, (float) (length * 16 / scale)));
    }

    /** A leg's direction in the world: {@code splay} radians out from straight down, towards {@code outward} (level). */
    private static Vec3 legDirection(Vec3 outward, double splay) {
        return outward.scale(Math.sin(splay)).add(0, -Math.cos(splay), 0);
    }

    /** How far from {@code start} along {@code direction} (a unit vector) the ground is, in blocks, or -1 beyond {@link #MAX_REACH}. */
    private static double groundDistance(Level level, Vec3 start, Vec3 direction) {
        BlockHitResult hit = level.clip(new ClipContext(start, start.add(direction.scale(MAX_REACH)), ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, CollisionContext.empty()));
        return hit.getType() == HitResult.Type.MISS ? -1 : hit.getLocation().distanceTo(start);
    }

    /** A part's pose: at {@code at}, turned by {@code rotation}, at {@code scale}, mirrored like the gun. */
    private static Matrix4f pose(Vector3f at, Quaternionf rotation, float scale, boolean mirrored) {
        Matrix4f pose = new Matrix4f().translation(at).rotate(rotation).scale(scale);
        return mirrored ? pose.scale(-1, 1, 1) : pose;
    }

    /**
     * Draws one part of the tripod model so that its bone's pivot lands at {@code pose}'s origin, along {@code pose}'s
     * axes with the bone's rest rotation undone (a leg hangs along {@code pose}'s -Y). No bone: the model's origin.
     */
    private static void drawPart(Draw draw, String part, float extension, Matrix4f pose, @Nullable GeoBone bone) {
        PoseStack poseStack = draw.poseStack();
        poseStack.pushPose();
        PoseStack.Pose last = poseStack.last();
        last.pose().set(pose);
        if (bone != null) {
            last.pose().rotate(restRotation(bone).conjugate()).translate(pivot(bone).div(-16));
        }
        // rotation (and mirroring) only: the scale is uniform
        last.normal().set(pose).scale(1 / (float) Math.cbrt(Math.abs(pose.determinant3x3())));
        RENDERER.get().draw(draw, part, extension);
        poseStack.popPose();
    }

    /** Where the parts are drawn. */
    private record Draw(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, int packedLight) {
    }

    private static boolean isHeld(@Nullable ItemDisplayContext perspective) {
        return perspective == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || perspective == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || perspective == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND || perspective == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
    }

    private static boolean isFirstPerson(@Nullable ItemDisplayContext perspective) {
        return perspective == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || perspective == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
    }

    private static float smoothstep(float t) {
        return t * t * (3 - 2 * t);
    }

    /**
     * Draws the tripod model one part at a time (the other parts' bones are hidden), like the parent's
     * {@code AttachmentGeoRenderer}.
     */
    private static final class TripodGeoRenderer extends GeoObjectRenderer<GeoAnimatable> {
        private final GeoAnimatable animatable = new StaticAttachment();
        private String part = SADDLE;
        private float extension;

        TripodGeoRenderer() {
            super(new SimpleItemGeoModel<>(HeavyArtillery.MODID, "portable_tripod", "model/portable_tripod", "empty"));
        }

        BakedGeoModel model() {
            return getGeoModel().getBakedModel(getGeoModel().getModelResource(this.animatable));
        }

        void draw(Draw draw, String part, float extension) {
            this.part = part;
            this.extension = extension;
            RenderType renderType = getRenderType(this.animatable, getTextureLocation(this.animatable), draw.bufferSource(), draw.partialTick());
            render(draw.poseStack(), this.animatable, draw.bufferSource(), renderType, draw.bufferSource().getBuffer(renderType),
                    draw.packedLight(), draw.partialTick());
        }

        /** No placement of its own: the pose stack already holds the part's pose. */
        @Override
        public void preRender(@NotNull PoseStack poseStack, @NotNull GeoAnimatable animatable, @NotNull BakedGeoModel model, @Nullable MultiBufferSource bufferSource,
                              @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
            this.objectRenderTranslations = new Matrix4f(poseStack.last().pose());
        }

        /** Shows only the part being drawn, and slides its foot out (after the animation pass, which would reset them). */
        @Override
        public void renderRecursively(PoseStack poseStack, GeoAnimatable animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource,
                                      VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
            String name = bone.getName();
            for (String other : PARTS) {
                if (other.equals(name)) {
                    boolean shown = name.equals(this.part);
                    bone.setHidden(!shown);
                    bone.setChildrenHidden(!shown);
                }
            }
            if (name.equals(this.part + LOWER)) {
                bone.setPosY(-this.extension);
            } else if (name.equals(this.part + TUBE)) {
                bone.setScaleY(1 + this.extension / TUBE_LENGTH);
            }
            super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        }

        @Override
        public long getInstanceId(GeoAnimatable animatable) {
            return 0L;
        }
    }

    private static final class StaticAttachment implements GeoAnimatable {
        private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        }

        @Override
        public AnimatableInstanceCache getAnimatableInstanceCache() {
            return this.cache;
        }

        @Override
        public double getTick(Object object) {
            return RenderUtil.getCurrentTick();
        }
    }
}
