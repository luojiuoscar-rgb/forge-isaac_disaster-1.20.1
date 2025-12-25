package net.luojiuoscar.isaac_disaster.networking.packet;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ChargeBarUpdateS2CPacket {
    private final float progress;

    public ChargeBarUpdateS2CPacket(float progress){
        this.progress = progress;
    }

    public ChargeBarUpdateS2CPacket(FriendlyByteBuf buf){
        this.progress = buf.readFloat();
    }

    public void toBytes(FriendlyByteBuf buf){
        buf.writeFloat(progress);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {

            Player player = Minecraft.getInstance().player;
            if (player == null) return;

            ClientDataManager.getInstance().setChargeProgress(progress);
        });
        return true;
    }

}