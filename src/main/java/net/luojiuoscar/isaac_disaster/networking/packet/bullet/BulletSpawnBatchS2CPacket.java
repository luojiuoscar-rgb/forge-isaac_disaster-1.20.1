package net.luojiuoscar.isaac_disaster.networking.packet.bullet;

import net.luojiuoscar.isaac_disaster.client.network.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
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
        DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> context.enqueueWork(() -> ClientPacketHandlers.handleBulletSpawnBatch(this)));
        context.setPacketHandled(true);
    }
}
