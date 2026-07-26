package net.luojiuoscar.isaac_disaster.system.flight;

import net.minecraft.world.phys.Vec3;

/** Pure movement rules shared by client prediction and server authority. */
public final class IsaacFlightRules {
    private IsaacFlightRules() {
    }

    public static double calculateSpeedCap(double movementSpeed, double multiplier, double absoluteCap) {
        return Math.max(0.0D, Math.min(Math.max(0.0D, movementSpeed) * Math.max(0.0D, multiplier),
                Math.max(0.0D, absoluteCap)));
    }

    public static Vec3 calculateNextVelocity(Vec3 currentVelocity, Vec3 lookDirection, double speedCap,
                                              double steeringFactor) {
        double cap = Math.max(0.0D, speedCap);
        Vec3 direction = lookDirection.lengthSqr() < 1.0E-12D ? Vec3.ZERO : lookDirection.normalize();
        double steering = Math.max(0.0D, Math.min(1.0D, steeringFactor));
        Vec3 target = direction.scale(cap);
        Vec3 next = currentVelocity.add(target.subtract(currentVelocity).scale(steering));
        double lengthSqr = next.lengthSqr();
        if (cap == 0.0D || lengthSqr <= cap * cap) return cap == 0.0D ? Vec3.ZERO : next;
        return next.scale(cap / Math.sqrt(lengthSqr));
    }
}
