package net.luojiuoscar.isaac_disaster.groundeffect.client;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.LongSupplier;
import net.luojiuoscar.isaac_disaster.networking.packet.groundeffect.GroundEffectSnapshotFragmentS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.groundeffect.GroundEffectSnapshotBatchS2CPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.ResourceLocationException;

/** Bounded, ordered assembly for the single reliable channel. Call only on the client main thread. */
public final class GroundEffectSnapshotAssembler {
    private static final long INACTIVE_NANOS = 30_000_000_000L;
    private final LongSupplier nanoTime;
    private byte[] bytes;
    private long latestTransferId;
    private long activeTransferId;
    private int epoch;
    private int serverTick;
    private boolean update;
    private int nextOffset;
    private long lastActivity;

    public GroundEffectSnapshotAssembler() { this(System::nanoTime); }

    public GroundEffectSnapshotAssembler(LongSupplier nanoTime) {
        this.nanoTime = Objects.requireNonNull(nanoTime, "nanoTime");
    }

    /**
     * Returns only a fully decoded entry. A newer transfer replaces unfinished old data; stale
     * transfers are ignored. Gaps, overlap, changed metadata or invalid bytes discard the active
     * transfer, whose id cannot restart. The caller validates epoch before calling this method.
     */
    public Optional<Result> accept(GroundEffectSnapshotFragmentS2CPacket fragment) {
        Objects.requireNonNull(fragment, "fragment");
        expireInactive();
        long id = fragment.transferId();
        if (id <= 0 || id < latestTransferId || (id == latestTransferId && bytes == null)) return Optional.empty();
        byte[] data = fragment.data();
        if (fragment.totalBytes() <= 0 || fragment.totalBytes() > GroundEffectSnapshotFragmentS2CPacket.MAX_SNAPSHOT_BYTES
                || fragment.offset() < 0 || data.length == 0
                || (long) fragment.offset() + data.length > fragment.totalBytes()) {
            latestTransferId = Math.max(latestTransferId, id);
            discardActive();
            return Optional.empty();
        }
        if (id > latestTransferId) {
            latestTransferId = id;
            discardActive();
            if (fragment.offset() != 0) return Optional.empty();
            bytes = new byte[fragment.totalBytes()];
            activeTransferId = id;
            epoch = fragment.epoch();
            serverTick = fragment.serverTick();
            update = fragment.update();
        }
        if (bytes == null || activeTransferId != id || fragment.epoch() != epoch
                || fragment.serverTick() != serverTick || fragment.update() != update
                || fragment.totalBytes() != bytes.length || fragment.offset() != nextOffset) {
            discardActive();
            return Optional.empty();
        }
        System.arraycopy(data, 0, bytes, nextOffset, data.length);
        nextOffset += data.length;
        lastActivity = nanoTime.getAsLong();
        if (nextOffset != bytes.length) return Optional.empty();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes));
        try {
            var entry = GroundEffectSnapshotBatchS2CPacket.Entry.read(buf);
            if (buf.isReadable()) return Optional.empty();
            return Optional.of(new Result(update, serverTick, entry));
        } catch (IndexOutOfBoundsException | IllegalArgumentException | DecoderException | ResourceLocationException malformedSnapshot) {
            // A corrupt protocol snapshot has no safe partial interpretation.
            return Optional.empty();
        } finally {
            buf.release();
            discardActive();
        }
    }

    /** Called from the client tick so an abandoned transfer also expires without further packets. */
    public void expireInactive() {
        if (bytes != null && nanoTime.getAsLong() - lastActivity >= INACTIVE_NANOS) discardActive();
    }

    /** World/epoch reset: also allows the new server session to start its transfer counter again. */
    public void clear() {
        discardActive();
        latestTransferId = 0;
    }

    private void discardActive() {
        bytes = null;
        nextOffset = 0;
        activeTransferId = 0;
    }

    public record Result(boolean update, int serverTick, GroundEffectSnapshotBatchS2CPacket.Entry entry) { }
}
