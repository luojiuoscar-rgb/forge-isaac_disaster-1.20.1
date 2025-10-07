package net.luojiuoscar.isaac_disaster.networking.packet;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SetRightClickC2SPacket {
    private boolean isRightClick;

    //客户端构造时的函数
    public SetRightClickC2SPacket(boolean isRightClick){
        this.isRightClick = isRightClick;
    }

    //服务器接收时使用的构造函数（从缓冲区读取数据）
    public SetRightClickC2SPacket(FriendlyByteBuf buf){
        this.isRightClick = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf){
        buf.writeBoolean(this.isRightClick);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // On the server
            ServerPlayer player = context.getSender();

            player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                    playerAbility -> playerAbility.setHoldRightClick(isRightClick)
                    );

        });
        return true;
    }

}
