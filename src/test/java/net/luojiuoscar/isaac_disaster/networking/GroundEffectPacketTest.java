package net.luojiuoscar.isaac_disaster.networking;

import io.netty.buffer.Unpooled;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.client.GroundEffectStream;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.core.AbstractGroundEffect;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.core.GroundEffectContact;
import net.luojiuoscar.isaac_disaster.registries.ground_effect.GroundEffectType;
import net.luojiuoscar.isaac_disaster.networking.packet.groundeffect.GroundEffectDespawnBatchS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.groundeffect.GroundEffectSnapshotBatchS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.groundeffect.GroundEffectSyncS2CPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.GroundEffectTestFixtures.*;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroundEffectPacketTest {
    @Test
    void sessionResetAndClockOnlySamplesPreserveTheirWorldAndTick() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            for (boolean reset : new boolean[]{true, false}) {
                buffer.clear();
                var packet = new GroundEffectSyncS2CPacket(8, ResourceLocation.parse("minecraft:the_nether"), 123, reset);
                packet.toBytes(buffer);
                assertEquals(packet, new GroundEffectSyncS2CPacket(buffer));
                assertEquals(0, buffer.readableBytes());
            }
        } finally {
            buffer.release();
        }
    }
    @Test
    void effectProvidedColorReachesTheClientThroughSpawnAndUpdate() {
        var type = new GroundEffectType<>(TintedEffect::new);
        var effect = type.create(1, 2, null, Vec3.ZERO, 0.5D, 100, 1.0F, 3);
        var stream = new GroundEffectStream();
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            new GroundEffectSnapshotBatchS2CPacket(1, 0, false,
                    List.of(snapshot(effect))).toBytes(buffer);
            var spawn = new GroundEffectSnapshotBatchS2CPacket(buffer);
            assertEquals(0xA040E0, stream.spawn(spawn.entries().get(0)).getColor());

            buffer.clear();
            effect.color = 0x40D080;
            effect.repaint(4);
            new GroundEffectSnapshotBatchS2CPacket(1, 1, true, List.of(
                    snapshot(effect))).toBytes(buffer);
            var update = new GroundEffectSnapshotBatchS2CPacket(buffer);
            assertTrue(stream.applyUpdate(update.entries().get(0)));
            assertEquals(0x40D080, stream.get(1, 2).getColor());
        } finally {
            buffer.release();
        }
    }

    private static final class TintedEffect extends AbstractGroundEffect {
        private int color = 0xA040E0;
        TintedEffect(GroundEffectType<TintedEffect> type, int slot, int generation, UUID owner,
                     Vec3 position, double radius, int lifetime, float damage, long order, int animationDurationTicks) {
            super(type, slot, generation, owner, position, radius, lifetime, damage, order, animationDurationTicks);
        }
        @Override public int getColor() { return color; }
        @Override public void onContactEffect(GroundEffectContact contact) { }
    }

    @Test
    void spawnPacketRoundTripPreservesWaterState() {
        var original = new GroundEffectSnapshotBatchS2CPacket(7, 100, false, List.of(
                new GroundEffectSnapshotBatchS2CPacket.Entry(2, 4, new Vec3(1, 2, 3), 0.5,
                        8, 100, 12, new UUID(4, 5),
                        ResourceLocation.fromNamespaceAndPath("isaac_disaster", "water"), 0x3C9BE8, 12)));
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            original.toBytes(buffer);
            var decoded = new GroundEffectSnapshotBatchS2CPacket(buffer);
            assertEquals(original.epoch(), decoded.epoch());
            assertEquals(original.serverTick(), decoded.serverTick());
            assertEquals(original.entries(), decoded.entries());
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }

    @Test
    void despawnAndUpdatePacketsRoundTripTheirIdentity() {
        FriendlyByteBuf despawnBuffer = new FriendlyByteBuf(Unpooled.buffer());
        FriendlyByteBuf updateBuffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            var despawn = new GroundEffectDespawnBatchS2CPacket(2, 3, true, List.of(4L, 5L));
            despawn.toBytes(despawnBuffer);
            var decodedDespawn = new GroundEffectDespawnBatchS2CPacket(despawnBuffer);
            assertEquals(despawn.identities(), decodedDespawn.identities());
            assertEquals(despawn.visibilityExit(), decodedDespawn.visibilityExit());

            var update = new GroundEffectSnapshotBatchS2CPacket(2, 3, true,
                    List.of(
                            new GroundEffectSnapshotBatchS2CPacket.Entry(7, 8, new Vec3(9, 10, 11),
                                    0.5D, 0, 100, 3, null,
                                    ResourceLocation.fromNamespaceAndPath("isaac_disaster", "water"),
                                    0x3C9BE8, 3, 2, 10, 5)));
            update.toBytes(updateBuffer);
            var decodedUpdate = new GroundEffectSnapshotBatchS2CPacket(updateBuffer);
            assertEquals(update.entries(), decodedUpdate.entries());
            assertEquals(0, updateBuffer.readableBytes());
        } finally {
            despawnBuffer.release();
            updateBuffer.release();
        }
    }
}
