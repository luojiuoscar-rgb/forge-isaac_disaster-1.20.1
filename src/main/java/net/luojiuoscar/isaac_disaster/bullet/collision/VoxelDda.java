package net.luojiuoscar.isaac_disaster.bullet.collision;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/** Amanatides-Woo voxel traversal for swept block collision broad phase. */
public final class VoxelDda {
    private VoxelDda() {}

    @FunctionalInterface
    public interface Visitor {
        /** Return true to stop traversal. */
        boolean visit(int x, int y, int z, double tEnter);
    }

    /** Traverses the segment's voxels in entry order until the visitor stops it. */
    public static boolean traverse(Vec3 start, Vec3 end, Visitor visitor) {
        int x = BlockPos.containing(start).getX();
        int y = BlockPos.containing(start).getY();
        int z = BlockPos.containing(start).getZ();
        int endX = BlockPos.containing(end).getX();
        int endY = BlockPos.containing(end).getY();
        int endZ = BlockPos.containing(end).getZ();
        double dx = end.x - start.x, dy = end.y - start.y, dz = end.z - start.z;
        int sx = Integer.compare((int) Math.signum(dx), 0), sy = Integer.compare((int) Math.signum(dy), 0), sz = Integer.compare((int) Math.signum(dz), 0);
        double txDelta = dx == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dx);
        double tyDelta = dy == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dy);
        double tzDelta = dz == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dz);
        double tx = dx == 0 ? Double.POSITIVE_INFINITY : (((sx > 0 ? x + 1 : x) - start.x) / dx);
        double ty = dy == 0 ? Double.POSITIVE_INFINITY : (((sy > 0 ? y + 1 : y) - start.y) / dy);
        double tz = dz == 0 ? Double.POSITIVE_INFINITY : (((sz > 0 ? z + 1 : z) - start.z) / dz);
        if (visitor.visit(x, y, z, 0.0)) return true;
        while (x != endX || y != endY || z != endZ) {
            if (tx < ty && tx < tz) { x += sx; if (visitor.visit(x, y, z, tx)) return true; tx += txDelta; }
            else if (ty < tz) { y += sy; if (visitor.visit(x, y, z, ty)) return true; ty += tyDelta; }
            else { z += sz; if (visitor.visit(x, y, z, tz)) return true; tz += tzDelta; }
        }
        return false;
    }
}
