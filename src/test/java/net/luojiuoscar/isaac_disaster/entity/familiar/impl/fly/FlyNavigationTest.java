package net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly;

import net.luojiuoscar.isaac_disaster.bullet.collision.SweptCollision;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;

import static org.junit.jupiter.api.Assertions.*;

class FlyNavigationTest {
    @Test
    void plannerSpreadsAFullSearchAcrossFourTicksEvenWithAnOversizedAllowance() {
        FlyNavigation.RoutePlanner planner = new FlyNavigation.RoutePlanner(
                outside(new AABB(2.7, -0.3, -0.3, 3.3, 0.3, 0.3)));
        for (long tick = 0; tick < 4; tick++) {
            assertTrue(planner.nextSteps(Vec3.ZERO, new Vec3(3, 0, 0), tick,
                    Integer.MAX_VALUE, 0.5).isEmpty());
            assertEquals((tick + 1) * 512, planner.expandedNodes());
            assertEquals(tick == 3, planner.failed());
        }
    }

    @Test
    void searchUsesLocalGoalConnectionsInsteadOfLongSweepsAtEveryNode() {
        AtomicInteger longSweeps = new AtomicInteger();
        BiPredicate<Vec3, Vec3> wall = outside(new AABB(1, -2, -1, 2, 2, 1));
        FlyNavigation.Search search = solve(Vec3.ZERO, new Vec3(3, 0, 0), (from, to) -> {
            if (from.distanceToSqr(to) > 1.00001) longSweeps.incrementAndGet();
            return wall.test(from, to);
        });
        assertNotNull(search.path());
        assertRouteClear(Vec3.ZERO, new Vec3(3, 0, 0), search.path(), wall);
        assertEquals(0, longSweeps.get());
    }

    @Test
    void directFlightHonorsIdleAndChaseBudgetsAndClampsToMaximum() {
        FlyNavigation.RoutePlanner planner = new FlyNavigation.RoutePlanner((a, b) -> true);
        for (double speed : new double[]{0.3, 0.5, 0.6, 5}) {
            var steps = planner.nextSteps(Vec3.ZERO, new Vec3(5, 0, 0), 0, 0, speed);
            assertEquals(Math.min(speed, 0.6), steps.stream().mapToDouble(Vec3::length).sum(), 1e-9);
        }
    }

    @Test
    void routesAroundWallAndChecksEverySweptEdge() {
        Vec3 start = new Vec3(0, 0, 0);
        Vec3 goal = new Vec3(3, 0, 0);
        BiPredicate<Vec3, Vec3> clear = outside(new AABB(1, -2, -1, 2, 2, 1));
        FlyNavigation.Search search = solve(start, goal, clear);

        assertNotNull(search.path());
        assertTrue(search.path().size() > 1);
        assertRouteClear(start, goal, search.path(), clear);
    }

    @Test
    void takesVerticalRouteOverWallWhenBothSidesAreClosed() {
        Vec3 start = Vec3.ZERO;
        Vec3 goal = new Vec3(3, 0, 0);
        BiPredicate<Vec3, Vec3> clear = outside(new AABB(1, -10, -10, 2, 1, 10));
        FlyNavigation.Search search = solve(start, goal, clear);

        assertNotNull(search.path());
        assertTrue(search.path().stream().anyMatch(point -> point.y > 1));
        assertRouteClear(start, goal, search.path(), clear);
    }

    @Test
    void rejectsDiagonalCornerCuttingEvenWhenEndpointsAreFree() {
        BiPredicate<Vec3, Vec3> clear = outside(new AABB(0.2, -1, -0.1, 0.4, 1, 0.35));
        Vec3 start = Vec3.ZERO;
        Vec3 goal = new Vec3(0.5, 0, 0.5);
        assertTrue(clear.test(start, start));
        assertTrue(clear.test(goal, goal));
        assertFalse(clear.test(start, goal));

        FlyNavigation.Search search = solve(start, goal, clear);
        assertNotNull(search.path());
        assertRouteClear(start, goal, search.path(), clear);
    }

    @Test
    void unreachableGoalExhaustsBoundedRequest() {
        FlyNavigation.Search search = solve(Vec3.ZERO, new Vec3(3, 0, 0),
                outside(new AABB(2.7, -0.3, -0.3, 3.3, 0.3, 0.3)));
        assertTrue(search.failed());
        assertNull(search.path());
        assertTrue(search.expandedNodes() <= 2048);
    }

    @Test
    void smallBudgetsRetainProgressAndZeroBudgetDoesNoWork() {
        FlyNavigation.Search search = new FlyNavigation.Search(Vec3.ZERO, new Vec3(3, 0, 0),
                outside(new AABB(1, -2, -1, 2, 2, 1)));
        search.advance(0);
        assertEquals(0, search.expandedNodes());
        assertFalse(search.failed());
        for (int i = 0; i < 2048 && search.path() == null && !search.failed(); i++) {
            int before = search.expandedNodes();
            search.advance(1);
            assertTrue(search.expandedNodes() - before <= 1);
        }
        assertNotNull(search.path());
    }

