package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class LaserPathTest {
    @Test void particleSpacingIsIndependentOfCollisionSubdivision() {
        List<Vec3> fine = new ArrayList<>(), coarse = new ArrayList<>();
        var a = new LaserPath.Particles(0.2);
        var b = new LaserPath.Particles(0.2);
        for (int i = 0; i < 600; i++)
            a.segment(new Vec3(i / 60.0, 0, 0), new Vec3((i + 1) / 60.0, 0, 0), fine::add);
        b.segment(Vec3.ZERO, new Vec3(10, 0, 0), coarse::add);
        assertEquals(coarse.size(), fine.size());
        assertTrue(fine.size() <= 51);
        for (int i = 0; i < fine.size(); i++) assertTrue(fine.get(i).distanceTo(coarse.get(i)) < 1e-9);
    }

    @Test void compactOnlyCollinearMotionWithEqualCostDensity() {
        var path = List.of(new Vec3(1, 0, 0), new Vec3(2, 0, 0), new Vec3(2, 1, 0),
                new Vec3(2, 2, 0), new Vec3(3, 2, 0), new Vec3(4, 2, 0));
        var compact = LaserPath.compact(Vec3.ZERO, path, List.of(1.0, 1.0, 0.0, 0.0, 0.0, 1.0), 0);
        assertEquals(4, compact.size());
        assertEquals(new Vec3(2, 0, 0), compact.get(0).end());
        assertEquals(2, compact.get(0).cost());
        assertEquals(new Vec3(2, 2, 0), compact.get(1).end());
        assertEquals(0, compact.get(2).cost());
        assertEquals(1, compact.get(3).cost());
    }

    @Test void curvesAndReversalsKeepEveryCollisionEdge() {
        var points = new ArrayList<Vec3>();
        for (int i = 1; i <= 64; i++) points.add(new Vec3(Math.cos(i * Math.PI / 32), Math.sin(i * Math.PI / 32), 0));
        assertEquals(64, LaserPath.compact(new Vec3(1, 0, 0), points, List.of(), 0).size());
        assertEquals(2, LaserPath.compact(Vec3.ZERO, List.of(new Vec3(1, 0, 0), Vec3.ZERO), List.of(), 2).size());
        assertTrue(LaserPath.compact(Vec3.ZERO, List.of(Vec3.ZERO), List.of(0.0), 0).isEmpty());
    }
}
