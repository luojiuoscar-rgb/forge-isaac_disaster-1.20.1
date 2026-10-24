package net.luojiuoscar.isaac_disaster.networking;

import io.netty.buffer.Unpooled;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.client.GroundEffectStream;
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
            var original = new GroundEffectSnapshotBatchS2CPacket(8, 123, update, List.of(entry(1, null)));
            var decoded = GroundEffectPacketBatchingTest.roundTrip(original,
                    GroundEffectSnapshotBatchS2CPacket::toBytes, GroundEffectSnapshotBatchS2CPacket::new);
            assertEquals(update, decoded.update());
            assertEquals(8, decoded.epoch());
            assertEquals(123, decoded.serverTick());
            assertEquals(original.entries(), decoded.entries());
        }
    }

    @Test
    void predictedByteSizeExactlyMatchesEncodingAcrossVarIntBoundaries() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            for (int value : new int[]{0, 127, 128, 16383, 16384, Integer.MAX_VALUE, -1, Integer.MIN_VALUE}) {
                for (UUID owner : new UUID[]{null, new UUID(1, 2)}) {
                    var fixture = entry(value, owner);
                    for (int duration : new int[]{0, value}) {
                        var snapshot = new GroundEffectSnapshotBatchS2CPacket.Entry(fixture.slot(), fixture.generation(),
                                fixture.position(), fixture.radius(), fixture.age(), fixture.lifetime(), fixture.creationOrder(),
                                fixture.ownerUuid(), fixture.typeId(), fixture.color(), fixture.shapeSeed(), fixture.revision(),
                                duration, Math.max(0, value));
                        buffer.clear();
                        snapshot.toBytes(buffer);
                        assertEquals(buffer.writerIndex(), snapshot.serializedBytes());
                        assertEquals(snapshot, GroundEffectSnapshotBatchS2CPacket.Entry.read(buffer));
                        assertEquals(0, buffer.readableBytes());
                    }
                }
            }
        } finally { buffer.release(); }
    }

    @Test
    void maximumLengthTypeMetadataFitsAnOrdinarySnapshotPacket() {
        var entry = new GroundEffectSnapshotBatchS2CPacket.Entry(1, 1, Vec3.ZERO, 1, 0, 100,
                1, new UUID(1, 2), ResourceLocation.parse("t:" + "a".repeat(32765)), 0x3C9BE8, 1,
                1, 100, 50);
        var parts = GroundEffectPacketBatchingTest.split(
                new GroundEffectSnapshotBatchS2CPacket(8, 100, false, List.of(entry)));
        assertEquals(1, parts.size());
        var packet = assertInstanceOf(GroundEffectSnapshotBatchS2CPacket.class, parts.get(0));
        var decoded = GroundEffectPacketBatchingTest.roundTrip(packet,
                GroundEffectSnapshotBatchS2CPacket::toBytes, GroundEffectSnapshotBatchS2CPacket::new);
        assertEquals(List.of(entry), decoded.entries());
    }

    @Test
    void mergedUpdateStillRequiresALiveInstanceAndCannotReviveATerminatedOne() {
        var stream = new GroundEffectStream();
        var snapshot = entry(1, null);
        assertFalse(stream.applyUpdate(snapshot, 100));
        var initial = new GroundEffectSnapshotBatchS2CPacket.Entry(snapshot.slot(), snapshot.generation(),
                snapshot.position(), snapshot.radius(), 0, 100, 0, null, snapshot.typeId(),
                snapshot.color(), snapshot.shapeSeed(), 0);
        var live = stream.spawn(initial, 100);
        assertTrue(stream.applyUpdate(snapshot, 101));
        stream.despawn(live.identity());
        assertFalse(stream.applyUpdate(snapshot, 102));
        assertNull(stream.spawn(snapshot, 102));
    }

    private static GroundEffectSnapshotBatchS2CPacket.Entry entry(int value, UUID owner) {
        return new GroundEffectSnapshotBatchS2CPacket.Entry(value, value, Vec3.ZERO, 0.5D, value, value,
                value, owner, ResourceLocation.parse("test:" + "a".repeat(130)), 0x3C9BE8, value, value);
    }
}
