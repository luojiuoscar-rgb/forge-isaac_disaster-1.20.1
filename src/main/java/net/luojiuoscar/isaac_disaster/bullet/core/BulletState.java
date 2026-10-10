package net.luojiuoscar.isaac_disaster.bullet.core;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.bullet.tracking.TrackingProfile;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.util.DamagedEntities;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.BulletColor;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitSequence;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitTriggerCounts;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitTriggerType;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryMotion;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryRuntime;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectorySpec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.jetbrains.annotations.Nullable;

/**
 * Lightweight transient projectile state. It deliberately contains no Minecraft Entity lifecycle.
 */
public final class BulletState implements IBulletObject {
    /**
     * Temporary safety net for a rare stalled-projectile failure. Remove or revise this guard once
     * the underlying source of invalid low-speed states is identified.
     */
    public static final double MIN_VALID_SPEED = BulletMotionState.MIN_VALID_SPEED;

    private final BulletMotionState motionState;
    private final BulletTrajectoryState trajectoryState;
    private float damage;
    private final BulletProfile profile;
    private final boolean noGravity;
    private boolean spectral;
    private boolean piercing;
    private final BulletTrackingState trackingState;
    private int slot = -1;
    private int generation;
    private final BulletAttackIdentity identity;
    private SplitSequence splitSequence = new SplitSequence();
    private final SplitTriggerCounts splitTriggerCounts = new SplitTriggerCounts();
    private final BulletCollisionState collisionState;
    private final CompositeTrigger triggers = new CompositeTrigger();

    private BulletState(Builder b) {
        this.motionState =
            new BulletMotionState(
                b.position,
                b.velocity,
                b.acceleration,
                b.baseSpeed,
                b.lifetime,
                b.range);
        this.damage = b.damage;
        this.profile = new BulletProfile(
            b.renderScale,
            b.collisionWidth,
            b.collisionHeight,
            b.color,
            b.alpha,
            b.colorId,
            b.visualIds);
        this.noGravity = b.noGravity;
        AttackContext attackContext =
            b.attackContext == null ? null : b.attackContext.copyConfiguration();
        List<TrajectorySpec> specs =
            b.trajectorySpecs.isEmpty()
                ? attackContext == null ? List.of() : attackContext.getTrajectorySpecs()
                : b.trajectorySpecs;
        TrajectoryRuntime trajectoryRuntime =
            b.trajectorySnapshot != null
                ? b.trajectorySnapshot.restore()
                : TrajectoryRuntime.forSpawn(
                    motionState.position(),
                    motionState.velocity(),
                    specs,
                    b.shooter,
                    attackContext == null ? null : attackContext.getInheritedTrajectorySnapshot());
        this.trajectoryState = new BulletTrajectoryState(trajectoryRuntime);
        this.collisionState = new BulletCollisionState(
            attackContext == null ? b.hitBlockPositions : attackContext.getHitBlockPositions());
        this.spectral = b.spectral;
        this.piercing = b.piercing;
        this.trackingState = new BulletTrackingState(
            b.homing,
            b.controllable,
            b.rememberHitTargets,
            b.steeringMode,
            b.homingRange,
            b.homingSteer,
            b.maxSteeringAcceleration,
            b.maxSpeedChange,
            b.controlRange,
            b.controlSteer);
        ResourceLocation typeId =
            attackContext == null
                ? b.typeId
                : java.util.Objects.requireNonNull(attackContext.getTypeId(), "prepared attack type");
        ResourceLocation rootTypeId =
            attackContext == null
                ? b.rootTypeId
                : java.util.Objects.requireNonNull(attackContext.getRootTypeId(), "prepared root type");
        this.identity = new BulletAttackIdentity(
            b.owner,
            b.shooter,
            b.ownerUuid != null ? b.ownerUuid : b.owner == null ? null : b.owner.getUUID(),
            attackContext,
            typeId,
            rootTypeId);
        if (b.splitSequence != null) this.splitSequence = b.splitSequence.copy();
        this.triggers.addAll(b.triggers);
    }

    /** Starts building a standalone state for tests or non-entity runtime adapters. */
    public static Builder builder() {
        return new Builder();
    }

