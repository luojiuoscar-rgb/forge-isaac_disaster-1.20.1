package net.luojiuoscar.isaac_disaster.bullet.core;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.bullet.tracking.TrackingProfile;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.BulletSourceType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.attack_type.util.DamagedEntities;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.BulletColor;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitSequence;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitTriggerCounts;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitTriggerType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.HashSet;

/** Lightweight transient projectile state. It deliberately contains no Minecraft Entity lifecycle. */
public final class BulletState implements IBulletObject {
    private Vec3 position;
    private Vec3 previousPosition;
    private Vec3 velocity;
    private Vec3 acceleration;
    private final double baseSpeed;
    private int age;
    private int lifetime;
    private double traveled;
    private double range;
    private float damage;
    private double renderScale;
    private double collisionWidth;
    private double collisionHeight;
    private int color;
    private float alpha = 1.0F;
    private ResourceLocation colorId;
    private Set<ResourceLocation> visualIds = Set.of();
    private boolean spectral;
    private boolean piercing;
    private boolean homing;
    private boolean controllable;
    private final boolean rememberHitTargets;
    private final BulletSteeringMode steeringMode;
    private BulletSourceType sourceType = BulletSourceType.TEAR_BULLET;
    private double homingRange = 4.0;
    private double homingSteer = 0.6;
    private Vec3 desiredVelocity;
    private Vec3 steeringAcceleration = Vec3.ZERO;
    private double maxSteeringAcceleration = 0.25D;
    private double maxSpeedChange = 0.15D;
    private LivingEntity trackingTarget;
    private int targetSearchCooldown;
    private int hitCooldownTicks;
    private int trackingHandle;
    private Vec3 trackingPosition;
    private boolean trackingUsesControl;
    private double controlRange = 64.0D;
    private double controlSteer = 0.8D;
    private int slot = -1;
    private int generation;
    private boolean alive = true;
    private LivingEntity owner;
    private Object shooter;
    private UUID ownerUuid;
    private AttackContext attackContext;
    private SplitSequence splitSequence = new SplitSequence();
    private final SplitTriggerCounts splitTriggerCounts = new SplitTriggerCounts();
    private final DamagedEntities damagedEntities = new DamagedEntities();
    private final CompositeTrigger triggers = new CompositeTrigger();
    private BlockHitResult lastBlockHit;
    private final Set<BlockPos> hitBlockPositions = new HashSet<>();

    private BulletState(Builder b) {
        this.position = b.position;
        this.previousPosition = b.position;
        this.velocity = b.velocity;
        this.acceleration = b.acceleration;
        this.baseSpeed = b.baseSpeed > 0.0D ? b.baseSpeed : b.velocity.length();
        this.lifetime = Math.max(1, b.lifetime);
        this.range = Math.max(0.0, b.range);
        this.damage = b.damage;
        this.renderScale = Math.max(0.0, b.renderScale);
        this.collisionWidth = Math.max(0.0, b.collisionWidth);
        this.collisionHeight = Math.max(0.0, b.collisionHeight);
        this.color = b.color;
        this.alpha = Math.max(0.0F, Math.min(1.0F, b.alpha));
        this.colorId = b.colorId;
        this.visualIds = Set.copyOf(b.visualIds);
        this.owner = b.owner;
        this.shooter = b.shooter;
        this.ownerUuid = b.ownerUuid != null ? b.ownerUuid : b.owner == null ? null : b.owner.getUUID();
        this.attackContext = b.attackContext == null ? null : b.attackContext.copy();
        this.hitBlockPositions.addAll(this.attackContext == null ? b.hitBlockPositions : this.attackContext.getHitBlockPositions());
        this.spectral = b.spectral;
        this.piercing = b.piercing;
        this.homing = b.homing;
        this.controllable = b.controllable;
        this.rememberHitTargets = b.rememberHitTargets;
        this.steeringMode = b.steeringMode;
        this.sourceType = b.sourceType;
        this.hitCooldownTicks = 0;
        this.controlRange = Math.max(0.0D, b.controlRange);
        this.controlSteer = Math.max(0.0D, Math.min(1.0D, b.controlSteer));
        this.homingRange = b.homingRange;
        this.homingSteer = b.homingSteer;
        this.maxSteeringAcceleration = b.maxSteeringAcceleration;
        this.maxSpeedChange = b.maxSpeedChange;
        if (b.splitSequence != null) this.splitSequence = b.splitSequence.copy();
        this.triggers.addAll(b.triggers);
}
    /** Starts building a standalone state for tests or non-entity runtime adapters. */
    public static Builder builder() { return new Builder(); }
    /** Creates a trajectory-free lightweight state snapshot from an attack context. */
    public static Builder from(AttackContext context) {
        BulletColor color = resolveColor(context.getColorRl());
        return builder().position(context.getPos()).velocity(context.getMainAxis().scale(context.getBulletSpeed()))
                .baseSpeed(context.getBulletSpeed())
                .range(context.getBulletRange()).damage(context.getDamage()).owner(context.getOwner())
                .shooter(context.getShooter()).attackContext(context).splitSequence(context.copySplitSequence())
                .triggers(context.copyTrigger()).colorId(context.getColorRl()).color(color.color()).alpha(color.alpha())
                .visualIds(context.getVisualIds());

    }

