package net.luojiuoscar.isaac_disaster.networking.packet.laser;

import net.luojiuoscar.isaac_disaster.client.network.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

/** One short-lived packet containing the straight laser visuals created in one attack batch. */
public final class LaserBeamBatchS2CPacket {
    private final List<Beam> entries;

    public LaserBeamBatchS2CPacket(List<Beam> entries) {
        this.entries = entries == null ? List.of() : List.copyOf(entries);
    }

    public LaserBeamBatchS2CPacket(FriendlyByteBuf buf) {
        this(buf.readList(Beam::new));
    }

    public List<Beam> entries() {
        return entries;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeCollection(entries, (buffer, entry) -> entry.toBytes(buffer));
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        DistExecutor.unsafeRunWhenOn(
            Dist.CLIENT,
            () -> () -> context.enqueueWork(() -> ClientPacketHandlers.handleLaserBeamBatch(this)));
        context.setPacketHandled(true);
    }

    public record Beam(Vec3 start, Vec3 end, float width, int color) {
        public Beam {
            if (start == null || end == null) throw new IllegalArgumentException("Beam endpoints cannot be null");
            if (!Float.isFinite(width) || width <= 0.0F) throw new IllegalArgumentException("Beam width must be positive");
        }

        public Beam(FriendlyByteBuf buf) {
            this(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                buf.readFloat(), buf.readInt());
        }

        public void toBytes(FriendlyByteBuf buf) {
            buf.writeDouble(start.x);
            buf.writeDouble(start.y);
            buf.writeDouble(start.z);
            buf.writeDouble(end.x);
            buf.writeDouble(end.y);
            buf.writeDouble(end.z);
            buf.writeFloat(width);
            buf.writeInt(color);
        }
    }
}