    /** Defers trajectory initialization until the final position and velocity are known. */
    public static Builder from(AttackContext context) {
        BulletColor color = resolveColor(context.getColorRl());
        return builder()
            .position(context.getPos())
            .velocity(context.getMainAxis().scale(context.getBulletSpeed()))
            .baseSpeed(context.getBulletSpeed())
            .range(context.getBulletRange())
            .damage(context.getDamage())
            .owner(context.getOwner())
            .shooter(context.getShooter())
            .attackContext(context)
            .splitSequence(context.copySplitSequence())
            .triggers(context.copyTrigger())
            .colorId(context.getColorRl())
            .color(color.color())
            .alpha(color.alpha())
            .visualIds(context.getVisualIds());
    }

    /**
     * Resolves the one-time RGB/alpha snapshot so the client never needs the color registry per
     * bullet.
     */
    private static BulletColor resolveColor(ResourceLocation colorId) {
        IForgeRegistry<BulletColor> registry =
            RegistryManager.ACTIVE.getRegistry(ModBulletColors.BULLET_COLOR_KEY);
        BulletColor color = registry == null || colorId == null ? null : registry.getValue(colorId);
        return color == null ? ModBulletColors.BASE.get() : color;
    }

    /** Advances one authoritative tick using only base kinematics. Returns whether still alive. */
    public boolean tickPhysics() {
        if (!motionState.isAlive()) return false;
        trajectoryState.suspendForPhysics();
        tickHitCooldown();
        return motionState.tickPhysics(rangeLimited());
    }

    /** Advances one tick to an evaluator-selected absolute position. */
    public boolean advanceTrajectory(
        Vec3 nextPosition,
        Vec3 nextVelocity,
        Vec3 nextAcceleration,
        double progressRate,
        double deltaTicks) {
        if (!motionState.isAlive()) return false;
        tickHitCooldown();
        if (!motionState.advanceTrajectory(
            nextPosition, nextVelocity, nextAcceleration, deltaTicks, rangeLimited())) return false;
        trajectoryState.advanceRuntime(progressRate, deltaTicks);
        trajectoryState.captureMotion(
            new TrajectoryMotion(
                motionState.position(),
                motionState.velocity(),
                motionState.acceleration(),
                trajectoryState.progressRate(),
                TrajectoryMotion.CompositionMode.ABSOLUTE));
        return true;
    }

    /** Reconciles the distance counter when swept collision truncates this tick's endpoint. */
    public void truncateMovementDistance(double fullDistance, double acceptedDistance) {
        motionState.setTraveled(
            trajectoryState.truncateMovementDistance(
                motionState.traveled(), fullDistance, acceptedDistance));
    }

    public void retainTrajectoryFraction(double fraction) {
        motionState.setTraveled(
            trajectoryState.retainTrajectoryFraction(motionState.traveled(), fraction));
    }

    public Vec3 position() {
        return motionState.position();
    }

    public Vec3 previousPosition() {
        return motionState.previousPosition();
    }

    public Vec3 velocity() {
        return motionState.velocity();
    }

    @Override
    public Vec3 getAcceleration() {
        return motionState.acceleration();
    }

    public Vec3 acceleration() {
        return motionState.acceleration();
    }

    /** Returns the creation-time cruise speed used when homing is inactive. */
    public double baseSpeed() {
        return motionState.baseSpeed();
    }

    public int age() {
        return motionState.age();
    }

    public int lifetime() {
        return motionState.lifetime();
    }

    public double traveled() {
        return motionState.traveled();
    }

    /** Returns the trajectory distance observed immediately before the latest evaluation. */
    public double lastTrajectoryInputDistance() {
        return trajectoryState.lastInputDistance();
    }

    /** Returns how many trajectory evaluations have occurred for this state. */
    public int trajectoryEvaluationCount() {
        return trajectoryState.evaluationCount();
    }

    /** Records evaluator boundary data for runtime diagnostics. */
    public void markTrajectoryEvaluation(double inputDistance) {
        trajectoryState.markEvaluation(inputDistance);
    }

    public boolean advanceTrajectory(TrajectoryMotion motion, double deltaTicks) {
        double before = motionState.traveled();
        boolean advanced =
            advanceTrajectory(
                motion.desiredPosition(),
                motion.desiredVelocity(),
                motion.desiredAcceleration(),
                motion.progressRate(),
                deltaTicks);
        if (advanced) {
            motionState.setTraveled(before + motion.chargedDistance(motionState.traveled() - before));
            trajectoryState.captureMotion(motion);
        }
        return advanced;
    }

    private boolean rangeLimited() {
        return !trajectoryState.runtime().specs().isEmpty();
    }