    /** Resolves the one-time RGB/alpha snapshot so the client never needs the color registry per bullet. */
    private static BulletColor resolveColor(ResourceLocation colorId) {
        IForgeRegistry<BulletColor> registry = RegistryManager.ACTIVE.getRegistry(ModBulletColors.BULLET_COLOR_KEY);
        BulletColor color = registry == null || colorId == null ? null : registry.getValue(colorId);
        return color == null ? ModBulletColors.BASE.get() : color;
    }

    /** Advances one authoritative tick using only base kinematics. Returns whether still alive. */
    public boolean tickPhysics() {
        if (!alive) return false;
        tickHitCooldown();
        age++;
        // TearBullet decrements its life counter before moving. The final tick therefore
        // dispatches end-of-life without advancing the projectile one additional step.
        if (age >= lifetime) {
            alive = false;
            return false;
        }
        previousPosition = position;
        velocity = velocity.add(acceleration);
        position = position.add(velocity);
        traveled += velocity.length();
        return alive;
    }

    public Vec3 position() { return position; }
    public Vec3 previousPosition() { return previousPosition; }
    public Vec3 velocity() { return velocity; }
    public Vec3 acceleration() { return acceleration; }
    /** Returns the creation-time cruise speed used when homing is inactive. */
    public double baseSpeed() { return baseSpeed; }
    public int age() { return age; }
    public int lifetime() { return lifetime; }
    public double traveled() { return traveled; }
    /** Restores a server snapshot without applying client-local catch-up simulation. */
    public void restoreSnapshot(Vec3 previous, int snapshotAge, double snapshotTraveled) {
        previousPosition = previous == null ? position : previous;
        age = Math.max(0, snapshotAge);
        traveled = Math.max(0.0D, snapshotTraveled);
    }
    /** Returns the visual scale relative to the registered projectile geometry. */
    public double renderScale() { return renderScale; }
    /** Returns the horizontal collision width. */
    public double collisionWidth() { return collisionWidth; }
    /** Returns the vertical collision height. */
    public double collisionHeight() { return collisionHeight; }
    /** Compatibility broad-phase size accessor. */
    public double size() { return Math.max(collisionWidth, collisionHeight); }
    public int color() { return color; }
    public float alpha() { return alpha; }
    public Set<ResourceLocation> visualIds() { return visualIds; }
    public boolean isAlive() { return alive; }
    public void kill() { alive = false; }
    public int slot() { return slot; }
    public int generation() { return generation; }
    /** Assigns the runtime identity used by the server manager and client stream. */
    public void assignSlot(int slot, int generation) { this.slot = slot; this.generation = generation; }
    public void setPosition(Vec3 value) { position = value; }
    /** Writes the lightweight state position as its collision center. */
    @Override public void setCenter(Vec3 center) { if (center != null) position = center; }
    public void setVelocity(Vec3 value) { velocity = value; }
    public void setAcceleration(Vec3 value) { acceleration = value; }
    public void setTrackingTarget(@Nullable LivingEntity target) { trackingTarget = target; }
    public @Nullable LivingEntity trackingTarget() { return trackingTarget; }
    /** Records the authoritative steering point selected during this four-tick steering window. */
    public void setSteeringTarget(@Nullable Vec3 value, boolean usesControl) {
        trackingPosition = value;
        trackingUsesControl = usesControl;
    }
    public @Nullable Vec3 trackingPosition() { return trackingPosition; }
    public boolean trackingUsesControl() { return trackingUsesControl; }
    /** Associates this state with a compact stream-local target handle. */
    public void setTrackingHandle(int value) { trackingHandle = Math.max(0, value); }
    public int trackingHandle() { return trackingHandle; }
    /** Counts down the failed-target search backoff once per authoritative tick. */
    public void tickTargetSearchCooldown() { if (targetSearchCooldown > 0) targetSearchCooldown--; }
    /** Returns whether this state may issue another shared candidate query. */
    public boolean canSearchTrackingTarget() { return targetSearchCooldown == 0; }
    /** Applies a bounded retry delay after target acquisition succeeds or fails. */
    public void setTargetSearchCooldown(int ticks) { targetSearchCooldown = Math.max(0, ticks); }
    public double homingRange() { return homingRange; }
    @Override public double getHomingRange() { return homingRange; }
    @Override public float getCollisionWidth() { return (float) collisionWidth; }
    @Override public float getCollisionHeight() { return (float) collisionHeight; }
    public double homingSteer() { return homingSteer; }
    /** Returns the desired velocity retained between four-tick target selection passes. */
    public @Nullable Vec3 desiredVelocity() { return desiredVelocity; }
    /** Replaces the desired steering velocity without directly changing current movement. */
    public void setDesiredVelocity(@Nullable Vec3 value) { desiredVelocity = value; }
    /** Returns the actual steering delta applied during the latest simulation tick. */
    public Vec3 steeringAcceleration() { return steeringAcceleration; }
    /** Returns the maximum velocity-vector change permitted by one steering tick. */
    public double maxSteeringAcceleration() { return maxSteeringAcceleration; }
    /** Returns the maximum speed magnitude change permitted by one steering tick. */
    public double maxSpeedChange() { return maxSpeedChange; }
    /** Applies one bounded movement step toward the current desired velocity. */
    public void applySteering() {
        if (steeringMode == BulletSteeringMode.DIRECT) {
            if (desiredVelocity == null) {
                steeringAcceleration = Vec3.ZERO;
                return;
            }
            steeringAcceleration = desiredVelocity.subtract(velocity);
            velocity = desiredVelocity;
            return;
        }
        if (desiredVelocity == null || velocity.lengthSqr() < 1.0E-12D || desiredVelocity.lengthSqr() < 1.0E-12D) {
            steeringAcceleration = Vec3.ZERO;
            return;
        }
        double currentSpeed = velocity.length();
        double desiredSpeed = desiredVelocity.length();
        double limitedSpeed = approach(currentSpeed, desiredSpeed, maxSpeedChange);
        Vec3 limitedTarget = desiredVelocity.normalize().scale(limitedSpeed);
        Vec3 delta = limitedTarget.subtract(velocity);
        if (delta.lengthSqr() > maxSteeringAcceleration * maxSteeringAcceleration) {
            delta = delta.normalize().scale(maxSteeringAcceleration);
        }
        velocity = velocity.add(delta);
        steeringAcceleration = delta;
    }

