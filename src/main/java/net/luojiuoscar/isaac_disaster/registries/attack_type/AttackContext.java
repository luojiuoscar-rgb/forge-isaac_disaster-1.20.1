package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
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
    private float xRot;
    private float yRot;
    private float xRotOffset;
    private float yRotOffset;
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
            float[] rotation = rotationFromDirection(builder.direction);
            this.xRot = rotation[0];
            this.yRot = rotation[1];
        } else {
            this.xRot = builder.xRot == null ? owner.getXRot() : builder.xRot;
            this.yRot = builder.yRot == null ? owner.getYRot() : builder.yRot;
        }
        this.damage = resolveDamage(owner, builder.damage);
        this.bulletRange = sanitizeRange(builder.range);
        this.bulletSpeed = sanitizeSpeed(builder.speed);
        this.splitSequence = builder.splitSequence == null ? new SplitSequence() : builder.splitSequence.copy();
        this.useExactSpawnPosition = builder.useExactSpawnPosition;
        this.xRotOffset = builder.xRotOffset;
        this.yRotOffset = builder.yRotOffset;
    }

    public static Builder builder(@NotNull LivingEntity owner, @Nullable Entity shooter) {
        return new Builder(owner, shooter);
    }

    public Builder toBuilder() {
        return builder(owner, shooter).color(colorRl).trigger(trigger).trajectories(trajectories)
                .position(pos).rotation(xRot, yRot).damage((double) damage).range(bulletRange)
                .speed(bulletSpeed).splitSequence(splitSequence).useExactSpawnPosition(useExactSpawnPosition)
                .rotationOffsets(xRotOffset, yRotOffset);
    }

    public AttackContext copy() { return toBuilder().build(); }

    public CompositeTrigger getTrigger() { return trigger; }
    public CompositeTrigger copyTrigger() { return trigger.copy(); }
    public void addSimpleTrigger(SimpleTrigger trigger) { this.trigger.add(Objects.requireNonNull(trigger, "trigger")); }
    public void addSimpleTriggers(List<SimpleTrigger> triggers) { this.trigger.addAll(Objects.requireNonNull(triggers, "triggers")); }

    public Vec3 getPos() { return pos; }
    public void setPos(Vec3 pos) { this.pos = Objects.requireNonNull(pos, "pos"); }
    public float getXRot() { return xRot + xRotOffset; }
    public void setXRot(float xRot) { this.xRot = xRot; }
    public float getYRot() { return yRot + yRotOffset; }
    public void setYRot(float yRot) { this.yRot = yRot; }

    /** Sets the absolute firing direction and clears relative rotation offsets. */
    public void setDirection(Vec3 direction) {
        float[] rotation = rotationFromDirection(direction);
        this.xRot = rotation[0];
        this.yRot = rotation[1];
        this.xRotOffset = 0.0f;
        this.yRotOffset = 0.0f;
    }

    @NotNull public Entity getShooter() { return shooter; }
    @NotNull public LivingEntity getOwner() { return owner; }
    public void setXRotOffset(float xRotOffset) { this.xRotOffset = xRotOffset; }
    public void setYRotOffset(float yRotOffset) { this.yRotOffset = yRotOffset; }
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

    private static float[] rotationFromDirection(Vec3 direction) {
        Objects.requireNonNull(direction, "direction");
        if (!Double.isFinite(direction.x) || !Double.isFinite(direction.y)
                || !Double.isFinite(direction.z) || direction.lengthSqr() < 1.0E-8) {
            throw new IllegalArgumentException("direction must be finite and non-zero");
        }
        Vec3 normalized = direction.normalize();
        return new float[]{(float) Math.toDegrees(Math.asin(-normalized.y)),
                (float) Math.toDegrees(Math.atan2(-normalized.x, normalized.z))};
    }

    @Override public String toString() {
        return "AttackContext{damage=" + damage + ", range=" + bulletRange + ", speed=" + bulletSpeed + '}';
    }

    public static final class Builder {
        private final LivingEntity owner;
        private final Entity shooter;
        private ResourceLocation colorRl;
        private CompositeTrigger trigger;
        private Map<ResourceLocation, Integer> trajectories = Map.of();
        private Vec3 position;
        private Float xRot;
        private Float yRot;
        private Vec3 direction;
        private Double damage;
        private double range = DEFAULT_RANGE;
        private double speed = DEFAULT_SPEED;
        private SplitSequence splitSequence;
        private boolean useExactSpawnPosition;
        private float xRotOffset;
        private float yRotOffset;

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
        public Builder rotation(float xRot, float yRot) {
            this.xRot = xRot; this.yRot = yRot; this.direction = null; return this;
        }
        public Builder direction(Vec3 direction) {
            this.direction = Objects.requireNonNull(direction, "direction"); this.xRot = null; this.yRot = null; return this;
        }
        public Builder damage(@Nullable Double damage) { this.damage = damage; return this; }
        public Builder range(double range) { this.range = range; return this; }
        public Builder speed(double speed) { this.speed = speed; return this; }
        public Builder splitSequence(SplitSequence splitSequence) { this.splitSequence = splitSequence; return this; }
        public Builder useExactSpawnPosition() { this.useExactSpawnPosition = true; return this; }
        private Builder useExactSpawnPosition(boolean value) { this.useExactSpawnPosition = value; return this; }
        private Builder rotationOffsets(float xRotOffset, float yRotOffset) {
            this.xRotOffset = xRotOffset; this.yRotOffset = yRotOffset; return this;
        }
        public AttackContext build() { return new AttackContext(this); }
    }
}
