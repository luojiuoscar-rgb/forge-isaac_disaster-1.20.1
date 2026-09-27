package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.helper.GeometryHelper;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitSequence;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectorySpec;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectorySequence;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/** Creation-time data used to construct one attack object. */
public class AttackContext {
    public static final double DEFAULT_RANGE = 18.0;
    public static final double DEFAULT_SPEED = 1.0;
    public static final double MIN_RANGE = 1.0;
    public static final double MAX_RANGE = 64.0;
    private static final double MIN_BULLET_SCALE = 0.25D;
    private static final double BULLET_SCALE_BASE_DAMAGE = 2.0D;

    private ResourceLocation colorRl;
    private Set<ResourceLocation> visualIds;
    private final CompositeTrigger trigger;
    private final TrajectorySequence trajectorySequence;
    private Vec3 pos;
    private Vec3 mainAxis;
    private final float damage;
    private double bulletScaleModifier;
    private double bulletScale;
    private final double bulletRange;
    private final double bulletSpeed;
    private SplitSequence splitSequence;
    private boolean useExactSpawnPosition;
    private boolean frozen;
    private final Entity shooter;
    private final LivingEntity owner;
    private final Set<BlockPos> hitBlockPositions;
    /**
     * Optional immutable parent snapshot used to initialize a child's trajectory runtime.
     * Live projectiles retain copyConfiguration(), which excludes this consumed snapshot.
     */
    private final TrajectoryRuntime.Snapshot inheritedTrajectorySnapshot;

    private AttackContext(Builder builder) {
        this.owner = Objects.requireNonNull(builder.owner, "owner");
        this.shooter = builder.shooter == null ? owner : builder.shooter;
        this.colorRl = builder.colorRl;
        this.visualIds = Set.copyOf(builder.visualIds);
        this.trigger = builder.trigger == null ? new CompositeTrigger() : builder.trigger.copy();
        this.trajectorySequence = builder.trajectorySequence.copy();
        this.pos = builder.position == null ? owner.position() : builder.position;
        this.attackType = builder.attackType;
        if (builder.direction != null) {
            this.mainAxis = normalizeMainAxis(builder.direction);
        } else {
            this.mainAxis = builder.mainAxis == null
                    ? GeometryHelper.mainAxisFromRotation(owner.getXRot(), owner.getYRot())
                    : normalizeMainAxis(builder.mainAxis);
        }
        this.damage = resolveDamage(owner, builder.damage);
        this.bulletScaleModifier = resolveBulletScaleModifier(owner, builder.bulletScaleModifier);
        this.bulletScale = resolveBulletScale(damage, bulletScaleModifier);
        this.bulletRange = sanitizeRange(builder.range);
        this.bulletSpeed = sanitizeSpeed(builder.speed);
        this.splitSequence = builder.splitSequence == null ? new SplitSequence() : builder.splitSequence.copy();
        this.useExactSpawnPosition = builder.useExactSpawnPosition;
        this.hitBlockPositions = immutableBlockPositions(builder.hitBlockPositions);
        this.inheritedTrajectorySnapshot = builder.inheritedTrajectorySnapshot;
    }

    public static Builder builder(@NotNull LivingEntity owner, @Nullable Entity shooter) {
        return new Builder(owner, shooter);
    }

    /** Derives a new attack: size is recalculated from its damage and inherited modifier. */
    public Builder toBuilder() {
        return Builder.from(this).color(colorRl).visuals(visualIds).trigger(trigger).trajectorySequence(trajectorySequence)
                .position(pos).attackType(attackType).mainAxis(mainAxis).damage((double) damage).range(bulletRange)
                .speed(bulletSpeed).bulletScaleModifier(bulletScaleModifier).splitSequence(splitSequence)
                .useExactSpawnPosition(useExactSpawnPosition).hitBlockPositions(hitBlockPositions)
                .inheritTrajectorySnapshot(inheritedTrajectorySnapshot);
    }

    public AttackContext copy() { return new AttackContext(this, true); }

    /** Stored by a live projectile after it has consumed the optional inheritance snapshot. */
    public AttackContext copyConfiguration() { return new AttackContext(this, false); }

    /** Intentionally exposes the mutable trigger collection, including after freeze.
     * Use copyTrigger() for isolation; freeze guards context setters, not this escape hatch.
     */
    public CompositeTrigger getTrigger() { return trigger; }
    public CompositeTrigger copyTrigger() { return trigger.copy(); }
    public void addSimpleTrigger(SimpleTrigger trigger) { if (ensureMutable("addSimpleTrigger")) this.trigger.add(Objects.requireNonNull(trigger, "trigger")); }
    public void addSimpleTriggers(List<SimpleTrigger> triggers) { if (ensureMutable("addSimpleTriggers")) this.trigger.addAll(Objects.requireNonNull(triggers, "triggers")); }

