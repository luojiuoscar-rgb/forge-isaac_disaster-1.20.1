package net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.client;

import net.luojiuoscar.isaac_disaster.networking.packet.groundeffect.GroundEffectSnapshotBatchS2CPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.jupiter.api.Assertions.*;

class AquariusInterpolationTest {
    private GroundEffectSnapshotBatchS2CPacket.Entry entry(double x, int revision, int age) {
        return new GroundEffectSnapshotBatchS2CPacket.Entry(1, 1, new Vec3(x, 64.002, 0),
                0.5, age, 100, 1, new UUID(1, 2), ResourceLocation.parse("isaac_disaster:aquarius"),
                0x3C9BE8, 1, revision);
    }

    @Test
    void movementInterpolatesOverTwoTicksAndDuplicateSnapshotsDoNotRestartIt() {
        var nanos = new AtomicLong();
        var stream = new GroundEffectStream(nanos::get);
        stream.spawn(entry(0, 0, 0), 0);
        nanos.set(100_000_000L);
        assertTrue(stream.applyUpdate(entry(0.2, 1, 2), 2));
        var state = stream.get(1, 1);
        assertEquals(0, state.renderPosition(stream.localAnimationTick()).x, 1e-9);
        nanos.set(150_000_000L);
        assertEquals(0.1, state.renderPosition(stream.localAnimationTick()).x, 1e-9);
        assertFalse(stream.applyUpdate(entry(0.2, 1, 2), 2));
        nanos.set(200_000_000L);
        assertEquals(0.2, state.renderPosition(stream.localAnimationTick()).x, 1e-9);
        assertEquals(64.002, state.renderPosition(stream.localAnimationTick()).y, 1e-9);
        assertEquals(0.2, stream.get(1, 1).position().x, 1e-9);
        stream.clear();
        assertTrue(stream.states().isEmpty());
    }

    @Test
    void lateEntryUsesCurrentPositionAndAgeWithoutFlyingFromTheOrigin() {
        var stream = new GroundEffectStream(() -> 0L);
        var state = stream.spawn(entry(20, 9, 70), 500);
        assertEquals(20, state.renderPosition(stream.localAnimationTick()).x);
        assertEquals(70, state.age());
    }
}
