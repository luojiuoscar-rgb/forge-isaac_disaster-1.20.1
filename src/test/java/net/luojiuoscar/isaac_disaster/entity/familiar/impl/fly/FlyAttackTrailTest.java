package net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FlyAttackTrailTest {
    @Test
    void followsTheActualCornerInsteadOfCuttingTowardThePredecessor() {
        FlyAttackTrail trail = new FlyAttackTrail();
        trail.start(Vec3.ZERO);
        trail.append(new Vec3(1, 0, 0));
        trail.append(new Vec3(1, 0, 1));
        var steps = trail.steps(0.8, 2, 0.6);
        assertEquals(2, steps.size());
        assertEquals(0.2, steps.get(0).x, 1e-9);
        assertEquals(0, steps.get(0).z, 1e-9);
        assertEquals(0, steps.get(1).x, 1e-9);
        assertEquals(0.4, steps.get(1).z, 1e-9);
        assertEquals(0.6, steps.stream().mapToDouble(Vec3::length).sum(), 1e-9);
    }

    @Test
    void respectsPredecessorProgressAndTheMaximumSpeed() {
        FlyAttackTrail trail = new FlyAttackTrail();
        trail.start(Vec3.ZERO);
        trail.append(new Vec3(5, 0, 0));
        assertTrue(trail.steps(0, -0.3, 0.6).isEmpty());
        assertEquals(0.2, trail.steps(0, 0.2, 0.6).get(0).length(), 1e-9);
        assertEquals(0.6, trail.steps(0, 5, 10).get(0).length(), 1e-9);
    }

    @Test
    void discardsPassedHistoryAndBoundsAStalledTailsMemory() {
        FlyAttackTrail trail = new FlyAttackTrail();
        trail.start(Vec3.ZERO);
        for (int i = 1; i <= 400; i++) trail.append(new Vec3(i, 0, 0));
        trail.discardBefore(0);
        assertTrue(trail.startDistance() >= 145);
        assertEquals(400, trail.endDistance(), 1e-9);
        trail.discardBefore(390);
        assertEquals(390, trail.startDistance(), 1e-9);
        assertEquals(new Vec3(390, 0, 0), trail.entrance());
    }
}