    /** Restores a server snapshot without applying client-local catch-up simulation. */
    public void restoreSnapshot(Vec3 previous, int snapshotAge, double snapshotTraveled) {
        motionState.restoreSnapshot(previous, snapshotAge, snapshotTraveled);
    }

    /** Returns the visual scale relative to the registered projectile geometry. */
    public double renderScale() {
        return profile.renderScale();
    }

    /** Returns the horizontal collision width. */
    public double collisionWidth() {
        return profile.collisionWidth();
    }

    /** Returns the vertical collision height. */
    public double collisionHeight() {
        return profile.collisionHeight();
    }

    /** Compatibility broad-phase size accessor. */
    public double size() {
        return Math.max(profile.collisionWidth(), profile.collisionHeight());
    }

    public int color() {
        return profile.color();
    }

    public float alpha() {
        return profile.alpha();
    }

    public Set<ResourceLocation> visualIds() {
        return profile.visualIds();
    }

    public boolean isAlive() {
        return motionState.isAlive();
    }

    public void kill() {
        motionState.kill();
    }

    public int slot() {
        return slot;
    }

    public int generation() {
        return generation;
    }

    /** Assigns the runtime identity used by the server manager and client stream. */
    public void assignSlot(int slot, int generation) {
        this.slot = slot;
        this.generation = generation;
    }

    public void setPosition(Vec3 value) {
        motionState.setPosition(value);
    }

    /** Writes the lightweight state position as its collision center. */
    @Override
    public void setCenter(Vec3 center) {
        motionState.setCenter(center);
    }

    public void setVelocity(Vec3 value) {
        motionState.setVelocity(value);
    }

    /**
     * Validates the current ordinary-bullet velocity. This is a temporary safety net for a rare
     * stalled-projectile failure and may be removed or revised after its root cause is known.
     */
    public boolean validateRuntimeVelocity() {
        return motionState.validateRuntimeVelocity();
    }

    @Override
    public void redirectTrajectory(Vec3 value) {
        if (value == null) return;
        setVelocity(value);
        if (!isAlive()) return;
        trajectoryState.runtime().redirect(position(), value);
    }

    public void setAcceleration(Vec3 value) {
        motionState.setAcceleration(value);
    }

    public void setTrackingTarget(@Nullable LivingEntity target) {
        trackingState.trackingTarget(target);
    }

    public @Nullable LivingEntity trackingTarget() {
        return trackingState.trackingTarget();
    }

    /** Records the authoritative steering point selected during this four-tick steering window. */
    public void setSteeringTarget(@Nullable Vec3 value, boolean usesControl) {
        trackingState.setSteeringTarget(value, usesControl);
    }

    public @Nullable Vec3 trackingPosition() {
        return trackingState.trackingPosition();
    }

    public boolean trackingUsesControl() {
        return trackingState.trackingUsesControl();
    }

    /** Associates this state with a compact stream-local target handle. */
    public void setTrackingHandle(int value) {
        trackingState.setTrackingHandle(value);
    }

    public int trackingHandle() {
        return trackingState.trackingHandle();
    }

    /** Counts down the failed-target search backoff once per authoritative tick. */
    public void tickTargetSearchCooldown() {
        trackingState.tickTargetSearchCooldown();
    }

    /** Returns whether this state may issue another shared candidate query. */
    public boolean canSearchTrackingTarget() {
        return trackingState.canSearchTrackingTarget();
    }

    /** Applies a bounded retry delay after target acquisition succeeds or fails. */
    public void setTargetSearchCooldown(int ticks) {
        trackingState.setTargetSearchCooldown(ticks);
    }

    public double homingRange() {
        return trackingState.homingRange();
    }

    @Override
    public double getHomingRange() {
        return trackingState.homingRange();
    }

    @Override
    public float getCollisionWidth() {
        return (float) profile.collisionWidth();
    }

    @Override
    public float getCollisionHeight() {
        return (float) profile.collisionHeight();
    }

    public double homingSteer() {
        return trackingState.homingSteer();
    }

    /** Returns the desired velocity retained between four-tick target selection passes. */
    public @Nullable Vec3 desiredVelocity() {
        return trackingState.desiredVelocity();
    }

    /** Replaces the desired steering velocity without directly changing current movement. */
    public void setDesiredVelocity(@Nullable Vec3 value) {
        trackingState.setDesiredVelocity(value);
    }

