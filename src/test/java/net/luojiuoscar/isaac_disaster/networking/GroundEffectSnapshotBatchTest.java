package net.luojiuoscar.isaac_disaster.networking;

import io.netty.buffer.Unpooled;
import net.luojiuoscar.isaac_disaster.groundeffect.client.GroundEffectStream;
import net.luojiuoscar.isaac_disaster.groundeffect.core.GroundEffectCoverage;
import net.luojiuoscar.isaac_disaster.networking.packet.groundeffect.GroundEffectSnapshotBatchS2CPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GroundEffectSnapshotBatchTest {
    @Test
    void bothSnapshotOperationsPreserveTheirModeAndStateOnTheWire() {
        for (boolean update : List.of(false, true)) {
            var original = new GroundEffectSnapshotBatchS2CPacket(8, 123, update, List.of(entry(1, null, false)));
            var decoded = GroundEffectPacketBatchingTest.roundTrip(original,
                    GroundEffectSnapshotBatchS2CPacket::toBytes, GroundEffectSnapshotBatchS2CPacket::new);
            assertEquals(update, decoded.update());
            assertEquals(8, decoded.epoch());
            assertEquals(123, decoded.serverTick());
            assertEquals(original.entries(), decoded.entries());
        }
    }

    @Test
    void predictedByteSizeExactlyMatchesEncodingAcrossVarIntBoundariesAndCoverageForms() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            for (int value : new int[]{0, 127, 128, 16383, 16384, Integer.MAX_VALUE, -1, Integer.MIN_VALUE}) {
                for (UUID owner : new UUID[]{null, new UUID(1, 2)}) {
                    for (boolean clipped : List.of(false, true)) {
                        var snapshot = entry(value, owner, clipped);
                        buffer.clear();
                        snapshot.toBytes(buffer);
                        assertEquals(buffer.writerIndex(), snapshot.serializedBytes());
                    }
                }
            }
        } finally {
            buffer.release();
        }
    }

    @Test
    void mergedUpdateStillRequiresALiveInstanceAndCannotReviveATerminatedOne() {
        var stream = new GroundEffectStream();
        var snapshot = entry(1, null, false);
        assertFalse(stream.applyUpdate(snapshot, 100));
        var initial = new GroundEffectSnapshotBatchS2CPacket.Entry(snapshot.slot(), snapshot.generation(),
                snapshot.position(), snapshot.radius(), 0, 100, 0, null, snapshot.typeId(),
                snapshot.color(), snapshot.shapeSeed(), 0, snapshot.coverage());
        var live = stream.spawn(initial, 100);
        assertTrue(stream.applyUpdate(snapshot, 101));
        stream.despawn(live.identity());
        assertFalse(stream.applyUpdate(snapshot, 102));
        assertNull(stream.spawn(snapshot, 102));
    }

    private static GroundEffectSnapshotBatchS2CPacket.Entry entry(int value, UUID owner, boolean clipped) {
        var coverage = GroundEffectCoverage.puddle(0.5D, value);
        if (clipped) coverage = coverage.subtract(GroundEffectCoverage.puddle(0.25D, value + 1L), 0.1D, 0);
        return new GroundEffectSnapshotBatchS2CPacket.Entry(value, value, Vec3.ZERO, 0.5D, value, value,
                value, owner, ResourceLocation.parse("test:" + "a".repeat(130)), 0x3C9BE8, value, value, coverage);
    }
}
