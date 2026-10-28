package net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.server;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class GroundEffectTrackingQueryTest {
    @Test
    void aDenseTrailSharesOneQueryWithoutShrinkingAnySearchArea() {
        var manager = new GroundEffectManager();
        var calls = new AtomicInteger();
        var shared = new ArrayList<LivingEntity>();
        Function<AABB, List<LivingEntity>> query = bounds -> {
            calls.incrementAndGet();
            assertEquals(-8, bounds.minX);
            assertEquals(16, bounds.maxX);
            assertEquals(63, bounds.minY);
            assertEquals(65, bounds.maxY);
            return shared;
        };
        for (int i = 0; i < 60; i++) {
            assertSame(shared, manager.trackingCandidates(search(new Vec3(0.1 + i * 0.1, 64.002, 1.5)), query));
        }
        assertEquals(1, calls.get());
    }

    @Test
    void crossingARegionOrFloorUsesAFreshCompleteQueryIncludingNegativeCoordinates() {
        var manager = new GroundEffectManager();
        var calls = new AtomicInteger();
        for (Vec3 center : List.of(new Vec3(-0.01, 64.002, -0.01), new Vec3(0.01, 64.002, -0.01),
                new Vec3(0.01, 64.002, 0.01), new Vec3(0.01, 66.002, 0.01))) {
            AABB requested = search(center);
            manager.trackingCandidates(requested, bounds -> {
                calls.incrementAndGet();
                assertTrue(bounds.minX <= requested.minX && bounds.maxX >= requested.maxX);
                assertTrue(bounds.minY <= requested.minY && bounds.maxY >= requested.maxY);
                assertTrue(bounds.minZ <= requested.minZ && bounds.maxZ >= requested.maxZ);
                return List.of();
            });
        }
        assertEquals(4, calls.get());
    }

    @Test
    void clearingAndDifferentWorldManagersCannotReuseOldCandidates() {
        var first = new GroundEffectManager();
        var second = new GroundEffectManager();
        var calls = new AtomicInteger();
        Function<AABB, List<LivingEntity>> query = bounds -> { calls.incrementAndGet(); return List.of(); };
        first.trackingCandidates(search(Vec3.ZERO), query);
        second.trackingCandidates(search(Vec3.ZERO), query);
        first.clear();
        first.trackingCandidates(search(Vec3.ZERO), query);
        assertEquals(3, calls.get());
    }

    private static AABB search(Vec3 position) {
        return new AABB(position, position).inflate(8, 0.51, 8);
    }
}