    /** Applies a bounded post-steering correction without replacing the coordinate-derived heading. */
    public void applyVelocityCorrection(@Nullable Vec3 authoritativeVelocity) {
        if (authoritativeVelocity == null) return;
        double authoritativeSpeed = authoritativeVelocity.length();
        double currentSpeed = velocity.length();
        if (authoritativeSpeed < 1.0E-8D) {
            if (currentSpeed > maxSpeedChange) velocity = velocity.normalize().scale(currentSpeed - maxSpeedChange);
            return;
        }
        double speedDelta = authoritativeSpeed - currentSpeed;
        if (Math.abs(speedDelta) > 0.01D) {
            double limitedDelta = Math.max(-maxSpeedChange, Math.min(maxSpeedChange, speedDelta)) * 0.5D;
            double correctedSpeed = Math.max(0.0D, currentSpeed + limitedDelta);
            velocity = velocity.lengthSqr() < 1.0E-12D
                    ? authoritativeVelocity.normalize().scale(correctedSpeed)
                    : velocity.normalize().scale(correctedSpeed);
        }
        if (velocity.lengthSqr() > 1.0E-12D) {
            Vec3 currentDirection = velocity.normalize();
            Vec3 authorityDirection = authoritativeVelocity.normalize();
            if (currentDirection.dot(authorityDirection) < 0.999D) {
                Vec3 blendedDirection = currentDirection.lerp(authorityDirection, 0.08D).normalize();
                velocity = blendedDirection.scale(velocity.length());
            }
        }
    }
    public int hitCooldownTicks() { return hitCooldownTicks; }
    public void setHitCooldownTicks(int ticks) { hitCooldownTicks = Math.max(0, ticks); }
    /** Decrements the post-hit cooldown once per simulation tick. */
    public void tickHitCooldown() { if (hitCooldownTicks > 0) hitCooldownTicks--; }
    public double controlRange() { return controlRange; }
    public double controlSteer() { return controlSteer; }
    /** Returns whether this bullet applies the normal one-hit-per-target filter. */
    public boolean rememberHitTargets() { return rememberHitTargets; }
    /** Returns the source-specific steering controller. */
    public BulletSteeringMode steeringMode() { return steeringMode; }
    /** Moves the center completely beyond its collision extent along a resolved block normal. */
    @Override
    public void pushOutOfBlock(Vec3 outwardNormal) {
        if (outwardNormal == null || outwardNormal.lengthSqr() < 1.0E-12D) return;
        Vec3 normal = outwardNormal.normalize();
        double extent = Math.abs(normal.y) > 0.5D ? collisionHeight * 0.5D : collisionWidth * 0.5D;
        position = position.add(normal.scale(extent + 1.0E-4D));
        previousPosition = position;
    }

