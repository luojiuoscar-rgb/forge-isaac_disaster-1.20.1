package net.luojiuoscar.isaac_disaster.groundeffect.core;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** A fixed-height contact cylinder, clipped by the effect's remaining painted coverage. */
public final class GroundEffectContactGeometry {
    public static final double HEIGHT_TOLERANCE = 0.2D;

    private GroundEffectContactGeometry() { }

    public static boolean touches(AbstractGroundEffect effect, AABB targetBounds) {
        Vec3 surface = effect.position();
        double feetY = targetBounds.minY;
        if (feetY < surface.y - HEIGHT_TOLERANCE || feetY > surface.y + HEIGHT_TOLERANCE) return false;
        double dx = (targetBounds.minX + targetBounds.maxX) * 0.5D - surface.x;
        double dz = (targetBounds.minZ + targetBounds.maxZ) * 0.5D - surface.z;
        if (dx * dx + dz * dz > effect.radius() * effect.radius()) return false;
        return effect.coverage().contains(dx, dz);
    }
}
