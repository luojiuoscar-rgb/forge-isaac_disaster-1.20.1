package net.luojiuoscar.isaac_disaster.networking.packet.bullet;

import net.luojiuoscar.isaac_disaster.bullet.client.ClientBulletRuntime;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public final class BulletSpawnBatchS2CPacket {
    private final List<BulletSpawnS2CPacket> entries;

    public BulletSpawnBatchS2CPacket(List<BulletSpawnS2CPacket> entries) {
        this.entries = entries == null ? List.of() : List.copyOf(entries);
    }

    public BulletSpawnBatchS2CPacket(FriendlyByteBuf buf) {
        this(buf.readList(BulletSpawnS2CPacket::new));
    }

    public List<BulletSpawnS2CPacket> entries() { return entries; }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeCollection(entries, (buffer, entry) -> entry.toBytes(buffer));
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> entries.forEach(ClientBulletRuntime.INSTANCE::spawn));
        context.setPacketHandled(true);
    }
}