    /** Replays the server-selected steering point without querying client entities. */
    public void applyTrackingSample(Vec3 targetPosition, boolean usesControl) {
        if (targetPosition == null) return;
        TrackingProfile profile = TrackingProfile.forBullet(this);
        Vec3 desired = profile.desiredVelocity(this, targetPosition, usesControl);
        if (steeringMode == BulletSteeringMode.DIRECT) applyDirectSteering(targetPosition, desired.length());
        else {
            setDesiredVelocity(desired);
            applySteering();
        }
    }

    /** Applies one server-time target and velocity sample as a single coherent update. */
    public void applyDirectTrackingSample(@Nullable Vec3 targetPosition, @Nullable Vec3 authoritativeVelocity,
                                           boolean usesControl) {
        if (targetPosition != null) applyTrackingSample(targetPosition, usesControl);
        applyVelocityCorrection(authoritativeVelocity);
    }
    /** Directly points the velocity at a target while applying the supplied speed. */
    public void applyDirectSteering(Vec3 targetPosition, double speed) {
        if (targetPosition == null) return;
        Vec3 toTarget = targetPosition.subtract(position);
        if (toTarget.lengthSqr() < 1.0E-8D) return;
        velocity = toTarget.normalize().scale(Math.max(0.0D, speed));
        steeringAcceleration = Vec3.ZERO;
    }
    /** Steers the velocity toward a target while preserving its current speed. */
    public void steerTowards(Vec3 targetPosition) {
        Vec3 toTarget = targetPosition.subtract(position);
        if (toTarget.lengthSqr() < 1.0E-8 || velocity.lengthSqr() < 1.0E-8) return;
        Vec3 current = velocity.normalize();
        double strength = homingSteer * Math.min(1.0, toTarget.length());
        velocity = current.add(toTarget.normalize().subtract(current).scale(strength)).normalize().scale(velocity.length());
    }
    private static double approach(double current, double target, double limit) {
        return current < target ? Math.min(current + limit, target) : Math.max(current - limit, target);
    }
    /** Applies a bounded visual correction while retaining continuous client motion. */
    public void applyCorrection(Vec3 authoritativePosition, Vec3 authoritativeVelocity, double blend) {
        double factor = Math.max(0.0, Math.min(1.0, blend));
        previousPosition = position;
        position = position.lerp(authoritativePosition, factor);
        velocity = velocity.lerp(authoritativeVelocity, factor);
    }

