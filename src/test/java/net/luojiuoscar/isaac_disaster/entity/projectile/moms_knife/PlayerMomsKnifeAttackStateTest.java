package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlayerMomsKnifeAttackStateTest {
    @Test
    void traveledDistanceIncludesTheApexEvenWhenTheEndpointReturnsToTheStart() {
        var state = state();
        var start = PlayerMomsKnifeMotion.blade(Vec3.ZERO, PlayerMomsKnifeMotion.FORWARD);
        var apex = PlayerMomsKnifeMotion.blade(new Vec3(0, 0, 2), PlayerMomsKnifeMotion.FORWARD);
        var runtime = state.trajectory;
        state.advance(List.of(start, apex, start), 100);
        assertEquals(4, state.traveled, 1e-10);
        assertEquals(Vec3.ZERO, state.velocity);
        assertEquals(1, state.age);
        assertSame(runtime, state.trajectory);
        state.advance(List.of(start, apex), 100);
        assertEquals(4, state.traveled, 1e-10);
    }

    @Test
    void blockMemoryLastsForTheThrowWhileEntityDamageMemoryResetsEachTick() {
        var state = state();
        UUID target = UUID.randomUUID();
        BlockPos block = new BlockPos(1, 2, 3);
        assertTrue(state.collisions.markBlockHit(block));
        assertFalse(state.collisions.markBlockHit(block));
        state.collisions.damagedEntities().add(target);
        var blade = PlayerMomsKnifeMotion.blade(Vec3.ZERO, PlayerMomsKnifeMotion.FORWARD);
        state.advance(List.of(blade, blade), 1);
        assertEquals(0, state.collisions.damagedEntities().size());
        assertTrue(state.collisions.hitBlockPositions().contains(block));
        assertThrows(UnsupportedOperationException.class, () -> state.collisions.hitBlockPositions().clear());
        assertTrue(state().collisions.markBlockHit(block));
        assertTrue(state.splits.isEmpty());
        assertEquals(0, state.splitCounts.getBlockHits());
        assertTrue(state.trajectory.specs().isEmpty());
    }

    private static PlayerMomsKnifeAttackState state() {
        return new PlayerMomsKnifeAttackState(null, Vec3.ZERO, PlayerMomsKnifeMotion.FORWARD, 2,
                PlayerMomsKnifeMotion.FORWARD);
    }
}
