package net.luojiuoscar.isaac_disaster.helper;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/** Shared swept-volume geometry used by projectile collision checks. */
public final class ProjectileCollisionHelper {
    private static final double MIN_SHATTER_SCALE = 0.25D;
    private static final int BASE_FRAGMENT_COUNT = 2;
    private static final int MIN_FRAGMENT_COUNT = 1;
    private static final int MAX_FRAGMENT_COUNT = 5;
    private static final float BASE_FRAGMENT_QUAD_SIZE = 0.05F;

    private ProjectileCollisionHelper() {
    }

    public static int fragmentCount(double scale) {
        return clamp((int) Math.round(BASE_FRAGMENT_COUNT * Math.sqrt(normalizeScale(scale))),
                MIN_FRAGMENT_COUNT, MAX_FRAGMENT_COUNT);
    }

    /** Half-width of one camera-facing shatter fragment. */
    public static float fragmentQuadSize(double scale) {
        return (float) (BASE_FRAGMENT_QUAD_SIZE * normalizeScale(scale));
    }

    /** Physical fragment diameter, matching its rendered quad. */
    public static float fragmentCollisionSize(double scale) {
        return fragmentQuadSize(scale) * 2.0F;
    }

    public static double normalizeScale(double scale) {
        return Double.isFinite(scale) ? Math.max(MIN_SHATTER_SCALE, scale) : MIN_SHATTER_SCALE;
    }

    public static AABB sweptBounds(AABB projectileBounds, Vec3 motion) {
        return projectileBounds.expandTowards(motion);
    }

    public static Vec3 center(AABB bounds) {
        return bounds.getCenter();
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

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
