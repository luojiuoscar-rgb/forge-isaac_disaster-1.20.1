package net.luojiuoscar.isaac_disaster.networking.packet;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SetCountSyncS2CPacket {
    private final int id;
    private final int count;

    public SetCountSyncS2CPacket(int id, int count){
        this.id = id;
        this.count = count;
    }

    public SetCountSyncS2CPacket(FriendlyByteBuf buf){
        this.id = buf.readInt();
        this.count = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf){
        buf.writeInt(id);
        buf.writeInt(count);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {

            ClientDataManager.getInstance().setSetCountWithId(id, count);
        });
        return true;
    }

}