    public Vec3 getPos() { return pos; }
    public void setPos(Vec3 pos) { if (ensureMutable("setPos")) this.pos = Objects.requireNonNull(pos, "pos"); }
    public Vec3 getMainAxis() { return mainAxis; }
    public void setMainAxis(Vec3 mainAxis) { if (ensureMutable("setMainAxis")) this.mainAxis = normalizeMainAxis(mainAxis); }

    @NotNull public Entity getShooter() { return shooter; }
    @NotNull public LivingEntity getOwner() { return owner; }
    public ResourceLocation getColorRl() { return colorRl; }
    public void setColorRl(ResourceLocation colorRl) { if (ensureMutable("setColorRl")) this.colorRl = colorRl; }
    public Set<ResourceLocation> getVisualIds() { return visualIds; }
    public void setVisualIds(Set<ResourceLocation> visualIds) { if (ensureMutable("setVisualIds")) this.visualIds = visualIds == null ? Set.of() : Set.copyOf(visualIds); }
    public TrajectorySequence copyTrajectorySequence() { return trajectorySequence.copy(); }
    public void addTrajectoryModule(ResourceLocation moduleId, int stacks) {
        if (ensureMutable("addTrajectoryModule")) trajectorySequence.add(moduleId, stacks);
    }
    /** Returns an immutable ordered snapshot of the attached module stacks. */
    public List<TrajectorySpec> getTrajectorySpecs() {
        return trajectorySequence.snapshot();
    }
    @Nullable public TrajectoryRuntime.Snapshot getInheritedTrajectorySnapshot() { return inheritedTrajectorySnapshot; }
    /** Returns a defensive snapshot of block contacts inherited by a child attack. */
    public Set<BlockPos> getHitBlockPositions() { return hitBlockPositions; }
    public float getDamage() { return damage; }
    public double getBulletRange() { return bulletRange; }
    public double getBulletSpeed() { return bulletSpeed; }
    /** Definition of the concrete attack, bound before preparation or direct execution. */
    private AttackType attackType;
    @Nullable public AttackType getAttackType() { return attackType; }
    @Nullable public ResourceLocation getTypeId() { return attackType == null ? null : attackType.getId(); }
    @Nullable public ResourceLocation getRootTypeId() { return attackType == null ? null : attackType.getRootId(); }
    public boolean isLaserAttack() { return ModAttackTypes.LASER.getId().equals(getRootTypeId()); }
    public void setAttackType(AttackType type) {
        if (ensureMutable("setAttackType")) attackType = Objects.requireNonNull(type, "attackType");
    }
    /**
     * Binds the attack definition before preparation or direct execution, preserving finalized size.
     * Returns this context unchanged when the type already matches, including its frozen state.
     * Otherwise, updates a mutable context in place, or returns a mutable copy of a frozen context.
     * Callers must use the returned context. Existing modules are not replaced by this operation.
     */
    public AttackContext bindAttackTypeOrCopy(AttackType type) {
        Objects.requireNonNull(type, "attackType");
        if (attackType == type) return this;
        AttackContext boundContext = frozen ? copy() : this;
        boundContext.attackType = type;
        boundContext.frozen = false;
        return boundContext;
    }
    public double getBulletScaleModifier() { return bulletScaleModifier; }
    public double getBulletScale() { return bulletScale; }
    public void setBulletScale(double value, boolean useAsFinalScale) {
        if (Double.isFinite(value) && value > 0.0D) {
            if (!ensureMutable("setBulletScale")) return;
            if (useAsFinalScale) { bulletScale = sanitizeFinalBulletScale(value); }
            else { bulletScaleModifier = value; bulletScale = resolveBulletScale(damage, value); }
        }
    }

    /** Mutable module access, like getTrigger(); runtime consumers must copy this sequence. */
    @NotNull public SplitSequence getSplitSequence() { return splitSequence; }
    public SplitSequence copySplitSequence() { return splitSequence.copy(); }
    /** Adds split modules while the creation context is still mutable. */
    public void addSplitModule(@NotNull ResourceLocation moduleId, int stacks) {
        if (ensureMutable("addSplitModule")) splitSequence.add(moduleId, stacks);
    }
    public void useExactSpawnPosition() { if (ensureMutable("useExactSpawnPosition")) this.useExactSpawnPosition = true; }
    public boolean usesExactSpawnPosition() { return useExactSpawnPosition; }
    public boolean isFrozen() { return frozen; }
    /** Resolves trajectories and locks controlled setters; mutable trigger/split access remains explicit. */
    public void freeze() {
        if (frozen) return;
        trajectorySequence.resolve(getRootTypeId());
        frozen = true;
    }