    @Override public SplitSequence getSplitSequence() { return splitSequence; }
    /** Returns an independent creation-context snapshot for split and effect execution. */
    @Override public AttackContext getAttackContext() {
        return attackContext == null ? null : attackContext.toBuilder()
                .hitBlockPositions(hitBlockPositions).build();
    }
    @Override public BulletSourceType getSourceType() { return sourceType; }
    @Override public SplitTriggerCounts getSplitTriggerCounts() { return splitTriggerCounts; }
    @Override public void recordSplitTrigger(SplitTriggerType type) { splitTriggerCounts.increment(type); }
    @Override public float getDamage() { return damage; }
    @Override public Vec3 getVelocity() { return velocity; }
    @Override public double getTraveled() { return traveled; }
    @Override public double getRange() { return range; }
    @Override public Vec3 getPosition() { return position; }
    @Override public Vec3 getCenter() { return position; }
    @Override public @Nullable LivingEntity getOwner() { return owner; }
    @Override public @Nullable Object getShooter() { return shooter; }
    public @Nullable UUID ownerUuid() { return ownerUuid; }
    @Override public Vec3 getPrevShooterPos() { return Vec3.ZERO; }
    @Override public double getStartYRot() { return 0.0; }
    @Override public double getStartXRot() { return 0.0; }
    @Override public boolean noGravity() { return true; }
    @Override public boolean isHoming() { return homing; }
    @Override public boolean isSpectral() { return spectral; }
    @Override public boolean isControllable() { return controllable; }
    @Override public boolean isPiercing() { return piercing; }
    @Override public ResourceLocation getColorId() { return colorId; }
    @Override public Map<ResourceLocation, Integer> getTrajectories() { return Map.of(); }
    @Override public CompositeTrigger getTriggers() { return triggers; }
    @Override public @Nullable BlockHitResult getLastBlockHit() { return lastBlockHit; }
    @Override public void setLastBlockHit(@Nullable BlockHitResult value) { lastBlockHit = value; }
    /** Returns a read-only view of exact blocks contacted by this shot. */
    @Override public Set<BlockPos> getHitBlockPositions() { return Set.copyOf(hitBlockPositions); }
    /** Records one exact block contact for this shot. */
    @Override public boolean markBlockHit(@Nullable BlockPos position) {
        return position != null && hitBlockPositions.add(position.immutable());
    }
    @Override public DamagedEntities getDamagedEntities() { return damagedEntities; }

