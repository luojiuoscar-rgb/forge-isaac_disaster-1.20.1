package net.luojiuoscar.isaac_disaster.networking.packet;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ReviveHudSyncS2CPacket {
    private final List<ResourceLocation> icons;

    public ReviveHudSyncS2CPacket(List<ResourceLocation> icons) {
        this.icons = List.copyOf(icons);
    }

    public ReviveHudSyncS2CPacket(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<ResourceLocation> readIcons = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            readIcons.add(buffer.readResourceLocation());
        }
        this.icons = List.copyOf(readIcons);
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeVarInt(icons.size());
        icons.forEach(buffer::writeResourceLocation);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientDataManager.getInstance().replaceReviveHudIcons(icons));
        context.setPacketHandled(true);
        return true;
    }
}
