package net.luojiuoscar.isaac_disaster.networking.packet;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SetCountSyncS2CPacket {
    private final int setId;
    private final int count;

    //客户端构造时的函数
    public SetCountSyncS2CPacket(int itemId, int count){
        this.setId = itemId;
        this.count = count;
    }

    //服务器接收时使用的构造函数（从缓冲区读取数据）
    public SetCountSyncS2CPacket(FriendlyByteBuf buf){
        this.setId = buf.readInt();
        this.count = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf){
        buf.writeInt(setId);
        buf.writeInt(count);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {

            ClientDataManager.getInstance().setSetCountWithId(setId, count);
        });
        return true;
    }

}