package net.luojiuoscar.isaac_disaster.networking.packet;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class IsaacItemCountMapSyncS2CPacket {
    private final Map<ResourceLocation, Integer> itemMap;
    private final Map<ResourceLocation, Integer> trinketMap;

    public IsaacItemCountMapSyncS2CPacket(Map<ResourceLocation, Integer> itemMap,
                                          Map<ResourceLocation, Integer> trinketMap) {
        this.itemMap = itemMap;
        this.trinketMap = trinketMap;
    }

    public IsaacItemCountMapSyncS2CPacket(FriendlyByteBuf buf) {
        int size = buf.readInt();
        this.itemMap = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            itemMap.put(buf.readResourceLocation(), buf.readInt());
        }

        int trinketSize = buf.readInt();
        this.trinketMap = new HashMap<>(trinketSize);
        for (int i = 0; i < trinketSize; i++) {
            trinketMap.put(buf.readResourceLocation(), buf.readInt());
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(itemMap.size());
        itemMap.forEach((id, count) -> {
            buf.writeResourceLocation(id);
            buf.writeInt(count);
        });
        buf.writeInt(trinketMap.size());
        trinketMap.forEach((id, count) -> {
            buf.writeResourceLocation(id);
            buf.writeInt(count);
        });
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientDataManager clientData = ClientDataManager.getInstance();
            clientData.resetItemCountMap();
            clientData.resetTrinketCountMap();
            itemMap.forEach(clientData::setItemCount);
            trinketMap.forEach(clientData::setTrinketCount);
        });
        return true;
    }
}