    /** Returns the actual steering delta applied during the latest simulation tick. */
    public Vec3 steeringAcceleration() {
        return trackingState.steeringAcceleration();
    }

    /** Returns the maximum velocity-vector change permitted by one steering tick. */
    public double maxSteeringAcceleration() {
        return trackingState.maxSteeringAcceleration();
    }

    /** Returns the maximum speed magnitude change permitted by one steering tick. */
    public double maxSpeedChange() {
        return trackingState.maxSpeedChange();
    }

    /** Applies one bounded movement step toward the current desired velocity. */
    public void applySteering() {
        motionState.replaceVelocity(trackingState.applySteering(velocity()));
    }

    /**
     * Applies a bounded post-steering correction without replacing the coordinate-derived heading.
     */
    public void applyVelocityCorrection(@Nullable Vec3 authoritativeVelocity) {
        motionState.replaceVelocity(
            trackingState.velocityCorrection(velocity(), authoritativeVelocity));
    }

    public int hitCooldownTicks() {
        return collisionState.hitCooldownTicks();
    }

    public void setHitCooldownTicks(int ticks) {
        collisionState.setHitCooldownTicks(ticks);
    }

    /** Decrements the post-hit cooldown once per simulation tick. */
    public void tickHitCooldown() {
        collisionState.tickHitCooldown();
    }

    public double controlRange() {
        return trackingState.controlRange();
    }

    public double controlSteer() {
        return trackingState.controlSteer();
    }

    /** Returns whether this bullet applies the normal one-hit-per-target filter. */
    public boolean rememberHitTargets() {
        return trackingState.rememberHitTargets();
    }

    /** Returns the source-specific steering controller. */
    public BulletSteeringMode steeringMode() {
        return trackingState.steeringMode();
    }

    /** Moves the center completely beyond its collision extent along a resolved block normal. */
    @Override
    public void pushOutOfBlock(Vec3 outwardNormal) {
        if (outwardNormal == null || outwardNormal.lengthSqr() < 1.0E-12D) return;
        Vec3 normal = outwardNormal.normalize();
        double extent =
            Math.abs(normal.y) > 0.5D
                ? profile.collisionHeight() * 0.5D
                : profile.collisionWidth() * 0.5D;
        motionState.pushPosition(normal.scale(extent + 1.0E-4D));
    }

    /** Replays the server-selected steering point without querying client entities. */
    public void applyTrackingSample(Vec3 targetPosition, boolean usesControl) {
        if (targetPosition == null) return;
        TrackingProfile profile = TrackingProfile.forBullet(this);
        Vec3 desired = profile.desiredVelocity(this, targetPosition, usesControl);
        if (trackingState.steeringMode() == BulletSteeringMode.DIRECT)
            applyDirectSteering(targetPosition, desired.length());
        else {
            setDesiredVelocity(desired);
            applySteering();
        }
    }

    /** Applies one server-time target and velocity sample as a single coherent update. */
    public void applyDirectTrackingSample(
        @Nullable Vec3 targetPosition, @Nullable Vec3 authoritativeVelocity, boolean usesControl) {
        if (targetPosition != null) applyTrackingSample(targetPosition, usesControl);
        applyVelocityCorrection(authoritativeVelocity);
    }

    /** Directly points the velocity at a target while applying the supplied speed. */
    public void applyDirectSteering(Vec3 targetPosition, double speed) {
        if (targetPosition == null) return;
        Vec3 result = trackingState.directSteering(position(), targetPosition, speed);
        if (result != null) motionState.replaceVelocity(result);
    }

    /** Steers the velocity toward a target while preserving its current speed. */
    public void steerTowards(Vec3 targetPosition) {
        motionState.replaceVelocity(
            trackingState.steerTowards(position(), velocity(), targetPosition));
    }

    /** Applies a bounded visual correction while retaining continuous client motion. */
    public void applyCorrection(
        Vec3 authoritativePosition, Vec3 authoritativeVelocity, double blend) {
        motionState.applyCorrection(authoritativePosition, authoritativeVelocity, blend);
    }

    @Override
    public SplitSequence getSplitSequence() {
        return splitSequence;
    }

    /** Returns an independent creation-context snapshot for split and effect execution. */
    @Override
    public AttackContext getAttackContext() {
        return identity.attackContext() == null
            ? null
            : identity.attackContext().toBuilder()
                .hitBlockPositions(collisionState.hitBlockPositions())
                .inheritTrajectorySnapshot(trajectoryState.runtime().snapshot())
                .build();
    }

