package net.luojiuoscar.isaac_disaster.helper;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/** Shared swept-volume geometry used by projectile collision checks. */
public final class ProjectileCollisionHelper {
    private ProjectileCollisionHelper() {
    }

    public static AABB sweptBounds(AABB projectileBounds, Vec3 motion) {
        return projectileBounds.expandTowards(motion);
    }

    public static Optional<Vec3> clipExpandedTarget(Vec3 start, Vec3 end, AABB target, Vec3 halfExtents) {
        AABB expanded = target.inflate(halfExtents.x, halfExtents.y, halfExtents.z);
        if (expanded.contains(start)) {
            return Optional.of(start);
        }
        return expanded.clip(start, end);
    }

    public static double pathParameter(Vec3 start, Vec3 end, Vec3 point) {
        Vec3 movement = end.subtract(start);
        double lengthSqr = movement.lengthSqr();
        if (lengthSqr <= 1.0E-12) {
            return 0.0D;
        }
        return Math.max(0.0D, Math.min(1.0D, point.subtract(start).dot(movement) / lengthSqr));
    }

    @Nullable
    public static Direction strongestBlockedFace(Vec3 requested, Vec3 allowed) {
        Direction direction = null;
        double strongestReduction = 1.0E-7D;

        double yReduction = movementReduction(requested.y, allowed.y);
        if (yReduction > strongestReduction + 1.0E-7D) {
            strongestReduction = yReduction;
            direction = requested.y > 0.0D ? Direction.DOWN : Direction.UP;
        }

        double xReduction = movementReduction(requested.x, allowed.x);
        if (xReduction > strongestReduction + 1.0E-7D) {
            strongestReduction = xReduction;
            direction = requested.x > 0.0D ? Direction.WEST : Direction.EAST;
        }
        double zReduction = movementReduction(requested.z, allowed.z);
        if (zReduction > strongestReduction + 1.0E-7D) {
            direction = requested.z > 0.0D ? Direction.NORTH : Direction.SOUTH;
        }
        return direction;
    }

    private static double movementReduction(double requested, double allowed) {
        if (Math.abs(requested) <= 1.0E-12D) return 0.0D;
        return Math.max(0.0D, 1.0D - Math.abs(allowed / requested));
    }
}
