package net.luojiuoscar.isaac_disaster.networking.packet;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TrinketCountSyncS2CPacket {
    private final ResourceLocation id;
    private final int count;

    public TrinketCountSyncS2CPacket(ResourceLocation id, int count) {
        this.id = id;
        this.count = count;
    }

    public TrinketCountSyncS2CPacket(FriendlyByteBuf buf) {
        this.id = buf.readResourceLocation();
        this.count = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeResourceLocation(id);
        buf.writeInt(count);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientDataManager.getInstance().setTrinketCount(id, count));
        return true;
    }
}
