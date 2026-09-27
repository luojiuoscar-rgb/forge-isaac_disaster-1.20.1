package net.luojiuoscar.isaac_disaster.networking.packet;

import net.luojiuoscar.isaac_disaster.client.network.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FlyUpdateS2CPacket {
    private final int units;

    //客户端构造时的函数
    public FlyUpdateS2CPacket(int percentage){
        this.units = percentage;
    }

    //服务器接收时使用的构造函数（从缓冲区读取数据）
    public FlyUpdateS2CPacket(FriendlyByteBuf buf){
        this.units = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf){
        buf.writeInt(units);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context context = supplier.get();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> context.enqueueWork(() -> ClientPacketHandlers.handleFlyUpdate(units)));
        context.setPacketHandled(true);
        return true;
    }

}
