package net.luojiuoscar.isaac_disaster.bullet.collision;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Allocation-free slab intersection helpers for projectile sweeps. */
public final class SweptCollision {
    private SweptCollision() {
    }

    /** Compact result avoiding Optional allocation in hot collision loops. */
    public record OptionalDoubleHit(boolean hit, double parameter) {
        /** Creates a miss result with an unusable path parameter. */
        public static OptionalDoubleHit miss() {
            return new OptionalDoubleHit(false, Double.POSITIVE_INFINITY);
        }
    }

    /** Detailed segment contact including the outward face of the first entered slab. */
    public record SegmentHit(boolean hit, double parameter, Direction outwardFace) {
        /** Creates a miss result with no usable face. */
        public static SegmentHit miss() {
            return new SegmentHit(false, Double.POSITIVE_INFINITY, null);
        }
    }

    /** Performs a slab intersection and returns the earliest normalized path parameter. */
    public static OptionalDoubleHit segmentAabb(Vec3 start, Vec3 end, AABB box) {
        SegmentHit hit = segmentAabbDetailed(start, end, box);
        return hit.hit() ? new OptionalDoubleHit(true, hit.parameter()) : OptionalDoubleHit.miss();
    }

    /** Performs a slab intersection and identifies the surface from which the segment entered. */
    public static SegmentHit segmentAabbDetailed(Vec3 start, Vec3 end, AABB box) {
        double tMin = 0.0D, tMax = 1.0D;
        Direction outwardFace = null;
        double dx = end.x - start.x, dy = end.y - start.y, dz = end.z - start.z;
        double near, far, inv, axisDelta;
        for (int axis = 0; axis < 3; axis++) {
            double origin = axis == 0 ? start.x : axis == 1 ? start.y : start.z;
            axisDelta = axis == 0 ? dx : axis == 1 ? dy : dz;
            double min = axis == 0 ? box.minX : axis == 1 ? box.minY : box.minZ;
            double max = axis == 0 ? box.maxX : axis == 1 ? box.maxY : box.maxZ;
            if (Math.abs(axisDelta) < 1.0E-12D) {
                if (origin < min || origin > max) return SegmentHit.miss();
                continue;
            }
            inv = 1.0D / axisDelta;
            near = (min - origin) * inv;
            far = (max - origin) * inv;
            Direction axisFace = faceForEntry(axis, axisDelta);
            if (near > far) {
                double swap = near;
                near = far;
                far = swap;
            }
            if (near > tMin) {
                tMin = near;
                outwardFace = axisFace;
            }
            tMax = Math.min(tMax, far);
            if (tMin > tMax) return SegmentHit.miss();
        }
        if (outwardFace == null) {
            int dominantAxis = Math.abs(dy) > Math.abs(dx) ? 1 : 0;
            if (Math.abs(dz) > Math.abs(dominantAxis == 0 ? dx : dy)) dominantAxis = 2;
            double dominantDelta = dominantAxis == 0 ? dx : dominantAxis == 1 ? dy : dz;
            if (Math.abs(dominantDelta) > 1.0E-12D) {
                outwardFace = faceForEntry(dominantAxis, -dominantDelta);
            } else {
                double nearest = Double.POSITIVE_INFINITY;
                double minDistance = start.x - box.minX;
                double maxDistance = box.maxX - start.x;
                if (minDistance < nearest) {
                    nearest = minDistance;
                    outwardFace = outwardFace(0, -1.0D);
                }
                if (maxDistance < nearest) {
                    nearest = maxDistance;
                    outwardFace = outwardFace(0, 1.0D);
                }
                minDistance = start.y - box.minY;
                maxDistance = box.maxY - start.y;
                if (minDistance < nearest) {
                    nearest = minDistance;
                    outwardFace = outwardFace(1, -1.0D);
                }
                if (maxDistance < nearest) {
                    nearest = maxDistance;
                    outwardFace = outwardFace(1, 1.0D);
                }
                minDistance = start.z - box.minZ;
                maxDistance = box.maxZ - start.z;
                if (minDistance < nearest) outwardFace = outwardFace(2, -1.0D);
                if (maxDistance < nearest) outwardFace = outwardFace(2, 1.0D);
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
