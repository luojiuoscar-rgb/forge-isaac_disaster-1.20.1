package net.luojiuoscar.isaac_disaster.networking.packet;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PillQualitySyncS2CPacket {
    private final int pillQuality;

    //客户端构造时的函数
    public PillQualitySyncS2CPacket(int pillQuality){
        this.pillQuality = pillQuality;
    }

    //服务器接收时使用的构造函数（从缓冲区读取数据）
    public PillQualitySyncS2CPacket(FriendlyByteBuf buf){
        this.pillQuality = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf){
        buf.writeInt(pillQuality);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {

            ClientDataManager.getInstance().setPillQuality(pillQuality);
        });
        return true;
    }

}