    private boolean ensureMutable(String operation) {
        if (!frozen) return true;
        IsaacDisaster.LOGGER.warn("Ignored {} on frozen AttackContext", operation);
        return false;
    }

    private static Set<BlockPos> immutableBlockPositions(Set<BlockPos> source) {
        if (source == null || source.isEmpty()) return Set.of();
        HashSet<BlockPos> copy = new HashSet<>();
        for (BlockPos position : source) {
            if (position != null) copy.add(position.immutable());
        }
        return copy.isEmpty() ? Set.of() : Collections.unmodifiableSet(copy);
    }

    private static float resolveDamage(LivingEntity owner, Double requested) {
        if (requested != null && Double.isFinite(requested) && requested >= 0.0) return requested.floatValue();
        AttributeInstance attribute = owner.getAttribute(Attributes.ATTACK_DAMAGE);
        double value = attribute == null ? 1.0 : attribute.getValue();
        return Double.isFinite(value) && value >= 0.0 ? (float) value : 1.0f;
    }

    private static double sanitizeRange(double range) {
        if (!Double.isFinite(range) || range <= 0.0) return DEFAULT_RANGE;
        return Math.max(MIN_RANGE, Math.min(range, MAX_RANGE));
    }

    private static double sanitizeSpeed(double speed) {
        return !Double.isFinite(speed) || speed <= 0.0 ? DEFAULT_SPEED : speed;
    }

    private static double resolveBulletScaleModifier(LivingEntity owner, @Nullable Double requested) {
        if (requested != null) return requested;
        AttributeInstance attribute = owner.getAttribute(ModAttributes.BULLET_SCALE.get());
        return attribute == null ? 0.0D : attribute.getValue();
    }

    private static double resolveBulletScale(double damage, double modifier) {
        if (!Double.isFinite(damage) || !Double.isFinite(modifier)) return MIN_BULLET_SCALE;
        double value = Math.sqrt(Math.max(0.0D, damage) / BULLET_SCALE_BASE_DAMAGE) * (1.0D + modifier);
        return sanitizeFinalBulletScale(value);
    }

    private static double sanitizeFinalBulletScale(double value) {
        return Double.isFinite(value) && value >= MIN_BULLET_SCALE ? value : MIN_BULLET_SCALE;
    }

    private static Vec3 normalizeMainAxis(Vec3 direction) {
        Objects.requireNonNull(direction, "direction");
        if (!Double.isFinite(direction.x) || !Double.isFinite(direction.y)
                || !Double.isFinite(direction.z) || direction.lengthSqr() < 1.0E-8) {
            throw new IllegalArgumentException("mainAxis must be finite and non-zero");
        }
        return direction.normalize();
    }

    @Override public String toString() {
        return "AttackContext{damage=" + damage + ", range=" + bulletRange + ", speed=" + bulletSpeed + '}';
    }

    private AttackContext(AttackContext source, boolean includeInheritance) {
        this.owner = source.owner;
        this.shooter = source.shooter;
        this.colorRl = source.colorRl;
        this.visualIds = Set.copyOf(source.visualIds);
        this.trigger = source.trigger.copy();
        this.trajectorySequence = source.trajectorySequence.copy();
        this.pos = source.pos;
        this.mainAxis = source.mainAxis;
        this.damage = source.damage;
        this.bulletScaleModifier = source.bulletScaleModifier;
        this.bulletScale = source.bulletScale;
        this.bulletRange = source.bulletRange;
        this.bulletSpeed = source.bulletSpeed;
        this.splitSequence = source.splitSequence.copy();
        this.useExactSpawnPosition = source.useExactSpawnPosition;
        this.hitBlockPositions = immutableBlockPositions(source.hitBlockPositions);
        this.inheritedTrajectorySnapshot = includeInheritance ? source.inheritedTrajectorySnapshot : null;
        this.frozen = source.frozen;
        this.attackType = source.attackType;
    }

