package net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.server;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/** Snaps only to nearby collision tops; otherwise keeps a valid airborne position. */
public final class HolyLightPlacement {
    public static final double GROUND_SNAP_DISTANCE = 1;
    public static final double MAX_BEAM_HEIGHT = 16;
    public static final double SURFACE_OFFSET = 0.002;
    private static final double SUPPORT_PROBE = 0.01;
    private static final double SURFACE_EPSILON = 1.0E-4;
    private static final double SNAP_EPSILON = 1.0E-7;

    private HolyLightPlacement() {
    }

    public static Optional<Placement> resolve(ServerLevel level, Vec3 candidate) {
        if (level == null || !validPosition(level, candidate)) return Optional.empty();
        BlockPos requested = BlockPos.containing(candidate);
        if (!level.hasChunkAt(requested) || !level.getWorldBorder().isWithinBounds(requested)) {
            return Optional.empty();
        }
        Vec3 from = candidate.add(0, 0.25, 0);
        // Collision clipping excludes a hit exactly at the ray endpoint.
        Vec3 to = candidate.add(0, -GROUND_SNAP_DISTANCE - SNAP_EPSILON, 0);
        BlockHitResult support = clip(level, from, to);
        // A clear airborne origin can be less than 0.25 blocks below a ceiling.
        // Retry from the actual origin when only the raised ground probe starts inside it.
        if (support.isInside()) support = clip(level, candidate, to);
        if (support.isInside()) return Optional.empty();
        boolean grounded = support.getType() == HitResult.Type.BLOCK && support.getDirection() == Direction.UP
                && candidate.y - support.getLocation().y <= GROUND_SNAP_DISTANCE + SNAP_EPSILON;
        Vec3 position = grounded ? support.getLocation().add(0, SURFACE_OFFSET, 0) : candidate;
        if (!validPosition(level, position)) return Optional.empty();
        // Start above the surface offset so the support cannot mask the overhead obstacle.
        Vec3 top = position.add(0, MAX_BEAM_HEIGHT, 0);
        BlockHitResult overhead = clip(level, position, top);
        double beamHeight = overhead.getType() == HitResult.Type.BLOCK
                ? Math.max(0, overhead.getLocation().y - position.y) : MAX_BEAM_HEIGHT;
        if (beamHeight <= 0) return Optional.empty();
        return Optional.of(new Placement(position, beamHeight, grounded));
    }

    public static boolean hasSupport(ServerLevel level, Vec3 position) {
        if (level == null || !validPosition(level, position)
                || !level.hasChunkAt(BlockPos.containing(position))) return false;
        double surfaceY = position.y - SURFACE_OFFSET;
        BlockHitResult hit = clip(level, new Vec3(position.x, surfaceY + SUPPORT_PROBE, position.z),
                new Vec3(position.x, surfaceY - SUPPORT_PROBE, position.z));
        return hit.getType() == HitResult.Type.BLOCK && !hit.isInside()
                && hit.getDirection() == Direction.UP
                && Math.abs(hit.getLocation().y - surfaceY) <= SURFACE_EPSILON;
    }

    private static boolean validPosition(ServerLevel level, Vec3 position) {
        return position != null && Double.isFinite(position.x) && Double.isFinite(position.y)
                && Double.isFinite(position.z) && position.x >= -30_000_000 && position.x < 30_000_000
                && position.z >= -30_000_000 && position.z < 30_000_000
                && position.y >= level.getMinBuildHeight() && position.y < level.getMaxBuildHeight();
    }

    private static BlockHitResult clip(ServerLevel level, Vec3 from, Vec3 to) {
        return level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
    }

    public record Placement(Vec3 position, double beamHeight, boolean grounded) {
    }
}
