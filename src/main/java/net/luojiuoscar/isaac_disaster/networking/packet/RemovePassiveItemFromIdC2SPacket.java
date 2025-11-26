package net.luojiuoscar.isaac_disaster.networking.packet;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RemovePassiveItemFromIdC2SPacket {
    private ResourceLocation itemId;

    //客户端构造时的函数
    public RemovePassiveItemFromIdC2SPacket(ResourceLocation itemId){
        this.itemId = itemId;
    }

    //服务器接收时使用的构造函数（从缓冲区读取数据）
    public RemovePassiveItemFromIdC2SPacket(FriendlyByteBuf buf){
        this.itemId = buf.readResourceLocation();
    }

    public void toBytes(FriendlyByteBuf buf){
        buf.writeResourceLocation(itemId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // On the server
            ServerPlayer player = context.getSender();

            player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                    playerPassiveItem -> {playerPassiveItem.removeFromId(player, itemId);
                    });

        });
        return true;
    }

}
