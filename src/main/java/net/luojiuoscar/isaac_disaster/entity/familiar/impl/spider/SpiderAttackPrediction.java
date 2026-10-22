package net.luojiuoscar.isaac_disaster.entity.familiar.impl.spider;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

/** Bounded horizontal interception and the lifetime of one straight attack attempt. */
final class SpiderAttackPrediction {
    static final double MAX_LEAD_DISTANCE = 3;
    static final double MAX_PREDICTION_TICKS = 16;
    private static final double MIN_SPEED_SQR = 0.02 * 0.02;
    private final double timeScale;
    private final double sideBias;
    boolean calibrated;
    private Vec3 launchTarget, launchVelocity, launchGoal, launchHeading;
    private long launchTick;

    SpiderAttackPrediction(RandomSource random) {
        this(0.85 + random.nextDouble() * 0.3, random.nextDouble() * 2 - 1);
    }

    SpiderAttackPrediction(double timeScale, double sideBias) {
        this.timeScale = timeScale;
        this.sideBias = sideBias;
    }

    Vec3 goal(Vec3 from, Vec3 target, Vec3 observedVelocity, double turnTicks, double contactRadius) {
        Vec3 velocity = horizontal(observedVelocity);
        double speed = velocity.horizontalDistance();
        double distance = target.subtract(from).horizontalDistance();
        if (speed < 0.02 || distance <= contactRadius) return target;
        double turning = Mth.clamp(turnTicks, 0, 8);
        Vec3 relative = horizontal(target.subtract(from)).add(velocity.scale(turning));
        double travel = interceptionTime(relative, velocity);
        double prediction = Mth.clamp((turning + travel) * timeScale, 0, MAX_PREDICTION_TICKS);
        double fade = Mth.clamp((distance - contactRadius) / 1.5, 0, 1);
        double lead = Math.min(MAX_LEAD_DISTANCE, speed * prediction) * fade;
        double spread = Math.min(0.25, Math.max(0, contactRadius) * 0.6) * sideBias * fade;
        return target.add(velocity.scale(lead / speed))
                .add(-velocity.z / speed * spread, 0, velocity.x / speed * spread);
    }

    private static double interceptionTime(Vec3 relative, Vec3 velocity) {
        double a = velocity.horizontalDistanceSqr() - SpiderMovement.MAX_SPEED * SpiderMovement.MAX_SPEED;
        double b = 2 * relative.dot(velocity);
        double c = relative.horizontalDistanceSqr();
        double fallback = Math.sqrt(c) / SpiderMovement.MAX_SPEED;
        if (Math.abs(a) < 1e-9) return b < -1e-9 ? -c / b : fallback;
        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0) return fallback;
        double first = (-b - Math.sqrt(discriminant)) / (2 * a);
        double second = (-b + Math.sqrt(discriminant)) / (2 * a);
        double time = first >= 0 ? first : Double.POSITIVE_INFINITY;
        if (second >= 0) time = Math.min(time, second);
        return Double.isFinite(time) ? time : fallback;
    }

    void depart(Vec3 from, Vec3 goal, Vec3 target, Vec3 velocity, long tick) {
        launchTarget = target;
        launchVelocity = horizontal(velocity);
        launchGoal = goal;
        launchHeading = horizontal(goal.subtract(from)).normalize();
        launchTick = tick;
    }

    boolean passedGoal(Vec3 position) {
        return launchGoal != null && horizontal(launchGoal.subtract(position)).dot(launchHeading) <= 1e-8;
    }

    boolean obsolete(Vec3 position, Vec3 target, Vec3 observedVelocity, long tick, double contactRadius) {
        if (launchTarget == null) return false;
        if (passedGoal(position)) return true;
        double imminent = SpiderMovement.MAX_SPEED + contactRadius;
        if (target.subtract(position).horizontalDistanceSqr() <= imminent * imminent) return false;
        Vec3 velocity = horizontal(observedVelocity);
        double oldSpeed = launchVelocity.horizontalDistanceSqr();
        double newSpeed = velocity.horizontalDistanceSqr();
        if (oldSpeed >= MIN_SPEED_SQR && newSpeed >= MIN_SPEED_SQR
                && launchVelocity.dot(velocity) < 0.5 * Math.sqrt(oldSpeed * newSpeed)) return true;
        Vec3 expected = launchTarget.add(launchVelocity.scale(Math.max(0, tick - launchTick)));
        double tolerance = Math.max(1, contactRadius * 2);
        return target.subtract(expected).horizontalDistanceSqr() > tolerance * tolerance;
    }

    private static Vec3 horizontal(Vec3 value) { return new Vec3(value.x, 0, value.z); }

    /** One owner-shared observation per target per tick; entity velocity flags are not consulted. */
    static final class Motion {
        private Vec3 position;
        private Vec3 velocity = Vec3.ZERO;
        private long tick = Long.MIN_VALUE;

        void sample(Vec3 current, long gameTick) {
            if (gameTick == tick) return;
            long elapsed = gameTick - tick;
            if (position == null || gameTick < tick || elapsed > 4) {
                velocity = Vec3.ZERO;
            } else {
                Vec3 delta = current.subtract(position);
                double distance = delta.lengthSqr();
                Vec3 measured = horizontal(delta).scale(1.0 / elapsed);
                // Teleports and stale snapshots must not create a huge predicted lead.
                if (!Double.isFinite(distance) || distance > 4.0 * elapsed * elapsed
                        || measured.horizontalDistanceSqr() < MIN_SPEED_SQR) velocity = Vec3.ZERO;
                else if (velocity.horizontalDistanceSqr() >= MIN_SPEED_SQR
                        && velocity.dot(measured) > 0.5 * Math.sqrt(velocity.horizontalDistanceSqr() * measured.horizontalDistanceSqr())) {
                    velocity = velocity.scale(0.5).add(measured.scale(0.5));
                } else velocity = measured;
            }
            position = current;
            tick = gameTick;
        }

        Vec3 velocity() { return velocity; }
        long tick() { return tick; }
    }
}
