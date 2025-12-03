package net.luojiuoscar.isaac_disaster.networking.packet;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PillRecordsSyncS2CPacket {
    private final int pillId;
    private final ResourceLocation effectId;

    public PillRecordsSyncS2CPacket(int itemId, ResourceLocation count){
        this.pillId = itemId;
        this.effectId = count;
    }

    public PillRecordsSyncS2CPacket(FriendlyByteBuf buf){
        this.pillId = buf.readInt();
        this.effectId = buf.readResourceLocation();
    }

    public void toBytes(FriendlyByteBuf buf){
        buf.writeInt(pillId);
        buf.writeResourceLocation(effectId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {

            ClientDataManager.getInstance().setPillRecordsWithId(pillId, effectId);
        });
        return true;
    }

}