    private AttackContext(AttackContext source, Builder builder) {
        this.owner = source.owner;
        this.shooter = source.shooter;
        this.colorRl = builder.colorRl;
        this.visualIds = Set.copyOf(builder.visualIds);
        this.trigger = builder.trigger == null ? new CompositeTrigger() : builder.trigger.copy();
        this.trajectorySequence = builder.trajectorySequence.copy();
        this.pos = Objects.requireNonNull(builder.position, "position");
        this.mainAxis = builder.direction != null ? normalizeMainAxis(builder.direction)
                : normalizeMainAxis(Objects.requireNonNull(builder.mainAxis, "mainAxis"));
        this.damage = Objects.requireNonNull(builder.damage, "damage").floatValue();
        this.bulletScaleModifier = builder.bulletScaleModifier == null
                ? source.bulletScaleModifier : resolveBulletScaleModifier(source.owner, builder.bulletScaleModifier);
        this.bulletScale = resolveBulletScale(damage, bulletScaleModifier);
        this.bulletRange = sanitizeRange(builder.range);
        this.bulletSpeed = sanitizeSpeed(builder.speed);
        this.splitSequence = builder.splitSequence == null ? new SplitSequence() : builder.splitSequence.copy();
        this.useExactSpawnPosition = builder.useExactSpawnPosition;
        this.hitBlockPositions = immutableBlockPositions(builder.hitBlockPositions);
        this.inheritedTrajectorySnapshot = builder.inheritedTrajectorySnapshot;
        this.frozen = false;
        this.attackType = builder.attackType;
    }

    public static final class Builder {
        private final LivingEntity owner;
        private final Entity shooter;
        private AttackContext source;
        private ResourceLocation colorRl;
        private Set<ResourceLocation> visualIds = Set.of();
        private CompositeTrigger trigger;
        private TrajectorySequence trajectorySequence = new TrajectorySequence();
        private Vec3 position;
        private Vec3 mainAxis;
        private Vec3 direction;
        private Double damage;
        private Double bulletScaleModifier;
        private double range = DEFAULT_RANGE;
        private double speed = DEFAULT_SPEED;
        private SplitSequence splitSequence;
        private boolean useExactSpawnPosition;
        private Set<BlockPos> hitBlockPositions = Set.of();
        private AttackType attackType;
        private TrajectoryRuntime.Snapshot inheritedTrajectorySnapshot;

        private Builder(@NotNull LivingEntity owner, @Nullable Entity shooter) {
            this.owner = Objects.requireNonNull(owner, "owner");
            this.shooter = shooter;
        }

        private Builder(AttackContext source) { this.owner = null; this.shooter = null; this.source = source; }
        private static Builder from(AttackContext source) { return new Builder(source); }

        public Builder color(ResourceLocation colorRl) { this.colorRl = colorRl; return this; }
        public Builder visuals(Set<ResourceLocation> visualIds) { this.visualIds = visualIds == null ? Set.of() : Set.copyOf(visualIds); return this; }
        public Builder trigger(CompositeTrigger trigger) { this.trigger = trigger; return this; }
        public Builder trajectorySequence(TrajectorySequence sequence) {
            this.trajectorySequence = Objects.requireNonNull(sequence, "sequence").copy(); return this;
        }
        /** Supplies block contacts inherited by a split child. */
        public Builder hitBlockPositions(Set<BlockPos> positions) {
            if (positions == null || positions.isEmpty()) {
                this.hitBlockPositions = Set.of();
            } else {
                java.util.HashSet<BlockPos> copy = new java.util.HashSet<>();
                for (BlockPos position : positions) {
                    if (position != null) copy.add(position.immutable());
                }
                this.hitBlockPositions = copy.isEmpty() ? Set.of() : Set.copyOf(copy);
            }
            return this;
        }
        public Builder attackType(AttackType value) { this.attackType = value; return this; }
        /** Supplies a parent runtime snapshot for child initialization; null starts a fresh trajectory. */
        public Builder inheritTrajectorySnapshot(@Nullable TrajectoryRuntime.Snapshot snapshot) {
            this.inheritedTrajectorySnapshot = snapshot; return this;
        }
        public Builder position(Vec3 position) { this.position = Objects.requireNonNull(position, "position"); return this; }
        public Builder mainAxis(Vec3 mainAxis) {
            this.mainAxis = Objects.requireNonNull(mainAxis, "mainAxis"); this.direction = null; return this;
        }
        public Builder direction(Vec3 direction) {
            this.direction = Objects.requireNonNull(direction, "direction"); this.mainAxis = null; return this;
        }
        public Builder damage(@Nullable Double damage) { this.damage = damage; return this; }
        public Builder bulletScaleModifier(@Nullable Double modifier) { this.bulletScaleModifier = modifier; return this; }
        public Builder range(double range) { this.range = range; return this; }
        public Builder speed(double speed) { this.speed = speed; return this; }
        public Builder splitSequence(SplitSequence splitSequence) { this.splitSequence = splitSequence; return this; }
        public Builder useExactSpawnPosition() { this.useExactSpawnPosition = true; return this; }
        private Builder useExactSpawnPosition(boolean value) { this.useExactSpawnPosition = value; return this; }
        public AttackContext build() { return source == null ? new AttackContext(this) : new AttackContext(source, this); }
    }
}
