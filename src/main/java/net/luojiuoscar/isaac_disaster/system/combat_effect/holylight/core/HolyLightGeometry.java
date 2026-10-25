package net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.core;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** The damaging volume is near the fixed impact point, independent of visual beam height. */
public final class HolyLightGeometry {
    public static final double MIN_Y = -1.0;
    public static final double MAX_Y = 2.5;
    public static final double BOUNDARY_EPSILON = 1.0E-7;

    private HolyLightGeometry() {
    }

    public static AABB bounds(Vec3 position, double radius) {
        return new AABB(position.x - radius, position.y + MIN_Y, position.z - radius,
                position.x + radius, position.y + MAX_Y, position.z + radius);
    }

    public static boolean intersects(Vec3 position, double radius, AABB target) {
        if (target.maxY < position.y + MIN_Y || target.minY > position.y + MAX_Y) return false;
        double dx = Mth.clamp(position.x, target.minX, target.maxX) - position.x;
        double dz = Mth.clamp(position.z, target.minZ, target.maxZ) - position.z;
        // World-coordinate subtraction can round exact tangency just outside the circle.
        double inclusiveRadius = radius + BOUNDARY_EPSILON;
        return dx * dx + dz * dz <= inclusiveRadius * inclusiveRadius;
    }

    public static Vec3 closestPoint(Vec3 point, AABB bounds) {
        return new Vec3(Mth.clamp(point.x, bounds.minX, bounds.maxX),
                Mth.clamp(point.y, bounds.minY, bounds.maxY),
                Mth.clamp(point.z, bounds.minZ, bounds.maxZ));
    }
}
