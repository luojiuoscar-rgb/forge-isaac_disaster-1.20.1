package net.luojiuoscar.isaac_disaster.bullet.core;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** Mutable base kinematics and lifecycle counters for one projectile. */
final class BulletMotionState {
    static final double MIN_VALID_SPEED = 0.1D;
    private static final double MIN_VALID_SPEED_SQUARED = MIN_VALID_SPEED * MIN_VALID_SPEED;

    private Vec3 position;
    private Vec3 previousPosition;
    private Vec3 velocity;
    private Vec3 acceleration;
    private final double baseSpeed;
    private int age;
    private final int lifetime;
    private double traveled;
    private final double range;
    private boolean alive = true;

    BulletMotionState(
        Vec3 position,
        Vec3 velocity,
        Vec3 acceleration,
        double baseSpeed,
        int lifetime,
        double range) {
        this.position = position;
        this.previousPosition = position;
        this.velocity = velocity;
        this.acceleration = acceleration;
        this.baseSpeed = baseSpeed > 0.0D ? baseSpeed : velocity.length();
        this.lifetime = Math.max(1, lifetime);
        this.range = Math.max(0.0D, range);
    }

    boolean tickPhysics(boolean rangeLimited) {
        if (!alive) return false;
        age++;
        if (rangeLimited ? traveled >= range : age >= lifetime) {
            alive = false;
            return false;
        }
        previousPosition = position;
        if (!replaceVelocity(velocity.add(acceleration))) return false;
        position = position.add(velocity);
        traveled += velocity.length();
        return alive;
    }

    boolean advanceTrajectory(
        Vec3 nextPosition,
        Vec3 nextVelocity,
        Vec3 nextAcceleration,
        double deltaTicks,
        boolean rangeLimited) {
        if (!alive) return false;
        age++;
        if (rangeLimited ? traveled >= range : age >= lifetime) {
            alive = false;
            return false;
        }
        Vec3 start = position;
        previousPosition = start;
        Vec3 candidateVelocity = nextVelocity == null ? velocity : nextVelocity;
        Vec3 candidatePosition =
            nextPosition == null ? start.add(candidateVelocity.scale(deltaTicks)) : nextPosition;
        // Trajectory modules may intentionally slow a bullet while it still advances. Only a
        // low-speed state with no position progress is considered the temporary stall failure.
        boolean moved = candidatePosition.distanceToSqr(start) > 1.0E-16D;
        if (!replaceVelocity(candidateVelocity, moved)) return false;
        position = candidatePosition;
        acceleration = nextAcceleration == null ? Vec3.ZERO : nextAcceleration;
        traveled += position.distanceTo(start);
        return true;
    }

    Vec3 position() {
        return position;
    }

    Vec3 previousPosition() {
        return previousPosition;
    }

    Vec3 velocity() {
        return velocity;
    }

    Vec3 acceleration() {
        return acceleration;
    }

    double baseSpeed() {
        return baseSpeed;
    }

    int age() {
        return age;
    }

    int lifetime() {
        return lifetime;
    }

    double traveled() {
        return traveled;
    }

    void setTraveled(double value) {
        traveled = value;
    }

    double range() {
        return range;
    }

    boolean isAlive() {
        return alive;
    }

    void kill() {
        alive = false;
    }

    void setPosition(Vec3 value) {
        position = value;
    }

    void setCenter(Vec3 center) {
        if (center != null) position = center;
    }

    void pushPosition(Vec3 delta) {
        position = position.add(delta);
        previousPosition = position;
    }

    void setVelocity(@Nullable Vec3 value) {
        replaceVelocity(value);
    }

    boolean validateRuntimeVelocity() {
        return validateRuntimeVelocity(false);
    }

    private boolean validateRuntimeVelocity(boolean allowLowSpeedWhenMoving) {
        if (!alive || velocity == null) return false;
        double x = velocity.x;
        double y = velocity.y;
        double z = velocity.z;
        if (!Double.isFinite(x)
            || !Double.isFinite(y)
            || !Double.isFinite(z)
            || !allowLowSpeedWhenMoving && velocity.lengthSqr() < MIN_VALID_SPEED_SQUARED) {
            alive = false;
            return false;
        }
        return true;
    }

    boolean replaceVelocity(@Nullable Vec3 value) {
        return replaceVelocity(value, false);
    }

    boolean replaceVelocity(@Nullable Vec3 value, boolean allowLowSpeedWhenMoving) {
        velocity = value == null ? Vec3.ZERO : value;
        return validateRuntimeVelocity(allowLowSpeedWhenMoving);
    }

    void setAcceleration(Vec3 value) {
        acceleration = value;
    }

    void restoreSnapshot(Vec3 previous, int snapshotAge, double snapshotTraveled) {
        previousPosition = previous == null ? position : previous;
        age = Math.max(0, snapshotAge);
        traveled = Math.max(0.0D, snapshotTraveled);
    }

    void applyCorrection(
        Vec3 authoritativePosition, @Nullable Vec3 authoritativeVelocity, double blend) {
        double factor = Math.max(0.0, Math.min(1.0, blend));
        Vec3 target = authoritativePosition == null ? position : authoritativePosition;
        previousPosition = position.lerp(target, factor);
        position = target;
        boolean moved = target.distanceToSqr(previousPosition) > 1.0E-16D;
        if (authoritativeVelocity != null) replaceVelocity(authoritativeVelocity, moved);
        else validateRuntimeVelocity();
    }
}
