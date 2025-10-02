package net.luojiuoscar.isaac_disaster.networking.packet;

import net.luojiuoscar.isaac_disaster.manager.ClientDataManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PassiveItemSyncS2CPacket {
    private int itemId;
    private int count;

    //客户端构造时的函数
    public PassiveItemSyncS2CPacket(int itemId, int count){
        this.itemId = itemId;
        this.count = count;
    }

    //服务器接收时使用的构造函数（从缓冲区读取数据）
    public PassiveItemSyncS2CPacket(FriendlyByteBuf buf){
        this.itemId = buf.readInt();
        this.count = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf){
        buf.writeInt(itemId);
        buf.writeInt(count);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {

            ClientDataManager.getInstance().setItemWithId(itemId, count);
        });
        return true;
    }

}