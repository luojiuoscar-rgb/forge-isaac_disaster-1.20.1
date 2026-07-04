package net.luojiuoscar.isaac_disaster.networking.packet;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class OpenIsaacItemScreenC2SPacket {

    //客户端构造时的函数
    public OpenIsaacItemScreenC2SPacket(){
    }

    //服务器接收时使用的构造函数（从缓冲区读取数据）
    public OpenIsaacItemScreenC2SPacket(FriendlyByteBuf buf){

    }

    public void toBytes(FriendlyByteBuf buf){

    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // On the server
            ServerPlayer player = context.getSender();

            if (player == null) return;
            // 从Capability获取被动物品列表
            player.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).ifPresent(passiveItems -> {
                List<ItemStack> passiveList = passiveItems.getPassiveItems();
                List<ItemStack> trinkets = passiveItems.getSwallowedTrinkets();

                Collections.reverse(passiveList); // 反转列表；确保最先获取的道具在最前
                Collections.reverse(trinkets);

                // 发回
                ModMessages.sentToPlayer(new OpenIsaacItemScreenS2CPacket(passiveList, trinkets), player);
            });

        });
        return true;
    }

}
