package net.luojiuoscar.isaac_disaster.networking.packet;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.client.network.ClientPacketHandlers;
import net.luojiuoscar.isaac_disaster.registries.charge_bar.ModChargeBars;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ChargeBarUpdateS2CPacket {
    private final ResourceLocation id;
    private final boolean visible;
    private final float progress;

    public ChargeBarUpdateS2CPacket(float progress){
        this(ModChargeBars.ATTACK_CHARGE.getId(), progress > 0f, progress);
    }

    public ChargeBarUpdateS2CPacket(ResourceLocation id, boolean visible, float progress) {
        this.id = id;
        this.visible = visible;
        this.progress = progress;
        if (id == null) {
            IsaacDisaster.LOGGER.warn("Skipping charge bar packet with no registry ID");
        }
    }

    public ChargeBarUpdateS2CPacket(FriendlyByteBuf buf){
        ResourceLocation decodedId;
        boolean decodedVisible;
        float decodedProgress;
        try {
            decodedId = buf.readResourceLocation();
            decodedVisible = buf.readBoolean();
            decodedProgress = buf.readFloat();
        } catch (RuntimeException exception) {
            IsaacDisaster.LOGGER.warn("Skipping malformed charge bar packet", exception);
            decodedId = null;
            decodedVisible = false;
            decodedProgress = 0f;
        }
        this.id = decodedId;
        this.visible = decodedVisible;
        this.progress = decodedProgress;
    }

    public boolean isValid() {
        return id != null;
    }

    public void toBytes(FriendlyByteBuf buf){
        if (!isValid()) return;
        buf.writeResourceLocation(id);
        buf.writeBoolean(visible);
        buf.writeFloat(progress);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context context = supplier.get();
        if (!isValid()) {
            context.setPacketHandled(true);
            return true;
        }
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPacketHandlers.handleChargeUpdate(id, visible, progress)));
        context.setPacketHandled(true);
        return true;
    }

}
