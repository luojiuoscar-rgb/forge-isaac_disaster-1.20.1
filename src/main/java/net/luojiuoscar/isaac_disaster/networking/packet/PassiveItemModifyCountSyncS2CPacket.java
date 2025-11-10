package net.luojiuoscar.isaac_disaster.networking.packet;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PassiveItemModifyCountSyncS2CPacket {
    private final int itemId;
    private final int count;

    //客户端构造时的函数
    public PassiveItemModifyCountSyncS2CPacket(int itemId, int count){
        this.itemId = itemId;
        this.count = count;
    }

    //服务器接收时使用的构造函数（从缓冲区读取数据）
    public PassiveItemModifyCountSyncS2CPacket(FriendlyByteBuf buf){
        this.itemId = buf.readInt();
        this.count = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf){
        buf.writeInt(itemId);
        buf.writeInt(count);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientDataManager.getInstance().modifyItemCount(itemId, count));
        return true;
    }

}