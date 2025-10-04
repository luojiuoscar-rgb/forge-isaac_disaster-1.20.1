package net.luojiuoscar.isaac_disaster.networking.packet;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FlyUpdateS2CPacket {
    private int units;

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
        context.enqueueWork(() -> {

            Player player = Minecraft.getInstance().player;
            if (player == null) return;

            ClientDataManager.getInstance().setFlyPercentage(units);
        });
        return true;
    }

}