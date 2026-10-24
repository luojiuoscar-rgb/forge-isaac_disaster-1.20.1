package net.luojiuoscar.isaac_disaster.networking.packet.groundeffect;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.network.FriendlyByteBuf;
import java.util.function.Function;
import java.util.function.Supplier;

/** A failed decode yields an inert epoch-zero packet, never a partially decoded world mutation. */
final class GroundEffectPacketCodec {
    private GroundEffectPacketCodec() { }

    static <T> T decode(FriendlyByteBuf buf, Function<FriendlyByteBuf, T> reader, Supplier<T> fallback) {
        try {
            T packet = reader.apply(buf);
            if (packet != null && !buf.isReadable()) return packet;
            IsaacDisaster.LOGGER.warn("Discarding invalid or trailing ground-effect packet data");
        } catch (RuntimeException malformed) {
            IsaacDisaster.LOGGER.warn("Discarding malformed ground-effect packet: {}", malformed.toString());
        }
        if (buf != null) buf.readerIndex(buf.writerIndex());
        return fallback.get();
    }
}
