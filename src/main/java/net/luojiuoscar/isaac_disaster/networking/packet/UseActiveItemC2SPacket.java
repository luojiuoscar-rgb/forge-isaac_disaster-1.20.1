package net.luojiuoscar.isaac_disaster.networking.packet;

import net.luojiuoscar.isaac_disaster.manager.ActiveItemManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UseActiveItemC2SPacket {
    private int itemId;
    private ItemStack stack;

    //客户端构造时的函数
    public UseActiveItemC2SPacket(int itemId){
        this.itemId = itemId;
    }

    //服务器接收时使用的构造函数（从缓冲区读取数据）
    public UseActiveItemC2SPacket(FriendlyByteBuf buf){
        this.itemId = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf){
        buf.writeInt(itemId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // On the server
            ServerPlayer player = context.getSender();

            ActiveItemManager.getInstance().getItemFromId(itemId).onReceiveC2SPacket(player, itemId);
        });
        return true;
    }

}
