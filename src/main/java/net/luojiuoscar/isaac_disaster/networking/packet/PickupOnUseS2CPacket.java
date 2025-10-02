package net.luojiuoscar.isaac_disaster.networking.packet;

import net.luojiuoscar.isaac_disaster.manager.item_managers.PassiveItemManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PickupManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PickupOnUseS2CPacket {
    private int itemId;

    //客户端构造时的函数
    public PickupOnUseS2CPacket(int itemId){
        this.itemId = itemId;
    }

    //服务器接收时使用的构造函数（从缓冲区读取数据）
    public PickupOnUseS2CPacket(FriendlyByteBuf buf){
        this.itemId = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf){
        buf.writeInt(itemId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {

            Player player = Minecraft.getInstance().player;

            PickupManager.getInstance().getItemFromId(itemId).onUseClient(player);

        });
        return true;
    }

}
