package net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.core;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Optional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Resolves one player's feet position to the nearest collision surface below it. */
public final class GroundEffectPlacement {
    private static final int MAX_DROP = 4;
    private static final double MAX_SURFACE_GAP = 2.0D;
    private static final double SURFACE_OFFSET = 0.002D;

    private GroundEffectPlacement() {
    }

    public static Optional<Vec3> projectToGround(ServerLevel level, Vec3 feet) {
        if (!level.hasChunkAt(BlockPos.containing(feet))) return Optional.empty();
        int blockX = BlockPos.containing(feet).getX();
        int blockZ = BlockPos.containing(feet).getZ();
        int startY = (int) Math.floor(feet.y + 1.0E-6D);
        for (int y = startY; y >= Math.max(level.getMinBuildHeight(), startY - MAX_DROP); y--) {
            BlockPos blockPos = new BlockPos(blockX, y, blockZ);
            VoxelShape shape = level.getBlockState(blockPos).getCollisionShape(level, blockPos);
            if (shape.isEmpty()) continue;

            double maxY = shape.toAabbs().stream()
                    .filter(box -> feet.x - blockX >= box.minX && feet.x - blockX <= box.maxX
                            && feet.z - blockZ >= box.minZ && feet.z - blockZ <= box.maxZ
                            && ySurface(box, blockPos) <= feet.y + 0.1D)
                    .mapToDouble(box -> box.maxY).max().orElse(Double.NEGATIVE_INFINITY);
            double surfaceY = y + maxY;
            if (surfaceY <= feet.y + 0.1D && feet.y - surfaceY <= MAX_SURFACE_GAP) {
                return Optional.of(new Vec3(feet.x, surfaceY + SURFACE_OFFSET, feet.z));
            }
        }
        return Optional.empty();
    }

    /** Checks only the original center support, including slabs and collision shapes taller than a block. */
    public static boolean hasSupport(ServerLevel level, Vec3 position) {
        if (!level.hasChunkAt(BlockPos.containing(position))) return false;
        double surfaceY = position.y - SURFACE_OFFSET;
        AABB probe = new AABB(position.x - 0.001D, surfaceY - 0.01D, position.z - 0.001D,
                position.x + 0.001D, surfaceY + 0.001D, position.z + 0.001D);
        for (var collision : level.getBlockCollisions(null, probe)) {
            for (AABB box : collision.toAabbs()) {
                if (Math.abs(box.maxY - surfaceY) <= 0.01D) return true;
            }
        }
        return false;
    }

    private static double ySurface(AABB box, BlockPos block) { return block.getY() + box.maxY; }

    /** Checks the whole horizontal segment, so a narrow hole cannot be skipped in one tick. */
    public static boolean hasContinuousSupport(ServerLevel level, Vec3 from, Vec3 to) {
        double surface = from.y - SURFACE_OFFSET;
        AABB probe = new AABB(Math.min(from.x, to.x) - 0.001D, surface - 0.01D,
                Math.min(from.z, to.z) - 0.001D, Math.max(from.x, to.x) + 0.001D,
                surface + 0.001D, Math.max(from.z, to.z) + 0.001D);
        if (!level.hasChunksAt(BlockPos.containing(probe.minX, probe.minY, probe.minZ),
                BlockPos.containing(probe.maxX, probe.maxY, probe.maxZ))) return false;
        List<AABB> surfaces = new ArrayList<>();
        for (var shape : level.getBlockCollisions(null, probe)) surfaces.addAll(shape.toAabbs());
        return coversHorizontalPath(from, to, surfaces);
    }

    static boolean coversHorizontalPath(Vec3 from, Vec3 to, List<AABB> surfaces) {
        if (Math.abs(to.y - from.y) > 1.0E-8D) return false;
        List<double[]> intervals = new ArrayList<>();
        for (AABB box : surfaces) {
            if (Math.abs(box.maxY - (from.y - SURFACE_OFFSET)) > 0.01D) continue;
            double[] interval = {0, 1};
            if (clipAxis(from.x, to.x - from.x, box.minX, box.maxX, interval)
                    && clipAxis(from.z, to.z - from.z, box.minZ, box.maxZ, interval)) intervals.add(interval);
        }
        intervals.sort(Comparator.comparingDouble(value -> value[0]));
        double covered = 0;
        for (double[] interval : intervals) {
            if (interval[0] > covered + 1.0E-8D) return false;
            covered = Math.max(covered, interval[1]);
            if (covered >= 1.0D - 1.0E-8D) return true;
        }
        return false;
    }

    private static boolean clipAxis(double start, double delta, double min, double max, double[] interval) {
        if (Math.abs(delta) < 1.0E-10D) return start >= min && start <= max;
        double first = (min - start) / delta;
        double last = (max - start) / delta;
        interval[0] = Math.max(interval[0], Math.min(first, last));
        interval[1] = Math.min(interval[1], Math.max(first, last));
        return interval[0] <= interval[1];
    }
}
