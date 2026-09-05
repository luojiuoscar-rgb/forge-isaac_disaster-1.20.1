package net.luojiuoscar.isaac_disaster.networking.packet.bullet;

import net.luojiuoscar.isaac_disaster.bullet.client.ClientBulletRuntime;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public final class BulletCorrectionBatchS2CPacket {
    private final int epoch;
    private final List<BulletCorrectionS2CPacket> entries;

    public BulletCorrectionBatchS2CPacket(int epoch, List<BulletCorrectionS2CPacket> entries) {
        this.epoch = epoch;
        this.entries = entries == null ? List.of() : List.copyOf(entries);
    }

    public BulletCorrectionBatchS2CPacket(List<BulletCorrectionS2CPacket> entries) {
        this(entries == null || entries.isEmpty() ? 0 : entries.get(0).epoch(), entries);
    }

    public BulletCorrectionBatchS2CPacket(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readList(BulletCorrectionS2CPacket::new));
    }

    public int epoch() { return epoch; }
    public List<BulletCorrectionS2CPacket> entries() { return entries; }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(epoch);
        buf.writeCollection(entries, (buffer, entry) -> entry.toBytes(buffer));
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            if (!ClientBulletRuntime.INSTANCE.acceptEpoch(epoch)) return;
            entries.forEach(ClientBulletRuntime.INSTANCE::correct);
        });
        context.setPacketHandled(true);
    }
}
