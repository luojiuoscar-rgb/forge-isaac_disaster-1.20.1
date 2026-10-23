package net.luojiuoscar.isaac_disaster.groundeffect.core;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Optional;

/** Resolves one player's feet position to the nearest collision surface below it. */
public final class GroundEffectPlacement {
    private static final int MAX_DROP = 4;
    private static final double MAX_SURFACE_GAP = 2.0D;
    private static final double SURFACE_OFFSET = 0.002D;

    private GroundEffectPlacement() {
    }

    public static Optional<Vec3> projectToGround(ServerLevel level, Vec3 feet) {
        int blockX = BlockPos.containing(feet).getX();
        int blockZ = BlockPos.containing(feet).getZ();
        int startY = (int) Math.floor(feet.y + 1.0E-6D);
        for (int y = startY; y >= Math.max(level.getMinBuildHeight(), startY - MAX_DROP); y--) {
            BlockPos blockPos = new BlockPos(blockX, y, blockZ);
            VoxelShape shape = level.getBlockState(blockPos).getCollisionShape(level, blockPos);
            if (shape.isEmpty()) continue;

            double maxY = shape.toAabbs().stream().mapToDouble(box -> box.maxY).max().orElse(Double.NEGATIVE_INFINITY);
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
        return level.getBlockCollisions(null, probe).iterator().hasNext();
    }
}
