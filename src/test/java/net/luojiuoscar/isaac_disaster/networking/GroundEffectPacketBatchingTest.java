package net.luojiuoscar.isaac_disaster.networking;

import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import net.luojiuoscar.isaac_disaster.networking.packet.groundeffect.GroundEffectDespawnBatchS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.groundeffect.GroundEffectSnapshotBatchS2CPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GroundEffectPacketBatchingTest {
    @Test
    void emptyWaterBatchesStillCarryTheirEpochAndTick() {
        var spawn = (GroundEffectSnapshotBatchS2CPacket) split(new GroundEffectSnapshotBatchS2CPacket(8, 123, false, List.of())).get(0);
        assertEquals(8, spawn.epoch());
        assertEquals(123, spawn.serverTick());
        assertTrue(spawn.entries().isEmpty());
        var update = (GroundEffectSnapshotBatchS2CPacket) split(new GroundEffectSnapshotBatchS2CPacket(9, 124, true, List.of())).get(0);
        assertEquals(9, update.epoch());
        assertEquals(124, update.serverTick());
        assertTrue(update.entries().isEmpty());
        var despawn = (GroundEffectDespawnBatchS2CPacket) split(new GroundEffectDespawnBatchS2CPacket(10, 125, true, List.of())).get(0);
        assertEquals(10, despawn.epoch());
        assertEquals(125, despawn.serverTick());
        assertTrue(despawn.visibilityExit());
        assertTrue(despawn.identities().isEmpty());
    }

    @Test
    void twentyThousandUncutSpawnsPreserveEverySnapshotWithinPayloadBudget() {
        var entries = IntStream.range(0, 20_000).mapToObj(GroundEffectPacketBatchingTest::entry).toList();
        var parts = split(new GroundEffectSnapshotBatchS2CPacket(8, 123, false, entries));
        assertTrue(parts.size() > 1);
        List<GroundEffectSnapshotBatchS2CPacket.Entry> restored = new ArrayList<>();
        for (Object part : parts) {
            var decoded = roundTrip((GroundEffectSnapshotBatchS2CPacket) part,
                    GroundEffectSnapshotBatchS2CPacket::toBytes, GroundEffectSnapshotBatchS2CPacket::new);
            assertEquals(8, decoded.epoch());
            assertEquals(123, decoded.serverTick());
            restored.addAll(decoded.entries());
        }
        assertEquals(entries, restored);
    }

    @Test
    void twentyThousandUncutUpdatesPreserveEveryRevisionWithinPayloadBudget() {
        var entries = IntStream.range(0, 20_000).mapToObj(slot ->
                entry(slot)).toList();
        var parts = split(new GroundEffectSnapshotBatchS2CPacket(9, 124, true, entries));
        assertTrue(parts.size() > 1);
        List<GroundEffectSnapshotBatchS2CPacket.Entry> restored = new ArrayList<>();
        for (Object part : parts) {
            var decoded = roundTrip((GroundEffectSnapshotBatchS2CPacket) part,
                    GroundEffectSnapshotBatchS2CPacket::toBytes, GroundEffectSnapshotBatchS2CPacket::new);
            assertEquals(9, decoded.epoch());
            assertEquals(124, decoded.serverTick());
            restored.addAll(decoded.entries());
        }
        assertEquals(entries, restored);
    }

    @Test
    void despawnBurstPreservesReasonEpochTickAndAllIdentities() {
        var identities = LongStream.range(0, 100_000).boxed().toList();
        for (boolean visibilityExit : List.of(false, true)) {
            var parts = split(new GroundEffectDespawnBatchS2CPacket(10, 125, visibilityExit, identities));
            assertTrue(parts.size() > 1);
            List<Long> restored = new ArrayList<>();
            for (Object part : parts) {
                var decoded = roundTrip((GroundEffectDespawnBatchS2CPacket) part,
                        GroundEffectDespawnBatchS2CPacket::toBytes, GroundEffectDespawnBatchS2CPacket::new);
                assertEquals(10, decoded.epoch());
                assertEquals(125, decoded.serverTick());
                assertEquals(visibilityExit, decoded.visibilityExit());
                restored.addAll(decoded.identities());
            }
            assertEquals(identities, restored);
        }
    }

    private static GroundEffectSnapshotBatchS2CPacket.Entry entry(int slot) {
        return new GroundEffectSnapshotBatchS2CPacket.Entry(slot, 3, new Vec3(slot, 2, 4), 0.5D,
                7, 120, slot, null, ResourceLocation.parse("isaac_disaster:water"), 0x3C9BE8, slot);
    }

    static List<Object> split(Object packet) {
        List<Object> result = new ArrayList<>();
        PacketBatching.forEach(packet, result::add);
        return result;
    }

    static <P> P roundTrip(P packet, BiConsumer<P, FriendlyByteBuf> encoder,
                          Function<FriendlyByteBuf, P> decoder) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            encoder.accept(packet, buffer);
            assertTrue(buffer.readableBytes() + 5 <= 512 * 1024, "Include Forge discriminator");
            P decoded = decoder.apply(buffer);
            assertEquals(0, buffer.readableBytes());
            return decoded;
        } finally {
            buffer.release();
        }
    }
}
