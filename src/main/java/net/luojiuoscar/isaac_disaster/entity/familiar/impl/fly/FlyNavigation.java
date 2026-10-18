package net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly;

import net.luojiuoscar.isaac_disaster.bullet.collision.SweptCollision;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.BiPredicate;

/** Bounded, incremental body navigation. All positions refer to the entity's bottom center. */
final class FlyNavigation {
    private static final double STEP = 0.5;
    private static final int MAX_EXPANSIONS = 2048;
    static final int TICK_NODE_BUDGET = 512;
    private static final int REPLAN_COOLDOWN = 10;
    private static final double EPSILON = 1.0E-7;

    private FlyNavigation() { }

    /** Sweeps only the current collision body, ignoring entities and decorative wings. */
    static boolean clearPath(AbstractFlyFamiliarEntity fly, Vec3 bottomFrom, Vec3 bottomTo) {
        Level level = fly.level();
        double halfWidth = fly.getBbWidth() / 2.0;
        double height = fly.getBbHeight();
        AABB bounds = new AABB(
                Math.min(bottomFrom.x, bottomTo.x) - halfWidth,
                Math.min(bottomFrom.y, bottomTo.y),
                Math.min(bottomFrom.z, bottomTo.z) - halfWidth,
                Math.max(bottomFrom.x, bottomTo.x) + halfWidth,
                Math.max(bottomFrom.y, bottomTo.y) + height,
                Math.max(bottomFrom.z, bottomTo.z) + halfWidth);
        if (bounds.minY < level.getMinBuildHeight() || bounds.maxY > level.getMaxBuildHeight()) return false;

        // Block collision iteration inspects neighboring blocks for protruding shapes. Check its
        // entire neighborhood first so navigation never requests an unloaded chunk.
        int minChunkX = Mth.floor(bounds.minX - 1) >> 4;
        int maxChunkX = Mth.floor(bounds.maxX + 1) >> 4;
        int minChunkZ = Mth.floor(bounds.minZ - 1) >> 4;
        int maxChunkZ = Mth.floor(bounds.maxZ + 1) >> 4;
        for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                if (!level.hasChunkAt(new BlockPos(x << 4, Mth.floor(bottomFrom.y), z << 4))) return false;
            }
        }
        for (VoxelShape shape : level.getBlockCollisions(fly, bounds)) {
            for (AABB blockBox : shape.toAabbs()) {
                if (bodyIntersects(blockBox, bottomFrom, bottomTo, halfWidth, height)) return false;
            }
        }
        return true;
    }

    /** Minkowski expansion converts a moving body into a point segment. Face contact is free. */
    static boolean bodyIntersects(AABB block, Vec3 from, Vec3 to, double halfWidth, double height) {
        AABB expanded = new AABB(block.minX - halfWidth + EPSILON,
                block.minY - height + EPSILON, block.minZ - halfWidth + EPSILON,
                block.maxX + halfWidth - EPSILON, block.maxY - EPSILON,
                block.maxZ + halfWidth - EPSILON);
        return SweptCollision.segmentAabb(from, to, expanded).hit();
    }

    /** World-independent state machine, also exercised by navigation unit tests. */
    static final class RoutePlanner {
        private final BiPredicate<Vec3, Vec3> clear;
        private Search search;
        private List<Vec3> path = List.of();
        private int pathIndex;
        private Vec3 plannedGoal;
        private long lastPlanTick = Long.MIN_VALUE;
        private boolean failed;

        RoutePlanner(BiPredicate<Vec3, Vec3> clear) {
            this.clear = clear;
        }

        List<Vec3> nextSteps(Vec3 position, Vec3 goal, long tick, int budget, double speed) {
            double travel = Double.isFinite(speed) ? Mth.clamp(speed, 0, FlyOrbitLayout.MAX_SPEED) : 0;
            if (travel < EPSILON) return List.of();
            if (search == null && pathIndex >= path.size() && clear.test(position, goal)) {
                search = null;
                path = List.of();
                pathIndex = 0;
                plannedGoal = null;
                failed = false;
                Vec3 delta = goal.subtract(position);
                double length = delta.length();
                return length < EPSILON ? List.of() : List.of(delta.scale(Math.min(1.0, travel / length)));
            }
            boolean canReplan = lastPlanTick == Long.MIN_VALUE || tick - lastPlanTick >= REPLAN_COOLDOWN;
            if (plannedGoal != null && plannedGoal.distanceToSqr(goal) > 1.0 && canReplan) {
                discardRoute();
            }
            if (failed) {
                if (!canReplan) return List.of();
                discardRoute();
            }
            if (search == null && pathIndex >= path.size()) {
                if (!canReplan) return List.of();
                search = new Search(position, goal, clear);
                plannedGoal = goal;
                lastPlanTick = tick;
            }
            if (search != null && search.path() == null) {
                search.advance(Math.min(budget, TICK_NODE_BUDGET));
                if (search.failed()) {
                    failed = true;
                    return List.of();
                }
                if (search.path() == null) return List.of();
                path = search.path();
                pathIndex = 0;
            }
            while (pathIndex < path.size() && position.distanceToSqr(path.get(pathIndex)) < EPSILON * EPSILON) {
                pathIndex++;
            }
            if (pathIndex >= path.size()) {
                search = null;
                return List.of();
            }
            List<Vec3> result = new ArrayList<>();
            Vec3 cursor = position;
            double remaining = travel;
            while (pathIndex < path.size() && remaining > EPSILON) {
                Vec3 waypoint = path.get(pathIndex);
                Vec3 offset = waypoint.subtract(cursor);
                double distance = offset.length();
                if (distance < EPSILON) {
                    pathIndex++;
                    continue;
                }
                Vec3 delta = offset.scale(Math.min(remaining / distance, 1.0));
                Vec3 next = cursor.add(delta);
                // The caller validates each actual movement once against current blocks.
                result.add(delta);
                remaining -= delta.length();
                cursor = next;
                if (distance <= delta.length() + EPSILON) pathIndex++;
            }
            if (pathIndex >= path.size()) search = null;
            return result;
        }

        int expandedNodes() {
            return search == null ? 0 : search.expandedNodes();
        }

        boolean failed() { return failed; }

        private void discardRoute() {
            search = null;
            path = List.of();
            pathIndex = 0;
            plannedGoal = null;
            failed = false;
        }

        void clear() {
            discardRoute();
        }
    }

    /** Half-block 26-neighbor A*, with a total bound independent of per-tick scheduling. */
    static final class Search {
        private final Vec3 start;
        private final Vec3 goal;
        private final AABB bounds;
        private final BiPredicate<Vec3, Vec3> clear;
        private final PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(Node::score)
                .thenComparingDouble(Node::remaining));
        private final Map<GridPoint, Node> best = new HashMap<>();
        private final Set<GridPoint> closed = new HashSet<>();
        private int expanded;
        private boolean failed;
        private List<Vec3> path;

        Search(Vec3 start, Vec3 goal, BiPredicate<Vec3, Vec3> clear) {
            this.start = start;
            this.goal = goal;
            this.clear = clear;
            this.bounds = new AABB(start, goal).inflate(4.0);
            GridPoint origin = new GridPoint(0, 0, 0);
            Node first = new Node(origin, 0, start.distanceTo(goal), null);
            best.put(origin, first);
            open.add(first);
        }

        void advance(int nodeBudget) {
            if (path != null || failed || nodeBudget <= 0) return;
            int allowed = Math.min(nodeBudget, MAX_EXPANSIONS - expanded);
            int used = 0;
            while (used < allowed && !open.isEmpty()) {
                Node current = open.poll();
                if (best.get(current.point) != current || !closed.add(current.point)) continue;
                expanded++;
                used++;
                Vec3 from = position(current.point);
                if (from.distanceToSqr(goal) <= 1.0 && clear.test(from, goal)) {
                    List<Vec3> result = new ArrayList<>();
                    result.add(goal);
                    for (Node node = current; node.parent != null; node = node.parent) {
                        result.add(position(node.point));
                    }
                    Collections.reverse(result);
                    path = List.copyOf(result);
                    return;
                }
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        for (int z = -1; z <= 1; z++) {
                            if (x == 0 && y == 0 && z == 0) continue;
                            GridPoint next = new GridPoint(current.point.x + x, current.point.y + y, current.point.z + z);
                            if (closed.contains(next)) continue;
                            Vec3 to = position(next);
                            if (!bounds.contains(to)) continue;
                            double cost = current.cost + STEP * Math.sqrt(x * x + y * y + z * z);
                            Node previous = best.get(next);
                            if (previous != null && previous.cost <= cost) continue;
                            if (!clear.test(from, to)) continue;
                            Node candidate = new Node(next, cost, to.distanceTo(goal), current);
                            best.put(next, candidate);
                            open.add(candidate);
                        }
                    }
                }
            }
            if (open.isEmpty() || expanded >= MAX_EXPANSIONS) failed = true;
        }

        private Vec3 position(GridPoint point) {
            return start.add(point.x * STEP, point.y * STEP, point.z * STEP);
        }

        @Nullable
        List<Vec3> path() {
            return path;
        }

        boolean failed() {
            return failed;
        }

        int expandedNodes() {
            return expanded;
        }

        private record GridPoint(int x, int y, int z) {
        }

        private record Node(GridPoint point, double cost, double remaining, @Nullable Node parent) {
            double score() {
                return cost + 1.5 * remaining;
            }
        }
    }
}
