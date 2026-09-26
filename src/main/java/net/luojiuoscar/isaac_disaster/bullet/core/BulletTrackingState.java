package net.luojiuoscar.isaac_disaster.bullet.core;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** Mutable steering memory and bounded velocity operations for one bullet. */
public final class BulletTrackingState {
    private final boolean homing;
    private final boolean controllable;
    private final boolean rememberHitTargets;
    private final BulletSteeringMode steeringMode;
    private final double homingRange;
    private final double homingSteer;
    private final double maxSteeringAcceleration;
    private final double maxSpeedChange;
    private final double controlRange;
    private final double controlSteer;

    private Vec3 desiredVelocity;
    private Vec3 steeringAcceleration = Vec3.ZERO;
    private LivingEntity trackingTarget;
    private int targetSearchCooldown;
    private int trackingHandle;
    private Vec3 trackingPosition;
    private boolean trackingUsesControl;

    public BulletTrackingState(
        boolean homing,
        boolean controllable,
        boolean rememberHitTargets,
        BulletSteeringMode steeringMode,
        double homingRange,
        double homingSteer,
        double maxSteeringAcceleration,
        double maxSpeedChange,
        double controlRange,
        double controlSteer) {
        this.homing = homing;
        this.controllable = controllable;
        this.rememberHitTargets = rememberHitTargets;
        this.steeringMode = steeringMode;
        this.homingRange = homingRange;
        this.homingSteer = homingSteer;
        this.maxSteeringAcceleration = maxSteeringAcceleration;
        this.maxSpeedChange = maxSpeedChange;
        this.controlRange = controlRange;
        this.controlSteer = controlSteer;
    }

    public boolean homing() {
        return homing;
    }

    public boolean controllable() {
        return controllable;
    }

    public boolean rememberHitTargets() {
        return rememberHitTargets;
    }

    public BulletSteeringMode steeringMode() {
        return steeringMode;
    }

    public double homingRange() {
        return homingRange;
    }

    public double homingSteer() {
        return homingSteer;
    }

    public double maxSteeringAcceleration() {
        return maxSteeringAcceleration;
    }

    public double maxSpeedChange() {
        return maxSpeedChange;
    }

    public double controlRange() {
        return controlRange;
    }

    public double controlSteer() {
        return controlSteer;
    }

    @Nullable
    public LivingEntity trackingTarget() {
        return trackingTarget;
    }

    public void trackingTarget(@Nullable LivingEntity target) {
        trackingTarget = target;
    }

    public void tickTargetSearchCooldown() {
        if (targetSearchCooldown > 0) targetSearchCooldown--;
    }

    public boolean canSearchTrackingTarget() {
        return targetSearchCooldown == 0;
    }

    public void setTargetSearchCooldown(int ticks) {
        targetSearchCooldown = Math.max(0, ticks);
    }

    public void setSteeringTarget(@Nullable Vec3 value, boolean usesControl) {
        trackingPosition = value;
        trackingUsesControl = usesControl;
    }

    @Nullable
    public Vec3 trackingPosition() {
        return trackingPosition;
    }

    public boolean trackingUsesControl() {
        return trackingUsesControl;
    }

    public void setTrackingHandle(int value) {
        trackingHandle = Math.max(0, value);
    }

    public int trackingHandle() {
        return trackingHandle;
    }

    @Nullable
    public Vec3 desiredVelocity() {
        return desiredVelocity;
    }

    public void setDesiredVelocity(@Nullable Vec3 value) {
        desiredVelocity = value;
    }

    public Vec3 steeringAcceleration() {
        return steeringAcceleration;
    }

    /** Applies the configured steering mode and returns the resulting velocity. */
    public Vec3 applySteering(Vec3 velocity) {
        if (steeringMode == BulletSteeringMode.DIRECT) {
            if (desiredVelocity == null) {
                steeringAcceleration = Vec3.ZERO;
                return velocity;
            }
            steeringAcceleration = desiredVelocity.subtract(velocity);
            return desiredVelocity;
        }
        if (desiredVelocity == null
            || velocity.lengthSqr() < 1.0E-12D
            || desiredVelocity.lengthSqr() < 1.0E-12D) {
            steeringAcceleration = Vec3.ZERO;
            return velocity;
        }
        double currentSpeed = velocity.length();
        double desiredSpeed = desiredVelocity.length();
        double limitedSpeed = approach(currentSpeed, desiredSpeed, maxSpeedChange);
        Vec3 limitedTarget = desiredVelocity.normalize().scale(limitedSpeed);
        Vec3 delta = limitedTarget.subtract(velocity);
        if (delta.lengthSqr() > maxSteeringAcceleration * maxSteeringAcceleration) {
            delta = delta.normalize().scale(maxSteeringAcceleration);
        }
        steeringAcceleration = delta;
        return velocity.add(delta);
    }

    /** Points the velocity at a target and returns the new velocity. */
    public Vec3 directSteering(Vec3 position, Vec3 targetPosition, double speed) {
        Vec3 toTarget = targetPosition.subtract(position);
        if (toTarget.lengthSqr() < 1.0E-8D) return null;
        steeringAcceleration = Vec3.ZERO;
        return toTarget.normalize().scale(Math.max(0.0D, speed));
    }

    /** Applies bounded speed and heading correction from a server sample. */
    public Vec3 velocityCorrection(Vec3 velocity, @Nullable Vec3 authoritativeVelocity) {
        if (authoritativeVelocity == null) return velocity;
        double authoritativeSpeed = authoritativeVelocity.length();
        double currentSpeed = velocity.length();
        if (authoritativeSpeed < 1.0E-8D) {
            if (currentSpeed > maxSpeedChange)
                return velocity.normalize().scale(currentSpeed - maxSpeedChange);
            return velocity;
        }
        double speedDelta = authoritativeSpeed - currentSpeed;
        Vec3 corrected = velocity;
        if (Math.abs(speedDelta) > 0.01D) {
            double limitedDelta = Math.max(-maxSpeedChange, Math.min(maxSpeedChange, speedDelta)) * 0.5D;
            double correctedSpeed = Math.max(0.0D, currentSpeed + limitedDelta);
            corrected =
                velocity.lengthSqr() < 1.0E-12D
                    ? authoritativeVelocity.normalize().scale(correctedSpeed)
                    : velocity.normalize().scale(correctedSpeed);
        }
        if (corrected.lengthSqr() > 1.0E-12D) {
            Vec3 currentDirection = corrected.normalize();
            Vec3 authorityDirection = authoritativeVelocity.normalize();
            if (currentDirection.dot(authorityDirection) < 0.999D) {
                corrected = currentDirection.lerp(authorityDirection, 0.08D).normalize()
                    .scale(corrected.length());
            }
        }
        return corrected;
    }

    /** Applies a weak homing turn while preserving the current speed. */
    public Vec3 steerTowards(Vec3 position, Vec3 velocity, Vec3 targetPosition) {
        Vec3 toTarget = targetPosition.subtract(position);
        if (toTarget.lengthSqr() < 1.0E-8D || velocity.lengthSqr() < 1.0E-8D) return velocity;
        Vec3 current = velocity.normalize();
        double strength = homingSteer * Math.min(1.0D, toTarget.length());
        return current
            .add(toTarget.normalize().subtract(current).scale(strength))
            .normalize()
            .scale(velocity.length());
    }

    private static double approach(double current, double target, double limit) {
        return current < target ? Math.min(current + limit, target) : Math.max(current - limit, target);
    }
}
