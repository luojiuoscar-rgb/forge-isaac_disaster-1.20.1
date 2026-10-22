package net.luojiuoscar.isaac_disaster.entity.familiar.impl.spider;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SpiderTargetAssignmentsTest {
    @Test
    void higherPriorityEnemyRecruitsEveryNearbySpiderBeforeDistanceIsConsidered() {
        var assignments = new SpiderTargetAssignments<String>();
        assignments.rebuild(List.of(spider(1, 0, 0, 0), spider(2, 2, 0, 0)),
                List.of(target("near", 10, 1, 1, 0, 0), target("threat", 20, 0, 7, 0, 0)));
        assertEquals("threat", assignments.target(1));
        assertEquals("threat", assignments.target(2));
    }

    @Test
    void samePriorityUsesNearestEnemyThenLowestEntityIdRegardlessOfInputOrder() {
        var assignments = new SpiderTargetAssignments<String>();
        var spiders = List.of(spider(1, 0, 0, 0), spider(2, 2, 0, 0));
        var left = target("left", 10, 1, -1, 0, 0);
        var right = target("right", 20, 1, 1, 0, 0);
        assignments.rebuild(spiders, List.of(right, left));
        assertEquals("left", assignments.target(1));
        assertEquals("right", assignments.target(2));
        assignments.rebuild(spiders, List.of(left, right));
        assertEquals("left", assignments.target(1));
        assertEquals("right", assignments.target(2));
    }

    @Test
    void recruitmentUsesThreeDimensionalEightBlockRadiusAcrossNegativeCells() {
        var assignments = new SpiderTargetAssignments<String>();
        assignments.rebuild(List.of(spider(1, -8, 0, 0), spider(2, 0, 8, 0),
                        spider(3, 0, 8.001, 0), spider(4, 6, 0, 6)),
                List.of(target("enemy", 10, 1, 0, 0, 0)));
        assertEquals("enemy", assignments.target(1));
        assertEquals("enemy", assignments.target(2));
        assertNull(assignments.target(3));
        assertNull(assignments.target(4));
    }

    @Test
    void eachEnemyRecruitsItsNearbyGroupRatherThanTheWholeOwnerRoster() {
        var assignments = new SpiderTargetAssignments<String>();
        assignments.rebuild(List.of(spider(1, -6, 0, 0), spider(2, -5, 0, 0),
                        spider(3, 6, 0, 0), spider(4, 5, 0, 0)),
                List.of(target("left", 10, 1, -7, 0, 0), target("right", 20, 1, 7, 0, 0)));
        assertEquals("left", assignments.target(1));
        assertEquals("left", assignments.target(2));
        assertEquals("right", assignments.target(3));
        assertEquals("right", assignments.target(4));
    }

    @Test
    void removingOneRecalledSpiderLeavesTheOtherGroupMembersAssigned() {
        var assignments = new SpiderTargetAssignments<String>();
        assignments.rebuild(List.of(spider(1, 0, 0, 0), spider(2, 1, 0, 0)),
                List.of(target("enemy", 10, 1, 2, 0, 0)));
        assignments.remove(1);
        assertNull(assignments.target(1));
        assertEquals("enemy", assignments.target(2));
    }

    @Test
    void rebuildingDropsSpidersAndEnemiesThatLeftTheFrame() {
        var assignments = new SpiderTargetAssignments<String>();
        assignments.rebuild(List.of(spider(1, 0, 0, 0)), List.of(target("enemy", 10, 1, 1, 0, 0)));
        assertEquals("enemy", assignments.target(1));
        assignments.rebuild(List.of(), List.of());
        assertNull(assignments.target(1));
    }

    private static SpiderTargetAssignments.Spider spider(int id, double x, double y, double z) {
        return new SpiderTargetAssignments.Spider(id, new Vec3(x, y, z));
    }

    private static SpiderTargetAssignments.Target<String> target(String value, int id, int priority,
                                                                  double x, double y, double z) {
        return new SpiderTargetAssignments.Target<>(value, id, priority, new Vec3(x, y, z));
    }
}
