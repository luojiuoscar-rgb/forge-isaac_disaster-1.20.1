package net.luojiuoscar.isaac_disaster.networking;

import io.netty.buffer.Unpooled;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.networking.packet.bullet.*;
import net.luojiuoscar.isaac_disaster.networking.packet.laser.LaserBeamBatchS2CPacket;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/** Dispatches outbound batches through a payload size boundary. */
public final class PacketBatching {
    // Leave ample room below vanilla's 1 MiB custom payload limit. Header budgets
    // include the Forge discriminator and five bytes for each VarInt/list count.
    private static final int MAX_PAYLOAD_BYTES = 512 * 1024;
    private PacketBatching() {}

    public static void forEach(Object packet, Consumer<Object> sender) {
        if (packet instanceof BulletSpawnBatchS2CPacket batch) {
            split(batch.entries(), (buf, entry) -> entry.toBytes(buf), BulletSpawnBatchS2CPacket::new, 10, sender);
        } else if (packet instanceof BulletCorrectionBatchS2CPacket batch) {
            split(batch.entries(), (buf, entry) -> entry.toBytes(buf),
                    entries -> new BulletCorrectionBatchS2CPacket(batch.epoch(), entries), 15, sender);
        } else if (packet instanceof BulletDespawnBatchS2CPacket batch) {
            split(batch.identities(), FriendlyByteBuf::writeLong,
                    entries -> new BulletDespawnBatchS2CPacket(batch.epoch(), entries), 15, sender);
        } else if (packet instanceof BulletShatterS2CPacket batch) {
            split(batch.entries(), (buf, entry) -> entry.toBytes(buf),
                    entries -> new BulletShatterS2CPacket(batch.epoch(), entries), 15, sender);
        } else if (packet instanceof BulletTrackingBatchS2CPacket batch) {
            splitTracking(batch, sender);
        } else if (packet instanceof LaserBeamBatchS2CPacket batch) {
            split(batch.entries(), (buf, entry) -> entry.toBytes(buf), LaserBeamBatchS2CPacket::new, 10, sender);
        } else {
            sender.accept(packet);
        }
    }

    private static void splitTracking(BulletTrackingBatchS2CPacket batch, Consumer<Object> sender) {
        long upperBound = 30L + 29L * batch.targets().size() + 16L * batch.assignments().size()
                + 58L * batch.velocitySamples().size();
        if (upperBound <= MAX_PAYLOAD_BYTES) {
            sender.accept(batch);
            return;
        }
        // The client only installs target samples for handles referenced by a live
        // bullet. Send every assignment before targets, then the velocity samples.
        split(batch.assignments(), (buf, entry) -> entry.toBytes(buf),
                entries -> new BulletTrackingBatchS2CPacket(batch.epoch(), batch.sampleTick(), List.of(), entries, List.of()),
                30, sender);
        split(batch.targets(), (buf, entry) -> entry.toBytes(buf),
                entries -> new BulletTrackingBatchS2CPacket(batch.epoch(), batch.sampleTick(), entries, List.of(), List.of()),
                30, sender);
        split(batch.velocitySamples(), (buf, entry) -> entry.toBytes(buf),
                entries -> new BulletTrackingBatchS2CPacket(batch.epoch(), batch.sampleTick(), List.of(), List.of(), entries),
                30, sender);
    }

    /** Measures each entry once; packet factories take their own immutable list copy. */
    private static <E> void split(List<E> entries, BiConsumer<FriendlyByteBuf, E> encoder,
                                 Function<List<E>, Object> factory, int headerBytes, Consumer<Object> sender) {
        if (entries.isEmpty()) {
            sender.accept(factory.apply(List.of()));
            return;
        }
        FriendlyByteBuf scratch = new FriendlyByteBuf(Unpooled.buffer());
        List<E> part = new ArrayList<>();
        int bytes = headerBytes;
        int oversized = 0;
        try {
            for (E entry : entries) {
                scratch.clear();
                encoder.accept(scratch, entry);
                int entryBytes = scratch.writerIndex();
                if (entryBytes > MAX_PAYLOAD_BYTES - headerBytes) {
                    oversized++;
                    continue;
                }
                if (bytes + entryBytes > MAX_PAYLOAD_BYTES) {
                    sender.accept(factory.apply(part));
                    part.clear();
                    bytes = headerBytes;
                }
                part.add(entry);
                bytes += entryBytes;
            }
            if (!part.isEmpty()) sender.accept(factory.apply(part));
        } finally {
            scratch.release();
        }
        if (oversized > 0) IsaacDisaster.LOGGER.warn("Discarded {} projectile sync entries exceeding the single-entry payload budget", oversized);
    }
}
