package net.luojiuoscar.isaac_disaster.networking.packet;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class RockBottomHistorySyncS2CPacket {
    private final Map<ResourceLocation, Double> history;

    public RockBottomHistorySyncS2CPacket(Map<ResourceLocation, Double> history) {
        this.history = new LinkedHashMap<>(history);
    }

    public RockBottomHistorySyncS2CPacket(FriendlyByteBuf buf) {
        int size = buf.readInt();
        this.history = new LinkedHashMap<>(size);
        for (int i = 0; i < size; i++) {
            this.history.put(buf.readResourceLocation(), buf.readDouble());
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(history.size());
        for (Map.Entry<ResourceLocation, Double> entry : history.entrySet()) {
            buf.writeResourceLocation(entry.getKey());
            buf.writeDouble(entry.getValue());
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientDataManager.getInstance().replaceRockBottomHistory(history));
        return true;
    }
}