    @Override
    public ResourceLocation getTypeId() {
        return identity.typeId();
    }

    @Override
    public ResourceLocation getRootTypeId() {
        return identity.rootTypeId();
    }

    @Override
    public SplitTriggerCounts getSplitTriggerCounts() {
        return splitTriggerCounts.copy();
    }

    @Override
    public void recordSplitTrigger(SplitTriggerType type) {
        splitTriggerCounts.increment(type);
    }

    @Override
    public float getDamage() {
        return damage;
    }

    @Override
    public Vec3 getVelocity() {
        return motionState.velocity();
    }

    @Override
    public double getTraveled() {
        return motionState.traveled();
    }

    @Override
    public double getRange() {
        return motionState.range();
    }

    @Override
    public Vec3 getPosition() {
        return motionState.position();
    }

    @Override
    public Vec3 getCenter() {
        return motionState.position();
    }

    @Override
    public @Nullable LivingEntity getOwner() {
        return identity.owner();
    }

    @Override
    public @Nullable Object getShooter() {
        return identity.shooter();
    }

    public @Nullable UUID ownerUuid() {
        return identity.ownerUuid();
    }

    @Override
    public boolean noGravity() {
        return noGravity;
    }

    @Override
    public boolean isHoming() {
        return trackingState.homing();
    }

    @Override
    public boolean isSpectral() {
        return spectral;
    }

    @Override
    public boolean isControllable() {
        return trackingState.controllable();
    }

    @Override
    public boolean isPiercing() {
        return piercing;
    }

    @Override
    public ResourceLocation getColorId() {
        return profile.colorId();
    }

    @Override
    public TrajectoryRuntime getTrajectoryRuntime() {
        return trajectoryState.runtime();
    }

    @Override
    public int getTrajectoryAge() {
        return motionState.age();
    }

    @Override
    public CompositeTrigger getTriggers() {
        return triggers;
    }

    @Override
    public @Nullable BlockHitResult getLastBlockHit() {
        return collisionState.lastBlockHit();
    }

    @Override
    public void setLastBlockHit(@Nullable BlockHitResult value) {
        collisionState.lastBlockHit(value);
    }

    /** Returns a read-only view of exact blocks contacted by this shot. */
    @Override
    public Set<BlockPos> getHitBlockPositions() {
        return collisionState.hitBlockPositions();
    }

    /** Records one exact block contact for this shot. */
    @Override
    public boolean markBlockHit(@Nullable BlockPos position) {
        return collisionState.markBlockHit(position);
    }

    @Override
    public DamagedEntities getDamagedEntities() {
        return collisionState.damagedEntities();
    }

    /** Mutable construction helper; the resulting state owns defensive copies. */
    public static final class Builder {
        private Vec3 position = Vec3.ZERO, velocity = Vec3.ZERO, acceleration = Vec3.ZERO;
        private double baseSpeed;
        private TrajectoryRuntime.Snapshot trajectorySnapshot;
        private List<TrajectorySpec> trajectorySpecs = List.of();
        private int lifetime = 1;
        private double range;
        private float damage = 1.0f;
        private double renderScale = 1.0D, collisionWidth = 0.125D, collisionHeight = 0.125D;
        private int color = 0xFFFFFF;
        private float alpha = 1.0F;
        private ResourceLocation colorId =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "base");
        private Set<ResourceLocation> visualIds = Set.of();
        private LivingEntity owner;
        private Object shooter;
        private UUID ownerUuid;
        private AttackContext attackContext;
        private SplitSequence splitSequence;
        private CompositeTrigger triggers = new CompositeTrigger();
        private Set<BlockPos> hitBlockPositions = Set.of();
        private boolean noGravity;
        private boolean spectral, piercing, homing, controllable;
        private boolean rememberHitTargets = true;
        private BulletSteeringMode steeringMode = BulletSteeringMode.LIMITED;
        private double homingRange = 4.0;
        private double homingSteer = 0.6;
        private double maxSteeringAcceleration = 0.25D;
        private double maxSpeedChange = 0.15D;
        private ResourceLocation typeId = ModAttackTypes.BULLET.getId();
        private ResourceLocation rootTypeId = ModAttackTypes.BULLET.getId();
        private double controlRange = 64.0D, controlSteer = 0.8D;

        public Builder position(Vec3 v) {
            position = v;
            return this;
        }

