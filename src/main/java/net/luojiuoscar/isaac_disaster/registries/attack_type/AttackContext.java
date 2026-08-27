package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.helper.GeometryHelper;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitSequence;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Creation-time data used to construct one attack object. */
public class AttackContext {
    public static final double DEFAULT_RANGE = 18.0;
    public static final double DEFAULT_SPEED = 1.0;
    public static final double MIN_RANGE = 1.0;
    public static final double MAX_RANGE = 64.0;

    private ResourceLocation colorRl;
    private final CompositeTrigger trigger;
    private final Map<ResourceLocation, Integer> trajectories;
    private Vec3 pos;
    private Vec3 mainAxis;
    private final float damage;
    private final double bulletRange;
    private final double bulletSpeed;
    private SplitSequence splitSequence;
    private boolean useExactSpawnPosition;
    private final Entity shooter;
    private final LivingEntity owner;

    private AttackContext(Builder builder) {
        this.owner = Objects.requireNonNull(builder.owner, "owner");
        this.shooter = builder.shooter == null ? owner : builder.shooter;
        this.colorRl = builder.colorRl;
        this.trigger = builder.trigger == null ? new CompositeTrigger() : builder.trigger.copy();
        this.trajectories = immutableMap(builder.trajectories);
        this.pos = builder.position == null ? owner.position() : builder.position;
        if (builder.direction != null) {
            this.mainAxis = normalizeMainAxis(builder.direction);
        } else {
            this.mainAxis = builder.mainAxis == null
                    ? GeometryHelper.mainAxisFromRotation(owner.getXRot(), owner.getYRot())
                    : normalizeMainAxis(builder.mainAxis);
        }
        this.damage = resolveDamage(owner, builder.damage);
        this.bulletRange = sanitizeRange(builder.range);
        this.bulletSpeed = sanitizeSpeed(builder.speed);
        this.splitSequence = builder.splitSequence == null ? new SplitSequence() : builder.splitSequence.copy();
        this.useExactSpawnPosition = builder.useExactSpawnPosition;
    }

    public static Builder builder(@NotNull LivingEntity owner, @Nullable Entity shooter) {
        return new Builder(owner, shooter);
    }

    public Builder toBuilder() {
        return builder(owner, shooter).color(colorRl).trigger(trigger).trajectories(trajectories)
                .position(pos).mainAxis(mainAxis).damage((double) damage).range(bulletRange)
                .speed(bulletSpeed).splitSequence(splitSequence).useExactSpawnPosition(useExactSpawnPosition);
    }

    public AttackContext copy() { return new AttackContext(this); }

    public CompositeTrigger getTrigger() { return trigger; }
    public CompositeTrigger copyTrigger() { return trigger.copy(); }
    public void addSimpleTrigger(SimpleTrigger trigger) { this.trigger.add(Objects.requireNonNull(trigger, "trigger")); }
    public void addSimpleTriggers(List<SimpleTrigger> triggers) { this.trigger.addAll(Objects.requireNonNull(triggers, "triggers")); }

    public Vec3 getPos() { return pos; }
    public void setPos(Vec3 pos) { this.pos = Objects.requireNonNull(pos, "pos"); }
    public Vec3 getMainAxis() { return mainAxis; }
    public void setMainAxis(Vec3 mainAxis) { this.mainAxis = normalizeMainAxis(mainAxis); }

    @NotNull public Entity getShooter() { return shooter; }
    @NotNull public LivingEntity getOwner() { return owner; }
    public ResourceLocation getColorRl() { return colorRl; }
    public void setColorRl(ResourceLocation colorRl) { this.colorRl = colorRl; }
    public Map<ResourceLocation, Integer> getTrajectories() { return trajectories; }
    public float getDamage() { return damage; }
    public double getBulletRange() { return bulletRange; }
    public double getBulletSpeed() { return bulletSpeed; }

    /** Returns this context's split sequence, which is always initialized. */
    @NotNull public SplitSequence getSplitSequence() { return splitSequence; }
    public void useExactSpawnPosition() { this.useExactSpawnPosition = true; }
    public boolean usesExactSpawnPosition() { return useExactSpawnPosition; }

    private static Map<ResourceLocation, Integer> immutableMap(Map<ResourceLocation, Integer> source) {
        if (source == null || source.isEmpty()) return Map.of();
        return Collections.unmodifiableMap(new HashMap<>(source));
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

    private AttackContext(AttackContext source) {
        this.owner = source.owner;
        this.shooter = source.shooter;
        this.colorRl = source.colorRl;
        this.trigger = source.trigger.copy();
        this.trajectories = immutableMap(source.trajectories);
        this.pos = source.pos;
        this.mainAxis = source.mainAxis;
        this.damage = source.damage;
        this.bulletRange = source.bulletRange;
        this.bulletSpeed = source.bulletSpeed;
        this.splitSequence = source.splitSequence.copy();
        this.useExactSpawnPosition = source.useExactSpawnPosition;
    }

    public static final class Builder {
        private final LivingEntity owner;
        private final Entity shooter;
        private ResourceLocation colorRl;
        private CompositeTrigger trigger;
        private Map<ResourceLocation, Integer> trajectories = Map.of();
        private Vec3 position;
        private Vec3 mainAxis;
        private Vec3 direction;
        private Double damage;
        private double range = DEFAULT_RANGE;
        private double speed = DEFAULT_SPEED;
        private SplitSequence splitSequence;
        private boolean useExactSpawnPosition;

        private Builder(@NotNull LivingEntity owner, @Nullable Entity shooter) {
            this.owner = Objects.requireNonNull(owner, "owner");
            this.shooter = shooter;
        }

        public Builder color(ResourceLocation colorRl) { this.colorRl = colorRl; return this; }
        public Builder trigger(CompositeTrigger trigger) { this.trigger = trigger; return this; }
        public Builder trajectories(Map<ResourceLocation, Integer> trajectories) {
            this.trajectories = trajectories == null ? Map.of() : trajectories; return this;
        }
        public Builder position(Vec3 position) { this.position = Objects.requireNonNull(position, "position"); return this; }
        public Builder mainAxis(Vec3 mainAxis) {
            this.mainAxis = Objects.requireNonNull(mainAxis, "mainAxis"); this.direction = null; return this;
        }
        public Builder direction(Vec3 direction) {
            this.direction = Objects.requireNonNull(direction, "direction"); this.mainAxis = null; return this;
        }
        public Builder damage(@Nullable Double damage) { this.damage = damage; return this; }
        public Builder range(double range) { this.range = range; return this; }
        public Builder speed(double speed) { this.speed = speed; return this; }
        public Builder splitSequence(SplitSequence splitSequence) { this.splitSequence = splitSequence; return this; }
        public Builder useExactSpawnPosition() { this.useExactSpawnPosition = true; return this; }
        private Builder useExactSpawnPosition(boolean value) { this.useExactSpawnPosition = value; return this; }
        public AttackContext build() { return new AttackContext(this); }
    }
}
