package net.luojiuoscar.isaac_disaster.networking.packet;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PillRecordsSyncS2CPacket {
    private final int pillId;
    private final int effectId;

    //客户端构造时的函数
    public PillRecordsSyncS2CPacket(int itemId, int count){
        this.pillId = itemId;
        this.effectId = count;
    }

    //服务器接收时使用的构造函数（从缓冲区读取数据）
    public PillRecordsSyncS2CPacket(FriendlyByteBuf buf){
        this.pillId = buf.readInt();
        this.effectId = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf){
        buf.writeInt(pillId);
        buf.writeInt(effectId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {

            ClientDataManager.getInstance().setPillRecordsWithId(pillId, effectId);
        });
        return true;
    }

}