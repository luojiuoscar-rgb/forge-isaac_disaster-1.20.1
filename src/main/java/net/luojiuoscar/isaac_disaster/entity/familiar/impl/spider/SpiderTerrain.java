package net.luojiuoscar.isaac_disaster.entity.familiar.impl.spider;

import net.luojiuoscar.isaac_disaster.bullet.collision.SweptCollision;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/** Small collision and spawn-placement helpers; ordinary movement uses vanilla collision. */
public final class SpiderTerrain {
    private static final double EPSILON = 1.0E-6;

    private SpiderTerrain() { }

    /** Finds the nearest clear collision-shape top in the body's narrow vertical column. */
    @Nullable
    public static Vec3 groundPosition(Entity spider, Vec3 near, int verticalRange) {
        double halfWidth = spider.getBbWidth() / 2.0;
        double height = spider.getBbHeight();
        Level level = spider.level();
        AABB requested = groundColumn(near, verticalRange, halfWidth, height);
        double minY = Math.max(requested.minY, level.getMinBuildHeight());
        double maxY = Math.min(requested.maxY, level.getMaxBuildHeight());
        if (minY >= maxY) return null;
        AABB column = new AABB(requested.minX, minY, requested.minZ, requested.maxX, maxY, requested.maxZ);
        if (!loaded(level, column)) return null;
        List<AABB> boxes = new ArrayList<>();
        for (VoxelShape shape : level.getBlockCollisions(spider, column)) boxes.addAll(shape.toAabbs());
        return groundPosition(boxes, near, column, halfWidth, height);
    }

    static AABB groundColumn(Vec3 near, int verticalRange, double halfWidth, double height) {
        int range = Mth.clamp(verticalRange, 0, 16);
        return new AABB(near.x - halfWidth, near.y - range - EPSILON, near.z - halfWidth,
                near.x + halfWidth, near.y + range + height + EPSILON, near.z + halfWidth);
    }

    @Nullable
    static Vec3 groundPosition(List<AABB> boxes, Vec3 near, AABB column, double halfWidth, double height) {
        Vec3 result = null;
        double nearest = Double.POSITIVE_INFINITY;
        for (AABB support : boxes) {
            if (support.maxY < column.minY || support.maxY + height > column.maxY
                    || support.maxX <= near.x - halfWidth + EPSILON
                    || support.minX >= near.x + halfWidth - EPSILON
                    || support.maxZ <= near.z - halfWidth + EPSILON
                    || support.minZ >= near.z + halfWidth - EPSILON) continue;
            double distance = Math.abs(support.maxY - near.y);
            if (distance >= nearest) continue;
            Vec3 candidate = new Vec3(near.x, support.maxY, near.z);
            boolean clear = true;
            for (AABB obstacle : boxes) {
                if (bodyIntersects(obstacle, candidate, candidate, halfWidth, height)) {
                    clear = false;
                    break;
                }
            }
            if (clear) {
                result = candidate;
                nearest = distance;
            }
        }
        return result;
    }

    /** Used for admission and recall; the owner's position is the last-resort fallback. */
    public static Vec3 nearOwner(Entity spider, Player owner) {
        Vec3 center = owner.position();
        for (int radius = 0; radius <= 3; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.max(Math.abs(x), Math.abs(z)) != radius || x * x + z * z > 9) continue;
                    Vec3 ground = groundPosition(spider, center.add(x, 0, z), 3);
                    if (ground != null) return ground;
                }
            }
        }
        return center;
    }

    /** Excludes entity collisions: familiar bodies may overlap one another. */
    public static boolean clearSweep(Entity spider, Vec3 from, Vec3 to) {
        double halfWidth = spider.getBbWidth() / 2.0;
        double height = spider.getBbHeight();
        AABB bounds = new AABB(Math.min(from.x, to.x) - halfWidth, Math.min(from.y, to.y),
                Math.min(from.z, to.z) - halfWidth, Math.max(from.x, to.x) + halfWidth,
                Math.max(from.y, to.y) + height, Math.max(from.z, to.z) + halfWidth);
        if (!loaded(spider.level(), bounds)) return false;
        for (VoxelShape shape : spider.level().getBlockCollisions(spider, bounds)) {
            for (AABB box : shape.toAabbs()) {
                if (bodyIntersects(box, from, to, halfWidth, height)) return false;
            }
        }
        return true;
    }

    static boolean bodyIntersects(AABB block, Vec3 from, Vec3 to, double halfWidth, double height) {
        AABB expanded = new AABB(block.minX - halfWidth + EPSILON,
                block.minY - height + EPSILON, block.minZ - halfWidth + EPSILON,
                block.maxX + halfWidth - EPSILON, block.maxY - EPSILON,
                block.maxZ + halfWidth - EPSILON);
        return SweptCollision.segmentAabb(from, to, expanded).hit();
    }

    private static boolean loaded(Level level, AABB bounds) {
        if (bounds.minY < level.getMinBuildHeight() || bounds.maxY > level.getMaxBuildHeight()) return false;
        for (int x = Mth.floor(bounds.minX - 1) >> 4; x <= Mth.floor(bounds.maxX + 1) >> 4; x++) {
            for (int z = Mth.floor(bounds.minZ - 1) >> 4; z <= Mth.floor(bounds.maxZ + 1) >> 4; z++) {
                if (!level.hasChunkAt(new BlockPos(x << 4, Mth.floor(bounds.minY), z << 4))) return false;
            }
        }
        return true;
    }
}