        public Builder velocity(Vec3 v) {
            velocity = v;
            return this;
        }

        public Builder acceleration(Vec3 v) {
            acceleration = v;
            return this;
        }

        public Builder baseSpeed(double v) {
            baseSpeed = Math.max(0.0D, v);
            return this;
        }

        /** Network restoration preserves the original launch frame, unlike child construction. */
        public Builder restoreTrajectory(TrajectoryRuntime.Snapshot snapshot) {
            trajectorySnapshot = snapshot;
            return this;
        }

        public Builder trajectorySpecs(List<TrajectorySpec> v) {
            trajectorySpecs = v == null ? List.of() : List.copyOf(v);
            return this;
        }

        public Builder lifetime(int v) {
            lifetime = v;
            return this;
        }

        public Builder range(double v) {
            range = v;
            return this;
        }

        public Builder damage(float v) {
            damage = v;
            return this;
        }

        public Builder damage(double v) {
            damage = (float) v;
            return this;
        }

        public Builder renderScale(double v) {
            renderScale = v;
            return this;
        }

        public Builder collisionWidth(double v) {
            collisionWidth = v;
            return this;
        }

        public Builder collisionHeight(double v) {
            collisionHeight = v;
            return this;
        }

        public Builder color(int v) {
            color = v;
            return this;
        }

        public Builder alpha(float v) {
            alpha = v;
            return this;
        }

        public Builder colorId(ResourceLocation v) {
            colorId = v;
            return this;
        }

        public Builder visualIds(Set<ResourceLocation> v) {
            visualIds = v == null ? Set.of() : Set.copyOf(v);
            return this;
        }

        /** Supplies block contacts inherited by a split child. */
        public Builder hitBlockPositions(Set<BlockPos> v) {
            hitBlockPositions = v == null ? Set.of() : Set.copyOf(v);
            return this;
        }

        public Builder owner(LivingEntity v) {
            owner = v;
            return this;
        }

        public Builder shooter(Object v) {
            shooter = v;
            return this;
        }

        public Builder ownerUuid(UUID v) {
            ownerUuid = v;
            return this;
        }

        public Builder attackContext(AttackContext v) {
            attackContext = v;
            return this;
        }

        public Builder splitSequence(SplitSequence v) {
            splitSequence = v;
            return this;
        }

        public Builder triggers(CompositeTrigger v) {
            triggers = v == null ? new CompositeTrigger() : v.copy();
            return this;
        }

        public Builder spectral(boolean v) {
            spectral = v;
            return this;
        }

        public Builder piercing(boolean v) {
            piercing = v;
            return this;
        }

        public Builder noGravity(boolean v) {
            noGravity = v;
            return this;
        }

        public Builder homing(boolean v) {
            homing = v;
            return this;
        }

        public Builder controllable(boolean v) {
            controllable = v;
            return this;
        }

        public Builder rememberHitTargets(boolean v) {
            rememberHitTargets = v;
            return this;
        }

        public Builder steeringMode(BulletSteeringMode v) {
            steeringMode = v == null ? BulletSteeringMode.LIMITED : v;
            return this;
        }

        public Builder homingRange(double v) {
            this.homingRange = Math.max(0.0, v);
            return this;
        }

        public Builder homingSteer(double v) {
            this.homingSteer = Math.max(0.0, Math.min(1.0, v));
            return this;
        }

        public Builder maxSteeringAcceleration(double v) {
            maxSteeringAcceleration = Math.max(0.0D, v);
            return this;
        }

        public Builder maxSpeedChange(double v) {
            maxSpeedChange = Math.max(0.0D, v);
            return this;
        }

        /** Identity for standalone states; contextual states use their AttackContext definition. */
        public Builder attackType(AttackType type) {
            return typeIds(type.getId(), type.getRootId());
        }

        /** Restores the immutable identity declared by the server's attack definition. */
        public Builder typeIds(ResourceLocation current, ResourceLocation root) {
            typeId = java.util.Objects.requireNonNull(current, "typeId");
            rootTypeId = java.util.Objects.requireNonNull(root, "rootTypeId");
            return this;
        }

        public Builder controlRange(double v) {
            controlRange = Math.max(0.0D, v);
            return this;
        }

        public Builder controlSteer(double v) {
            controlSteer = Math.max(0.0D, Math.min(1.0D, v));
            return this;
        }

        public BulletState build() {
            return new BulletState(this);
        }
    }

}
