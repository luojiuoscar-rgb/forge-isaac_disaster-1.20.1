package net.luojiuoscar.isaac_disaster.bullet.collision;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Direction;

/** Allocation-free slab intersection helpers for projectile sweeps. */
public final class SweptCollision {
    private SweptCollision() {}

    /** Compact result avoiding Optional allocation in hot collision loops. */
    public record OptionalDoubleHit(boolean hit, double parameter) {
        /** Creates a miss result with an unusable path parameter. */
        public static OptionalDoubleHit miss() { return new OptionalDoubleHit(false, Double.POSITIVE_INFINITY); }
    }

    /** Detailed segment contact including the outward face of the first entered slab. */
    public record SegmentHit(boolean hit, double parameter, Direction outwardFace) {
        /** Creates a miss result with no usable face. */
        public static SegmentHit miss() { return new SegmentHit(false, Double.POSITIVE_INFINITY, null); }
    }

    /** Performs a slab intersection and returns the earliest normalized path parameter. */
    public static OptionalDoubleHit segmentAabb(Vec3 start, Vec3 end, AABB box) {
        SegmentHit hit = segmentAabbDetailed(start, end, box);
        return hit.hit() ? new OptionalDoubleHit(true, hit.parameter()) : OptionalDoubleHit.miss();
    }

    /** Performs a slab intersection and identifies the surface from which the segment entered. */
    public static SegmentHit segmentAabbDetailed(Vec3 start, Vec3 end, AABB box) {
        double tMin = 0.0, tMax = 1.0;
        double[] s = {start.x, start.y, start.z};
        double[] d = {end.x - start.x, end.y - start.y, end.z - start.z};
        double[] min = {box.minX, box.minY, box.minZ};
        double[] max = {box.maxX, box.maxY, box.maxZ};
        Direction outwardFace = null;
        for (int axis = 0; axis < 3; axis++) {
            if (Math.abs(d[axis]) < 1.0E-12) {
                if (s[axis] < min[axis] || s[axis] > max[axis]) return SegmentHit.miss();
                continue;
            }
            double inv = 1.0 / d[axis];
            double near = (min[axis] - s[axis]) * inv;
            double far = (max[axis] - s[axis]) * inv;
            Direction axisFace = faceForEntry(axis, d[axis]);
            if (near > far) { double tmp = near; near = far; far = tmp; }
            if (near > tMin) {
                tMin = near;
                outwardFace = axisFace;
            }
            tMax = Math.min(tMax, far);
            if (tMin > tMax) return SegmentHit.miss();
        }
        // A segment that starts inside the expanded volume has no positive entry
        // plane. Pick the dominant incoming axis so callers still receive a
        // usable outward normal and can push the bullet out before the next tick.
        if (outwardFace == null) {
            int dominantAxis = 0;
            if (Math.abs(d[1]) > Math.abs(d[dominantAxis])) dominantAxis = 1;
            if (Math.abs(d[2]) > Math.abs(d[dominantAxis])) dominantAxis = 2;
            if (Math.abs(d[dominantAxis]) > 1.0E-12D) {
                outwardFace = faceForEntry(dominantAxis, -d[dominantAxis]);
            } else {
                double[] distanceToMin = {s[0] - min[0], s[1] - min[1], s[2] - min[2]};
                double[] distanceToMax = {max[0] - s[0], max[1] - s[1], max[2] - s[2]};
                double nearest = Double.POSITIVE_INFINITY;
                for (int axis = 0; axis < 3; axis++) {
                    if (distanceToMin[axis] < nearest) {
                        nearest = distanceToMin[axis];
                        outwardFace = outwardFace(axis, -1.0D);
                    }
                    if (distanceToMax[axis] < nearest) {
                        nearest = distanceToMax[axis];
                        outwardFace = outwardFace(axis, 1.0D);
                    }
                }
            }
        }
        return new SegmentHit(true, tMin, outwardFace);
    }

    /** Maps a positive movement axis to the opposite, outward face of the entered box. */
    private static Direction faceForEntry(int axis, double delta) {
        return switch (axis) {
            case 0 -> delta > 0.0D ? Direction.WEST : Direction.EAST;
            case 1 -> delta > 0.0D ? Direction.DOWN : Direction.UP;
            case 2 -> delta > 0.0D ? Direction.NORTH : Direction.SOUTH;
            default -> Direction.NORTH;
        };
    }

    private static Direction outwardFace(int axis, double sign) {
        return switch (axis) {
            case 0 -> sign < 0.0D ? Direction.WEST : Direction.EAST;
            case 1 -> sign < 0.0D ? Direction.DOWN : Direction.UP;
            case 2 -> sign < 0.0D ? Direction.NORTH : Direction.SOUTH;
            default -> Direction.NORTH;
        };
    }
}