    /** Mutable construction helper; the resulting state owns defensive copies. */
    public static final class Builder {
        private Vec3 position = Vec3.ZERO, velocity = Vec3.ZERO, acceleration = Vec3.ZERO;
        private double baseSpeed;
        private int lifetime = 1;
        private double range;
        private float damage = 1.0f;
        private double renderScale = 1.0D, collisionWidth = 0.125D, collisionHeight = 0.125D;
        private int color = 0xFFFFFF;
        private float alpha = 1.0F;
        private ResourceLocation colorId = ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "base");
        private Set<ResourceLocation> visualIds = Set.of();
        private LivingEntity owner;
        private Object shooter;
        private UUID ownerUuid;
        private AttackContext attackContext;
        private SplitSequence splitSequence;
        private CompositeTrigger triggers = new CompositeTrigger();
        private Set<BlockPos> hitBlockPositions = Set.of();
        private boolean spectral, piercing, homing, controllable;
        private boolean rememberHitTargets = true;
        private BulletSteeringMode steeringMode = BulletSteeringMode.LIMITED;
        private double homingRange = 4.0;
        private double homingSteer = 0.6;
        private double maxSteeringAcceleration = 0.25D;
        private double maxSpeedChange = 0.15D;
        private BulletSourceType sourceType = BulletSourceType.TEAR_BULLET;
        private double controlRange = 64.0D, controlSteer = 0.8D;
        public Builder position(Vec3 v) { position = v; return this; }
        public Builder velocity(Vec3 v) { velocity = v; return this; }
        public Builder acceleration(Vec3 v) { acceleration = v; return this; }
        public Builder baseSpeed(double v) { baseSpeed = Math.max(0.0D, v); return this; }
        public Builder lifetime(int v) { lifetime = v; return this; }
        public Builder range(double v) { range = v; return this; }
        public Builder damage(float v) { damage = v; return this; }
        public Builder damage(double v) { damage = (float) v; return this; }
        public Builder renderScale(double v) { renderScale = v; return this; }
        public Builder collisionWidth(double v) { collisionWidth = v; return this; }
        public Builder collisionHeight(double v) { collisionHeight = v; return this; }
        public Builder color(int v) { color = v; return this; }
        public Builder alpha(float v) { alpha = v; return this; }
        public Builder colorId(ResourceLocation v) { colorId = v; return this; }
        public Builder visualIds(Set<ResourceLocation> v) { visualIds = v == null ? Set.of() : Set.copyOf(v); return this; }
        /** Supplies block contacts inherited by a split child. */
        public Builder hitBlockPositions(Set<BlockPos> v) { hitBlockPositions = v == null ? Set.of() : Set.copyOf(v); return this; }
        public Builder owner(LivingEntity v) { owner = v; return this; }
        public Builder shooter(Object v) { shooter = v; return this; }
        public Builder ownerUuid(UUID v) { ownerUuid = v; return this; }
        public Builder attackContext(AttackContext v) { attackContext = v; return this; }
        public Builder splitSequence(SplitSequence v) { splitSequence = v; return this; }
        public Builder triggers(CompositeTrigger v) { triggers = v == null ? new CompositeTrigger() : v.copy(); return this; }
        public Builder spectral(boolean v) { spectral = v; return this; }
        public Builder piercing(boolean v) { piercing = v; return this; }
        public Builder homing(boolean v) { homing = v; return this; }
        public Builder controllable(boolean v) { controllable = v; return this; }
        public Builder rememberHitTargets(boolean v) { rememberHitTargets = v; return this; }
        public Builder steeringMode(BulletSteeringMode v) { steeringMode = v == null ? BulletSteeringMode.LIMITED : v; return this; }
        public Builder homingRange(double v) { this.homingRange = Math.max(0.0, v); return this; }
        public Builder homingSteer(double v) { this.homingSteer = Math.max(0.0, Math.min(1.0, v)); return this; }
        public Builder maxSteeringAcceleration(double v) { maxSteeringAcceleration = Math.max(0.0D, v); return this; }
        public Builder maxSpeedChange(double v) { maxSpeedChange = Math.max(0.0D, v); return this; }
        public Builder sourceType(BulletSourceType v) { sourceType = v == null ? BulletSourceType.TEAR_BULLET : v; return this; }
        public Builder controlRange(double v) { controlRange = Math.max(0.0D, v); return this; }
        public Builder controlSteer(double v) { controlSteer = Math.max(0.0D, Math.min(1.0D, v)); return this; }
        public BulletState build() { return new BulletState(this); }
    }

}
