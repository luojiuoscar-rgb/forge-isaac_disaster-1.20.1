package net.luojiuoscar.isaac_disaster.helper;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/** Shared direction and impact-plane geometry used by attacks and patterns. */
public final class GeometryHelper {
    private static final double EPSILON = 1.0E-8;
    private static final Vec3 WORLD_UP = new Vec3(0.0, 1.0, 0.0);
    private static final Vec3 WORLD_EAST = new Vec3(1.0, 0.0, 0.0);
    private static final Vec3 WORLD_FORWARD = new Vec3(0.0, 0.0, 1.0);

    private GeometryHelper() {
    }

    public static Vec3 mainAxisFromRotation(float xRot, float yRot) {
        return Vec3.directionFromRotation(xRot, yRot).normalize();
    }

    public static Rotation rotationFromMainAxis(Vec3 mainAxis) {
        Vec3 normalized = normalizeOrFallback(mainAxis);
        return new Rotation((float) Math.toDegrees(Math.asin(-normalized.y)),
                (float) Math.toDegrees(Math.atan2(-normalized.x, normalized.z)));
    }

    public static Vec3 rotateAroundAxis(Vec3 vector, Vec3 axis, double radians) {
        Vec3 normalizedAxis = normalizeOrFallback(axis);
        Vec3 normalizedVector = normalizeOrFallback(vector);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return normalizedVector.scale(cos)
                .add(normalizedAxis.cross(normalizedVector).scale(sin))
                .add(normalizedAxis.scale(normalizedAxis.dot(normalizedVector) * (1.0 - cos)))
                .normalize();
    }

    public static Vec3 lateralAxis(Vec3 mainAxis) {
        Vec3 axis = normalizeOrFallback(mainAxis);
        Vec3 side = axis.cross(WORLD_UP);
        if (side.lengthSqr() < EPSILON) {
            side = axis.cross(WORLD_EAST);
        }
        if (side.lengthSqr() < EPSILON) {
            side = WORLD_EAST;
        }
        return side.normalize();
    }

    public static Vec3 projectOntoImpactPlane(Vec3 mainAxis, BlockHitResult hit) {
        Vec3 normal = hit == null ? WORLD_UP : normalFromDirection(hit.getDirection());
        return projectOntoPlane(mainAxis, normal);
    }

    public static Vec3 projectOntoPlane(Vec3 mainAxis, Vec3 planeNormal) {
        Vec3 normal = normalizeOrFallback(planeNormal);
        Vec3 axis = normalizeOrFallback(mainAxis);
        Vec3 projected = axis.subtract(normal.scale(axis.dot(normal)));
        if (projected.lengthSqr() < EPSILON) {
            projected = fallbackTangent(normal);
        }
        return projected.normalize();
    }

    public static Vec3 normalFromDirection(Direction direction) {
        return direction == null ? WORLD_UP : Vec3.atLowerCornerOf(direction.getNormal()).normalize();
    }

    private static Vec3 normalizeOrFallback(Vec3 vector) {
        if (vector == null || !Double.isFinite(vector.x) || !Double.isFinite(vector.y)
                || !Double.isFinite(vector.z) || vector.lengthSqr() < EPSILON) {
            return WORLD_FORWARD;
        }
        return vector.normalize();
    }

    private static Vec3 fallbackTangent(Vec3 normal) {
        Vec3 up = Math.abs(normal.y) < 0.999 ? WORLD_UP : WORLD_FORWARD;
        Vec3 tangent = normal.cross(up);
        if (tangent.lengthSqr() < EPSILON) {
            tangent = normal.cross(WORLD_EAST);
        }
        return tangent.lengthSqr() < EPSILON ? WORLD_EAST : tangent.normalize();
    }

    public record Rotation(float xRot, float yRot) {
    }
}
