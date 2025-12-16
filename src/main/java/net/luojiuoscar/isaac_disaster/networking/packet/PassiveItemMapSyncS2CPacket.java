package net.luojiuoscar.isaac_disaster.networking.packet;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PassiveItemMapSyncS2CPacket {
    private final Map<Integer, Integer> itemMap;

    public PassiveItemMapSyncS2CPacket(Map<Integer, Integer> itemMap) {
        this.itemMap = itemMap;
    }

    public PassiveItemMapSyncS2CPacket(FriendlyByteBuf buf) {
        int size = buf.readInt();
        this.itemMap = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            int id = buf.readInt();
            int count = buf.readInt();
            itemMap.put(id, count);
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(itemMap.size());
        for (Map.Entry<Integer, Integer> entry : itemMap.entrySet()) {
            buf.writeInt(entry.getKey());
            buf.writeInt(entry.getValue());
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientDataManager clientData = ClientDataManager.getInstance();
            itemMap.forEach(clientData::setItemWithId); // 逐个同步
        });
        return true;
    }
}
