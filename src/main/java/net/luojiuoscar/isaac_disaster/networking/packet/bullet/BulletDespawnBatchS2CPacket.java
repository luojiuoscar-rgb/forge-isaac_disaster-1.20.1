package net.luojiuoscar.isaac_disaster.networking.packet.bullet;

import net.luojiuoscar.isaac_disaster.client.network.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public final class BulletDespawnBatchS2CPacket {
    private final int epoch;
    private final List<Long> identities;

    public BulletDespawnBatchS2CPacket(int epoch, List<Long> identities) {
        this.epoch = epoch;
        this.identities = identities == null ? List.of() : List.copyOf(identities);
    }

    public BulletDespawnBatchS2CPacket(List<Long> identities) {
        this(0, identities);
    }

    public BulletDespawnBatchS2CPacket(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readList(FriendlyByteBuf::readLong));
    }

    public int epoch() { return epoch; }
    public List<Long> identities() { return identities; }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(epoch);
        buf.writeCollection(identities, FriendlyByteBuf::writeLong);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> context.enqueueWork(() -> ClientPacketHandlers.handleBulletDespawnBatch(this)));
        context.setPacketHandled(true);
    }
}
