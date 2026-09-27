package net.luojiuoscar.isaac_disaster.registries.trajectory;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.luojiuoscar.isaac_disaster.registries.trajectory.impl.GravityTrajectoryModule;
import net.luojiuoscar.isaac_disaster.registries.trajectory.impl.RingWormTrajectoryModule;
import net.luojiuoscar.isaac_disaster.registries.trajectory.impl.WiggleWormTrajectoryModule;
import org.junit.jupiter.api.Test;

class TrajectoryRuntimeTest {
    @Test
    void moduleRejectsAnotherModulesStateAtItsBoundary() {
        var ring = new RingWormTrajectoryModule();
        TrajectoryState wrong = new WiggleWormTrajectoryModule.State();
        assertThrows(IllegalStateException.class, () -> {
            TrajectoryState ignored = ring.castState(wrong);
        });
    }

    @Test
    void childKeepsProgressButUsesItsOwnLaunchFrameAndIndependentState() {
        var id = ResourceLocation.parse("isaac_disaster:ring_worm");
        var removed = ResourceLocation.parse("test:removed");
        var specs = List.of(new TrajectorySpec(id, 1));
        var parent = new TrajectoryRuntime(new Vec3(1, 2, 3), new Vec3(1, 0, 0), specs);
        var phase = new RingWormTrajectoryModule.State();
        phase.phase(2.5);
        parent.states().put(id, phase);
        parent.states().put(removed, GravityTrajectoryModule.State.INSTANCE);
        parent.kinematics().initialized(true);
        parent.kinematics().distance(7);
        parent.distance(10);
        parent.compositionOffset(new Vec3(4, 0, 0));
        var snapshot = parent.snapshot();
        Vec3 spawn = new Vec3(20, 30, 40), axis = new Vec3(0, 1, 0);
        var child = TrajectoryRuntime.forSpawn(spawn, axis, specs, null, snapshot);
        assertEquals(spawn, child.origin());
        assertEquals(axis, child.launchDirection());
        assertEquals(10, child.distance());
        assertEquals(7, child.kinematics().distance());
        assertEquals(2.5, ((RingWormTrajectoryModule.State) child.states().get(id)).phase());
        assertTrue(child.suspended());
        assertEquals(Vec3.ZERO, child.compositionOffset());
        assertFalse(child.states().containsKey(removed));
        ((RingWormTrajectoryModule.State) child.states().get(id)).phase(90);
        child.kinematics().distance(100);
        assertEquals(2.5, ((RingWormTrajectoryModule.State) parent.states().get(id)).phase());
        assertEquals(7, parent.kinematics().distance());
        var restored = snapshot.restore();
        assertEquals(new Vec3(1, 2, 3), restored.origin());
        assertEquals(new Vec3(1, 0, 0), restored.launchDirection());
        assertEquals(new Vec3(4, 0, 0), restored.compositionOffset());
    }
}
