package net.luojiuoscar.isaac_disaster.networking.packet;

import net.luojiuoscar.isaac_disaster.manager.PassiveItemManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ObtainPassiveItemS2CPacket {
    private int itemId;

    //客户端构造时的函数
    public ObtainPassiveItemS2CPacket(int itemId){
        this.itemId = itemId;
    }

    //服务器接收时使用的构造函数（从缓冲区读取数据）
    public ObtainPassiveItemS2CPacket(FriendlyByteBuf buf){
        this.itemId = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf){
        buf.writeInt(itemId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {

            Player player = Minecraft.getInstance().player;

            PassiveItemManager.getInstance().getItemFromId(itemId).onObtainClient(player);

        });
        return true;
    }

}
