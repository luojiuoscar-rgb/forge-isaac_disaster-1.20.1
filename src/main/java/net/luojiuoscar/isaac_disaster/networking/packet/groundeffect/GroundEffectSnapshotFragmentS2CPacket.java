package net.luojiuoscar.isaac_disaster.networking.packet.groundeffect;

import java.util.Objects;
import java.util.function.Supplier;
import net.luojiuoscar.isaac_disaster.client.network.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/** One ordered byte range of a complete spawn/update entry; never a renderable partial snapshot. */
public final class GroundEffectSnapshotFragmentS2CPacket {
    public static final int MAX_SNAPSHOT_BYTES = 32 * 1024 * 1024;
    public static final int MAX_FRAGMENT_BYTES = 480 * 1024;
    private final int epoch;
    private final int serverTick;
    private final boolean update;
    private final long transferId;
    private final int totalBytes;
    private final int offset;
    private final byte[] data;

    public GroundEffectSnapshotFragmentS2CPacket(int epoch, int serverTick, boolean update, long transferId,
                                               int totalBytes, int offset, byte[] data) {
        Objects.requireNonNull(data, "data");
        if (data.length > MAX_FRAGMENT_BYTES) throw new IllegalArgumentException("Ground-effect fragment exceeds payload budget");
        this.epoch = epoch;
        this.serverTick = serverTick;
        this.update = update;
        this.transferId = transferId;
        this.totalBytes = totalBytes;
        this.offset = offset;
        this.data = data.clone();
    }

    public GroundEffectSnapshotFragmentS2CPacket(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readLong(),
                buf.readVarInt(), buf.readVarInt(), buf.readByteArray(MAX_FRAGMENT_BYTES));
    }

    public int epoch() { return epoch; }
    public int serverTick() { return serverTick; }
    public boolean update() { return update; }
    public long transferId() { return transferId; }
    public int totalBytes() { return totalBytes; }
    public int offset() { return offset; }
    public byte[] data() { return data.clone(); }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(epoch);
        buf.writeVarInt(serverTick);
        buf.writeBoolean(update);
        buf.writeLong(transferId);
        buf.writeVarInt(totalBytes);
        buf.writeVarInt(offset);
        buf.writeByteArray(data);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> context.enqueueWork(() -> ClientPacketHandlers.handleGroundEffectSnapshotFragment(this)));
        context.setPacketHandled(true);
    }
}
