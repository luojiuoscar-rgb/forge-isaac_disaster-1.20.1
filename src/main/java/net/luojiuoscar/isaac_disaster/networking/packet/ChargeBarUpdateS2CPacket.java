package net.luojiuoscar.isaac_disaster.networking.packet;

import net.luojiuoscar.isaac_disaster.client.network.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
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
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> context.enqueueWork(() -> ClientPacketHandlers.handleChargeUpdate(progress)));
        context.setPacketHandled(true);
        return true;
    }

}
