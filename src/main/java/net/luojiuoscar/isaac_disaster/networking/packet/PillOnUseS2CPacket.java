package net.luojiuoscar.isaac_disaster.networking.packet;

import net.luojiuoscar.isaac_disaster.manager.item_managers.PillEffectManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PillOnUseS2CPacket {
    private int pillEffectId;
    private boolean isHorsePill;

    //客户端构造时的函数
    public PillOnUseS2CPacket(int pillEffectId, boolean isHorsePill){
        this.pillEffectId = pillEffectId;
        this.isHorsePill = isHorsePill;
    }

    //服务器接收时使用的构造函数（从缓冲区读取数据）
    public PillOnUseS2CPacket(FriendlyByteBuf buf){
        this.pillEffectId = buf.readInt();
        this.isHorsePill = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf){
        buf.writeInt(pillEffectId);
        buf.writeBoolean(isHorsePill);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {

            Player player = Minecraft.getInstance().player;

            if (isHorsePill){
                PillEffectManager.getInstance().getEffectFromEffectId(pillEffectId).onUseClientH(player);
            }else{
                PillEffectManager.getInstance().getEffectFromEffectId(pillEffectId).onUseClient(player);
            }

        });
        return true;
    }

}
