package net.luojiuoscar.isaac_disaster.networking.packet;

import net.luojiuoscar.isaac_disaster.manager.item_managers.PassiveItemManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdatePlayerScaleS2CPacket {

    //客户端构造时的函数
    public UpdatePlayerScaleS2CPacket(){

    }

    //服务器接收时使用的构造函数（从缓冲区读取数据）
    public UpdatePlayerScaleS2CPacket(FriendlyByteBuf buf){
    }

    public void toBytes(FriendlyByteBuf buf){
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {

            Player player = Minecraft.getInstance().player;

            if (player != null){
                player.refreshDimensions();
            }

        });
        return true;
    }

}
