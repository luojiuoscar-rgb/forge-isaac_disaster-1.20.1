package net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

/** Short, collision-safe wandering spells when the owner's orbit does not fit the room. */
final class FlyRoaming {
    private final AbstractFlyFamiliarEntity fly;
    private final RandomSource random;
    private long until = Long.MIN_VALUE;
    private long nextGoalTick;
    private Vec3 goal;

    FlyRoaming(AbstractFlyFamiliarEntity fly, RandomSource random) {
        this.fly = fly;
        this.random = random;
    }

    boolean active(long tick) { return tick < until; }

    void start(long tick) {
        until = tick + 40 + random.nextInt(41);
        nextGoalTick = tick;
        goal = null;
    }

    void stop() {
        until = Long.MIN_VALUE;
        goal = null;
    }

    void blocked(long tick) {
        goal = null;
        nextGoalTick = tick + 10;
    }

    Vec3 goal(ServerPlayer owner, long tick) {
        Vec3 center = owner.getBoundingBox().getCenter().add(0, -fly.getBbHeight() * 0.5, 0);
        double radius = Math.max(0.75, Math.min(3, owner.getBbWidth() * (2.0 / 0.6)));
        double height = Math.min(1.25, Math.max(0.3, owner.getBbHeight() * 0.5));
        if (goal != null && tick < nextGoalTick
                && goal.subtract(center).horizontalDistanceSqr() <= radius * radius
                && Math.abs(goal.y - center.y) <= height
                && fly.position().distanceToSqr(goal) > 0.15 * 0.15) return goal;
        if (goal == null && tick < nextGoalTick) return null;
        nextGoalTick = tick + 10;
        goal = null;
        for (int attempt = 0; attempt < 24; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double distance = radius * (0.3 + random.nextDouble() * 0.7);
            Vec3 candidate = center.add(Math.cos(angle) * distance,
                    (random.nextDouble() * 2 - 1) * height, Math.sin(angle) * distance);
            // Prefer visible climbs/descents; the final attempts also fit low ceilings.
            if (attempt < 16 && Math.abs(candidate.y - fly.getY()) < height * 0.4) continue;
            if (fly.position().distanceToSqr(candidate) >= 0.2 * 0.2
                    && FlyNavigation.clearPath(fly, fly.position(), candidate)) {
                goal = candidate;
                break;
            }
        }
        return goal;
    }
}
