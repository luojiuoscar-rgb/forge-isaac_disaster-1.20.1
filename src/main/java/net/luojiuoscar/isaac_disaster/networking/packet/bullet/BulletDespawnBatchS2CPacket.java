package net.luojiuoscar.isaac_disaster.networking.packet.bullet;

import java.util.List;
import java.util.function.Supplier;
import net.luojiuoscar.isaac_disaster.client.network.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public final class BulletDespawnBatchS2CPacket {
    private final int epoch;
    private final int serverTick;
    private final boolean visibilityExit;
    private final List<Long> identities;

    public BulletDespawnBatchS2CPacket(int epoch, int serverTick, boolean visibilityExit, List<Long> identities) {
        this.epoch = epoch;
        this.serverTick = serverTick;
        this.visibilityExit = visibilityExit;
        this.identities = identities == null ? List.of() : List.copyOf(identities);
    }

    public BulletDespawnBatchS2CPacket(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readList(FriendlyByteBuf::readLong));
    }

    public int epoch() { return epoch; }
    public int serverTick() { return serverTick; }
    public boolean visibilityExit() { return visibilityExit; }
    public List<Long> identities() { return identities; }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(epoch);
        buf.writeVarInt(serverTick);
        buf.writeBoolean(visibilityExit);
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
