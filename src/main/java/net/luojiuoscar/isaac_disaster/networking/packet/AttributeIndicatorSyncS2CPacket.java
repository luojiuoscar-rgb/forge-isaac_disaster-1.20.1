package net.luojiuoscar.isaac_disaster.networking.packet;

import net.luojiuoscar.isaac_disaster.client.network.ClientPacketHandlers;
import net.luojiuoscar.isaac_disaster.system.attribute_indicator.AttributeSnapshot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record AttributeIndicatorSyncS2CPacket(AttributeSnapshot snapshot, boolean baseline) {
    public AttributeIndicatorSyncS2CPacket(FriendlyByteBuf buffer) {
        this(new AttributeSnapshot(buffer.readDouble(), buffer.readDouble(), buffer.readDouble(),
                buffer.readDouble(), buffer.readDouble(), buffer.readDouble()), buffer.readBoolean());
    }

    public void toBytes(FriendlyByteBuf buffer) {
        for (int row = 0; row < AttributeSnapshot.SIZE; row++) buffer.writeDouble(snapshot.value(row));
        buffer.writeBoolean(baseline);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPacketHandlers.handleAttributeIndicatorUpdate(snapshot, baseline)));
        context.setPacketHandled(true);
        return true;
    }
}
