package net.luojiuoscar.isaac_disaster.networking.packet;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClearPassiveItemC2SPacket {

    //客户端构造时的函数
    public ClearPassiveItemC2SPacket(){
    }

    //服务器接收时使用的构造函数（从缓冲区读取数据）
    public ClearPassiveItemC2SPacket(FriendlyByteBuf buf){

    }

    public void toBytes(FriendlyByteBuf buf){

    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // On the server
            ServerPlayer player = context.getSender();

            player.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).ifPresent(
                    playerPassiveItem -> {playerPassiveItem.clearPlayerPassiveItems(player);
                    });

        });
        return true;
    }

}