    @Test
    void followsMultipleSegmentsWithoutExceedingPerTickTravel() {
        BiPredicate<Vec3, Vec3> clear = outside(new AABB(1, -2, -1, 2, 2, 1));
        FlyNavigation.RoutePlanner planner = new FlyNavigation.RoutePlanner(clear);
        Vec3 position = Vec3.ZERO;
        Vec3 goal = new Vec3(3, 0, 0);
        for (long tick = 0; tick < 150 && position.distanceTo(goal) > 1.0E-6; tick++) {
            double speed = tick % 2 == 0 ? 0.3 : 0.5;
            List<Vec3> steps = planner.nextSteps(position, goal, tick, 64, speed);
            assertTrue(steps.stream().mapToDouble(Vec3::length).sum() <= speed + 1e-9);
            for (Vec3 delta : steps) {
                Vec3 next = position.add(delta);
                assertTrue(clear.test(position, next));
                position = next;
            }
        }
        assertEquals(0, position.distanceTo(goal), 1.0E-6);
    }

    @Test
    void movingGoalDuringCooldownDoesNotDiscardPendingSearch() {
        FlyNavigation.RoutePlanner planner = new FlyNavigation.RoutePlanner(
                outside(new AABB(1, -3, -3, 2, 3, 3)));
        Vec3 position = Vec3.ZERO;
        planner.nextSteps(position, new Vec3(3, 0, 0), 0, 1, 0.5);
        for (long tick = 1; tick < 10; tick++) {
            planner.nextSteps(position, new Vec3(3, tick * 0.01, 0), tick, 1, 0.5);
        }
        assertEquals(10, planner.expandedNodes());
    }

    @Test
    void failedRouteRetriesAfterCooldownWhenADetourOpens() {
        AtomicBoolean doorOpen = new AtomicBoolean();
        BiPredicate<Vec3, Vec3> wall = outside(new AABB(1, -2, -1, 2, 2, 1));
        FlyNavigation.RoutePlanner planner = new FlyNavigation.RoutePlanner(
                (from, to) -> doorOpen.get() && wall.test(from, to));
        Vec3 goal = new Vec3(3, 0, 0);
        assertTrue(planner.nextSteps(Vec3.ZERO, goal, 0, 2048, 0.5).isEmpty());
        doorOpen.set(true);
        assertTrue(planner.nextSteps(Vec3.ZERO, goal, 9, 2048, 0.5).isEmpty());
        assertFalse(planner.nextSteps(Vec3.ZERO, goal, 10, 2048, 0.5).isEmpty());
    }

    @Test
    void clearingFailedRoutePreservesReplanningCooldown() {
        AtomicBoolean doorOpen = new AtomicBoolean();
        BiPredicate<Vec3, Vec3> wall = outside(new AABB(1, -2, -1, 2, 2, 1));
        FlyNavigation.RoutePlanner planner = new FlyNavigation.RoutePlanner(
                (from, to) -> doorOpen.get() && wall.test(from, to));
        Vec3 goal = new Vec3(3, 0, 0);
        planner.nextSteps(Vec3.ZERO, goal, 0, 2048, 0.5);
        planner.clear();
        doorOpen.set(true);
        assertTrue(planner.nextSteps(Vec3.ZERO, goal, 9, 2048, 0.5).isEmpty());
        assertEquals(0, planner.expandedNodes());
        assertFalse(planner.nextSteps(Vec3.ZERO, goal, 10, 2048, 0.5).isEmpty());
    }

    @Test
    void sweptBodyUsesBottomCoordinatesAndAllowsSurfaceContact() {
        AABB block = new AABB(0, 0, 0, 1, 1, 1);
        assertTrue(FlyNavigation.bodyIntersects(block, new Vec3(-1, 0.5, 0.5),
                new Vec3(2, 0.5, 0.5), 0.25, 0.5));
        assertFalse(FlyNavigation.bodyIntersects(block, new Vec3(-1, 1, 0.5),
                new Vec3(2, 1, 0.5), 0.25, 0.5));
        assertTrue(FlyNavigation.bodyIntersects(block, new Vec3(0.5, -0.25, 0.5),
                new Vec3(0.5, -0.25, 0.5), 0.25, 0.5));
    }

    private static FlyNavigation.Search solve(Vec3 start, Vec3 goal, BiPredicate<Vec3, Vec3> clear) {
        FlyNavigation.Search search = new FlyNavigation.Search(start, goal, clear);
        search.advance(2048);
        return search;
    }

    private static void assertRouteClear(Vec3 start, Vec3 goal, List<Vec3> path,
                                         BiPredicate<Vec3, Vec3> clear) {
        Vec3 previous = start;
        for (Vec3 point : path) {
            assertTrue(clear.test(previous, point), () -> "Blocked edge to " + point);
            previous = point;
        }
        assertEquals(0, previous.distanceTo(goal), 1.0E-9);
    }

    private static BiPredicate<Vec3, Vec3> outside(AABB obstacle) {
        return (from, to) -> !SweptCollision.segmentAabb(from, to, obstacle).hit();
    }
}
